/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.*;

/**
 * Rule of thumb: here, specify "what" and "when", not "how" (this should be implemented in the model).
 * 
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	BOOT { // "Das muss das Boot abkönnen!"
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.clearLevelCounter();
			game.score().reset();
			game.loadHighscore();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				GameController.it().changeState(INTRO);
			}
		}
	},

	INTRO {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.setPlaying(false);
			game.removeLevel();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				GameController.it().changeState(READY);
			}
		}
	},

	CREDIT {
		@Override
		public void onUpdate(GameModel game) {
			// nothing to do here
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			GameController.it().getManualPacSteering().setEnabled(false);
			GameController.publishSoundEvent(SoundEvent.STOP_ALL_SOUNDS);
			if (!game.hasCredit()) {
				game.start();
				game.enterDemoLevel();
				GameController.publishGameEventOfType(GameEvent.LEVEL_STARTED);
			} else if (game.isPlaying()) {
				game.level().ifPresent(level -> level.letsGetReadyToRumbleAndShowGuys(true));
			} else {
				game.start();
				game.score().reset();
				game.clearLevelCounter();
				game.enterLevel(1);
				GameController.publishSoundEvent(SoundEvent.READY_TO_PLAY);
				GameController.publishGameEventOfType(GameEvent.LEVEL_STARTED);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			final int showGuysTick = 120; // not sure
			game.level().ifPresent(level -> {
				if (game.hasCredit() && !game.isPlaying()) {
					// start new game
					if (timer.tick() == showGuysTick) {
						level.guys().forEach(Creature::show);
						game.setOneLessLifeDisplayed(true);
					} else if (timer.tick() == showGuysTick + 120) {
						// start playing
						game.setPlaying(true);
						level.startHunting(0);
						GameController.it().changeState(GameState.HUNTING);
					}
				} else if (game.isPlaying()) {
					// game already running
					if (timer.tick() == 90) {
						level.guys().forEach(Creature::show);
						level.startHunting(0);
						GameController.it().changeState(GameState.HUNTING);
					}
				} else {
					// attract mode
					if (timer.tick() == 130) {
						level.guys().forEach(Creature::show);
						level.startHunting(0);
						GameController.it().changeState(GameState.HUNTING);
					}
				}
			});
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				GameController.it().getManualPacSteering().setEnabled(true);
				switch (level.huntingPhase()) {
				case 0:
					GameController.publishSoundEvent(SoundEvent.HUNTING_PHASE_STARTED_0);
					break;
				case 2:
					GameController.publishSoundEvent(SoundEvent.HUNTING_PHASE_STARTED_2);
					break;
				case 4:
					GameController.publishSoundEvent(SoundEvent.HUNTING_PHASE_STARTED_4);
					break;
				case 6:
					GameController.publishSoundEvent(SoundEvent.HUNTING_PHASE_STARTED_6);
					break;
				default:
					break;
				}
				level.pac().startAnimation();
				level.ghosts().forEach(Ghost::startAnimation);
				level.world().energizerBlinking().restart();
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				// TODO this looks ugly
				var steering = level.pacSteering().orElse(GameController.it().steering());
				steering.steer(level, level.pac());
				level.update();
				level.world().energizerBlinking().tick();
				if (level.isCompleted()) {
					GameController.it().changeState(LEVEL_COMPLETE);
				} else if (level.pacKilled()) {
					GameController.it().changeState(PACMAN_DYING);
				} else if (level.memo().edibleGhostsExist()) {
					level.killEdibleGhosts();
					GameController.it().changeState(GHOST_DYING);
				}
			});
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			GameController.it().getManualPacSteering().setEnabled(false);
			timer.restartSeconds(4);
			game.level().ifPresent(GameLevel::exit);
			GameController.publishSoundEvent(SoundEvent.STOP_ALL_SOUNDS);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						GameController.it().changeState(INTRO);
						// attract mode -> back to intro scene
					} else if (level.intermissionNumber > 0) {
						GameController.it().changeState(INTERMISSION); // play intermission scene
					} else {
						GameController.it().changeState(CHANGING_TO_NEXT_LEVEL); // next level
					}
				} else {
					level.pac().stopAnimation();
					level.pac().resetAnimation();
					var flashing = level.world().mazeFlashing();
					if (timer.atSecond(1)) {
						flashing.restart(2 * level.numFlashes);
					} else {
						flashing.tick();
					}
					level.pac().update();
				}
			});
		}
	},

	CHANGING_TO_NEXT_LEVEL {
		@Override
		public void onEnter(GameModel game) {
			GameController.it().getManualPacSteering().setEnabled(false);
			timer.restartSeconds(1);
			game.nextLevel();
			GameController.publishGameEventOfType(GameEvent.LEVEL_STARTED);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				GameController.it().changeState(READY);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.level().ifPresent(level -> {
				level.pac().hide();
				level.ghosts().forEach(ghost -> ghost.animations().ifPresent(Animations::stopSelected));
				GameController.publishSoundEvent(SoundEvent.GHOST_EATEN);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				GameController.it().resumePreviousState();
			} else {
				game.level().ifPresent(level -> {
					var steering = level.pacSteering().orElse(GameController.it().steering());
					steering.steer(level, level.pac());
					level.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
							.forEach(Ghost::update);
					level.world().energizerBlinking().tick();
				});
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				level.pac().show();
				level.ghosts(GhostState.EATEN).forEach(Ghost::enterStateReturningToHouse);
				level.ghosts().forEach(ghost -> ghost.animations().ifPresent(Animations::startSelected));
			});
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				GameController.it().getManualPacSteering().setEnabled(false);
				timer.restartSeconds(4);
				level.onPacKilled();
				GameController.publishSoundEvent(SoundEvent.STOP_ALL_SOUNDS);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.atSecond(1)) {
					level.pac().selectAnimation(PacAnimations.DYING);
					level.pac().resetAnimation();
					level.ghosts().forEach(Ghost::hide);
				} else if (timer.atSecond(1.4)) {
					level.pac().startAnimation();
					GameController.publishSoundEvent(SoundEvent.PACMAN_DEATH);
				} else if (timer.atSecond(3.0)) {
					level.pac().hide();
					game.setLives(game.lives() - 1);
					if (game.lives() == 0) {
						level.world().mazeFlashing().stop();
						game.setOneLessLifeDisplayed(false);
					}
				} else if (timer.hasExpired()) {
					if (!game.hasCredit()) {
						// end of demo level
						GameController.setSoundEventsEnabled(true);
						GameController.it().changeState(INTRO);
					} else {
						GameController.it().changeState(game.lives() == 0 ? GAME_OVER : READY);
					}
				} else {
					level.world().energizerBlinking().tick();
					level.pac().update();
				}
			});
		}

		@Override
		public void onExit(GameModel context) {
			context.level().ifPresent(level -> level.deactivateBonus());
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			GameController.it().getManualPacSteering().setEnabled(false);
			timer.restartSeconds(1.2);
			game.changeCredit(-1);
			game.saveNewHighscore();
			GameController.publishSoundEvent(SoundEvent.STOP_ALL_SOUNDS);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				GameController.it().changeState(game.hasCredit() ? CREDIT : INTRO);
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.setPlaying(false);
			game.removeLevel();
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				GameController.it().changeState(game.hasCredit() && game.isPlaying() ? CHANGING_TO_NEXT_LEVEL : INTRO);
			}
		}
	},

	LEVEL_TEST {
		private int lastTestedLevel;

		@Override
		public void onEnter(GameModel game) {
			switch (game.variant()) {
			case MS_PACMAN:
				lastTestedLevel = 18;
				break;
			case PACMAN:
				lastTestedLevel = 20;
				break;
			default:
				break;
			}
			timer.restartIndefinitely();
			game.start();
			game.enterLevel(1);
			GameController.publishGameEventOfType(GameEvent.LEVEL_STARTED);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (level.number() <= lastTestedLevel) {
					if (timer.atSecond(0.5)) {
						level.guys().forEach(Creature::show);
					} else if (timer.atSecond(1.5)) {
						level.handleBonusReached(0);
					} else if (timer.atSecond(2.5)) {
						level.getBonus().ifPresent(bonus -> bonus.setEaten(120));
						GameController.publishSoundEvent(SoundEvent.BONUS_EATEN);
					} else if (timer.atSecond(4.5)) {
						level.handleBonusReached(1);
					} else if (timer.atSecond(5.5)) {
						level.getBonus().ifPresent(bonus -> bonus.setEaten(60));
						level.guys().forEach(Creature::hide);
					} else if (timer.atSecond(6.5)) {
						var flashing = level.world().mazeFlashing();
						flashing.restart(2 * level.numFlashes);
					} else if (timer.atSecond(12.0)) {
						level.exit();
						game.nextLevel();
						timer.restartIndefinitely();
						GameController.publishGameEventOfType(GameEvent.LEVEL_STARTED);
					}
					level.world().energizerBlinking().tick();
					level.world().mazeFlashing().tick();
					level.ghosts().forEach(Ghost::update);
					level.updateBonus();
				} else {
					GameController.it().restart(GameState.BOOT);
				}
			});
		}

		@Override
		public void onExit(GameModel game) {
			game.clearLevelCounter();
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (game.intermissionTestNumber < 3) {
					++game.intermissionTestNumber;
					timer.restartIndefinitely();
					GameController.publishGameEventOfType(GameEvent.UNSPECIFIED_CHANGE);
				} else {
					game.intermissionTestNumber = 1;
					GameController.it().changeState(INTRO);
				}
			}
		}
	};

	final TickTimer timer = new TickTimer("Timer-" + name());

	@Override
	public TickTimer timer() {
		return timer;
	}
}