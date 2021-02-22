package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.model.guys.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.RENDERING_PACMAN;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;
import de.amr.games.pacman.ui.swing.scene.GameScene;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene {

	enum Phase {

		APPROACHING_NAIL, HITTING_NAIL, STRETCHED_1, STRETCHED_2, STRETCHED_3, LOOKING_UP, LOOKING_RIGHT;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final SoundManager sounds = PacManGameUI_Swing.SOUNDS_PACMAN;

	private final int chaseTileY = 20;
	private final V2i nailPosition = new V2i(t(14), t(chaseTileY) - 6);
	private Ghost blinky;
	private Pac pac;

	private Phase phase;

	public PacMan_IntermissionScene2(Dimension size) {
		super(size, RENDERING_PACMAN);
	}

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.visible = true;
		pac.setPosition(t(30), t(chaseTileY));
		pac.speed = 1;
		rendering.playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.setPosition(pac.position.sum(t(14), 0));
		blinky.speed = 1;
		rendering.ghostKicking(blinky, blinky.dir).restart();

		sounds.play(PacManGameSound.INTERMISSION_2);

		enter(Phase.APPROACHING_NAIL, Long.MAX_VALUE);
	}

	private void enter(Phase nextPhase, long ticks) {
		phase = nextPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void update() {
		int distFromNail = (int) (blinky.position.x - nailPosition.x) - 6;
		switch (phase) {
		case APPROACHING_NAIL:
			if (distFromNail == 0) {
				blinky.speed = 0;
				enter(Phase.HITTING_NAIL, clock.sec(0.1));
			}
			break;
		case HITTING_NAIL:
			if (phase.timer.expired()) {
				blinky.speed = 0.2f;
				enter(Phase.STRETCHED_1, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_1:
			if (distFromNail == -3) {
				blinky.speed = 0.15f;
				enter(Phase.STRETCHED_2, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_2:
			if (distFromNail == -6) {
				blinky.speed = 0.1f;
				enter(Phase.STRETCHED_3, Long.MAX_VALUE);
			}
			break;
		case STRETCHED_3:
			if (distFromNail == -9) {
				blinky.speed = 0;
				enter(Phase.LOOKING_UP, clock.sec(3));
			}
			break;
		case LOOKING_UP:
			if (phase.timer.expired()) {
				enter(Phase.LOOKING_RIGHT, clock.sec(3));
			}
			break;
		case LOOKING_RIGHT:
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0); // signal end of this scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawSprite(g, RENDERING_PACMAN.assets.nailImage, nailPosition.x, nailPosition.y);
		rendering.drawPlayer(g, pac);
		drawBlinky(g);
	}

	private void drawBlinky(Graphics2D g) {
		int baselineY = (int) blinky.position.y - 5;
		int blinkySpriteRightEdge = (int) blinky.position.x + 4;
		switch (phase) {
		case APPROACHING_NAIL:
		case HITTING_NAIL:
			rendering.drawGhost(g, blinky, false);
			break;
		case STRETCHED_1:
			rendering.drawSprite(g, RENDERING_PACMAN.assets.stretchedDress[0], blinkySpriteRightEdge - 8, baselineY);
			rendering.drawGhost(g, blinky, false);
			break;
		case STRETCHED_2:
			rendering.drawSprite(g, RENDERING_PACMAN.assets.stretchedDress[1], blinkySpriteRightEdge - 4, baselineY);
			rendering.drawGhost(g, blinky, false);
			break;
		case STRETCHED_3:
			rendering.drawSprite(g, RENDERING_PACMAN.assets.stretchedDress[2], blinkySpriteRightEdge - 2, baselineY);
			rendering.drawGhost(g, blinky, false);
			break;
		case LOOKING_UP:
			rendering.drawSprite(g, RENDERING_PACMAN.assets.blinkyLookingUp, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawSprite(g, RENDERING_PACMAN.assets.shred, nailPosition.x, baselineY);
			break;
		case LOOKING_RIGHT:
			rendering.drawSprite(g, RENDERING_PACMAN.assets.blinkyLookingRight, blinky.position.x - 4, blinky.position.y - 4);
			rendering.drawSprite(g, RENDERING_PACMAN.assets.shred, nailPosition.x, baselineY);
			break;
		default:
			break;
		}
	}
}