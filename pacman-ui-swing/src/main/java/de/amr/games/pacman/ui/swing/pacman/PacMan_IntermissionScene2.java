package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene {

	public PacMan_IntermissionScene2(PacManGameController controller, Dimension size, SwingRendering rendering,
			SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	enum Phase {

		WALKING, GETTING_STUCK, STUCK;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final int groundTileY = 20;

	private Ghost blinky;
	private Pac pac;
	private GameEntity nail;

	private Phase phase;

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.setTilePosition(30, groundTileY);
		pac.visible = true;
		pac.speed = 1;
		rendering.playerAnimations().playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.setPositionRelativeTo(pac, t(14), 0);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.speed = 1;
		rendering.ghostAnimations().ghostKicking(blinky, blinky.dir).restart();

		nail = new GameEntity();
		nail.visible = true;
		nail.setPosition(t(14), t(groundTileY) - 1);

		sounds.play(PacManGameSound.INTERMISSION_2);

		enter(Phase.WALKING);
	}

	private void enter(Phase nextPhase) {
		phase = nextPhase;
		phase.timer.setDuration(Long.MAX_VALUE);
	}

	private int nailDistance() {
		return (int) (nail.position.x - blinky.position.x);
	}

	@Override
	public void update() {
		switch (phase) {
		case WALKING:
			if (nailDistance() == 0) {
				enter(Phase.GETTING_STUCK);
			}
			break;
		case GETTING_STUCK:
			int stretching = nailDistance() / 4;
			blinky.speed = 0.3f - 0.1f * stretching;
//			Logging.log("stretching: %d, pixels: %d, speed: %.2f", stretching, nailDistance(), blinky.speed);
			if (stretching == 3) {
				blinky.speed = 0;
				blinky.dir = Direction.UP;
				enter(Phase.STUCK);
			}
			break;
		case STUCK:
			if (phase.timer.running() == clock.sec(3)) {
				blinky.dir = Direction.RIGHT;
			}
			if (phase.timer.running() == clock.sec(6)) {
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
		rendering.drawNail(g, nail);
		rendering.drawPlayer(g, pac);
		if (nailDistance() < 0) {
			rendering.drawGhost(g, blinky, false);
		} else {
			rendering.drawBlinkyStretched(g, blinky, nail.position, nailDistance() / 4);
		}
	}
}