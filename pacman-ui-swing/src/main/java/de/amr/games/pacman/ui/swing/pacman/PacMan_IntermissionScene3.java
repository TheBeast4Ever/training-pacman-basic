package de.amr.games.pacman.ui.swing.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends GameScene {

	enum Phase {
		CHASING_PACMAN, RETURNING_HALF_NAKED;
	}

	private final int chaseTileY = 20;
	private Ghost blinky;
	private Pac pac;

	private Phase phase;

	public PacMan_IntermissionScene3(Dimension size, SwingRendering rendering, SoundManager sounds) {
		super(size, rendering, sounds);
	}

	@Override
	public void start() {
		log("Start intermission scene %s at %d", this, clock.ticksTotal);

		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.setTilePosition(30, chaseTileY);
		pac.visible = true;
		pac.dead = false;
		pac.speed = 1.2f;
		pac.couldMove = true;
		pac.dir = LEFT;
		pac.couldMove = true;
		rendering.playerMunching(pac).forEach(Animation::restart);

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.setPositionRelativeTo(pac, t(8), 0);
		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.speed = pac.speed;
		blinky.dir = blinky.wishDir = LEFT;

		sounds.loop(PacManGameSound.INTERMISSION_3, 2);

		phase = Phase.CHASING_PACMAN;
	}

	@Override
	public void update() {
		switch (phase) {
		case CHASING_PACMAN:
			if (blinky.position.x <= -50) {
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				phase = Phase.RETURNING_HALF_NAKED;
			}
			break;
		case RETURNING_HALF_NAKED:
			if (blinky.position.x > t(28) + 200) {
				game.state.timer.setDuration(0); // end scene
			}
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawLevelCounter(g, game, t(25), t(34));
		rendering.drawPlayer(g, pac);
		if (phase == Phase.CHASING_PACMAN) {
			rendering.drawPatchedBlinky(g, blinky);
		} else {
			rendering.drawNakedBlinky(g, blinky);
		}
	}
}