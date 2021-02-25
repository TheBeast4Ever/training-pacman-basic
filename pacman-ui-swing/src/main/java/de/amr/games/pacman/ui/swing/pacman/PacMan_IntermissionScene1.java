package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends GameScene {

	enum Phase {

		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int groundY = t(20);

	private Ghost blinky;
	private Pac pac;

	private Phase phase;

	public PacMan_IntermissionScene1(Dimension size, SwingRendering rendering, SoundManager sounds) {
		super(size, rendering, sounds);
	}

	@Override
	public void start() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.visible = true;
		pac.setPosition(t(30), groundY);
		pac.speed = 1.0f;
		rendering.playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.setPositionRelativeTo(pac, t(3), 0);
		blinky.speed = pac.speed * 1.04f;
		rendering.ghostKicking(blinky, blinky.dir).restart();
		rendering.ghostFrightened(blinky, blinky.dir).restart();

		sounds.loop(PacManGameSound.INTERMISSION_1, 2);

		phase = Phase.BLINKY_CHASING_PACMAN;
		phase.timer.setDuration(clock.sec(5));
	}

	@Override
	public void update() {
		switch (phase) {
		case BLINKY_CHASING_PACMAN:
			if (phase.timer.expired()) {
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
				phase.timer.setDuration(clock.sec(7));
			}
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if (phase.timer.running() == 0) {
				blinky.setPosition(-t(2), groundY);
				blinky.dir = blinky.wishDir = RIGHT;
				blinky.speed = 1f;
				blinky.state = FRIGHTENED;
				pac.dir = RIGHT;
				pac.speed = 1.3f;
				pac.setPositionRelativeTo(blinky, -t(13), 0);
			}
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		pac.move();
		blinky.move();
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawGhost(g, blinky, false);
		if (phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPlayer(g, pac);
		} else {
			rendering.drawBigPacMan(g, pac);
		}
	}
}