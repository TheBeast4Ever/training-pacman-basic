package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.RENDERING_MSPACMAN;
import static de.amr.games.pacman.ui.swing.PacManGameUI_Swing.SOUNDS_MSPACMAN;
import static de.amr.games.pacman.ui.swing.rendering.MsPacMan_Rendering.assets;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.mspacman.entities.Flap;
import de.amr.games.pacman.ui.swing.mspacman.entities.Heart;
import de.amr.games.pacman.ui.swing.scene.GameScene;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends GameScene {

	enum Phase {

		FLAP, CHASED_BY_GHOSTS, COMING_TOGETHER, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int upperY = t(12), lowerY = t(24), middleY = t(18);

	private Phase phase;

	private Flap flap;
	private Pac pacMan, msPac;
	private Ghost pinky, inky;
	private Heart heart;
	private boolean ghostsMet;

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	public MsPacMan_IntermissionScene1(Dimension size) {
		super(size, RENDERING_MSPACMAN, SOUNDS_MSPACMAN);
	}

	@Override
	public void start() {

		flap = new Flap(1, "THEY MEET");
		flap.setPosition(t(3), t(10));
		flap.visible = true;
		flap.animation.restart();

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(-t(2), upperY);
		pacMan.visible = true;
		assets.pacManMunching.values().forEach(Animation::restart);

		inky = new Ghost(2, "Inky", Direction.RIGHT);
		inky.setPosition(pacMan.position.sum(-t(3), 0));
		inky.visible = true;

		msPac = new Pac("Ms. Pac-Man", Direction.LEFT);
		msPac.setPosition(t(30), lowerY);
		msPac.visible = true;
		rendering.playerMunching(msPac).forEach(Animation::restart);

		pinky = new Ghost(1, "Pinky", Direction.LEFT);
		pinky.setPosition(msPac.position.sum(t(3), 0));
		pinky.visible = true;

		heart = new Heart();
		heart.visible = false;

		rendering.ghostsKicking(Stream.of(inky, pinky)).forEach(Animation::restart);

		sounds.loop(PacManGameSound.INTERMISSION_1, 1);

		ghostsMet = false;

		enter(Phase.FLAP, clock.sec(1));
	}

	@Override
	public void update() {
		switch (phase) {
		case FLAP:
			if (phase.timer.expired()) {
				flap.visible = false;
				startChasedByGhosts();
			}
			break;

		case CHASED_BY_GHOSTS:
			inky.move();
			pacMan.move();
			pinky.move();
			msPac.move();
			if (inky.position.x > t(30)) {
				startComingTogether();
			}
			break;

		case COMING_TOGETHER:
			inky.move();
			pinky.move();
			pacMan.move();
			msPac.move();
			if (pacMan.dir == Direction.LEFT && pacMan.position.x < t(15)) {
				pacMan.dir = msPac.dir = Direction.UP;
			}
			if (pacMan.dir == Direction.UP && pacMan.position.y < upperY) {
				pacMan.speed = msPac.speed = 0;
				pacMan.dir = Direction.LEFT;
				msPac.dir = Direction.RIGHT;
				heart.setPosition((pacMan.position.x + msPac.position.x) / 2, pacMan.position.y - t(2));
				heart.visible = true;
				rendering.ghostKicking(inky).forEach(Animation::reset);
				rendering.ghostKicking(pinky).forEach(Animation::reset);
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			if (!ghostsMet && inky.position.x - pinky.position.x < 16) {
				ghostsMet = true;
				inky.dir = inky.wishDir = inky.dir.opposite();
				pinky.dir = pinky.wishDir = pinky.dir.opposite();
				inky.speed = pinky.speed = 0.2f;
			}
			break;

		case READY_TO_PLAY:
			if (phase.timer.running() == clock.sec(0.5)) {
				inky.visible = false;
				pinky.visible = false;
			}
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0);
			}
			break;

		default:
			break;
		}
		phase.timer.run();
	}

	private void startChasedByGhosts() {
		pacMan.speed = msPac.speed = 1;
		inky.speed = pinky.speed = 1.04f;
		enter(Phase.CHASED_BY_GHOSTS, Long.MAX_VALUE);
	}

	private void startComingTogether() {
		pacMan.setPosition(t(30), middleY);
		inky.setPosition(t(33), middleY);
		pacMan.dir = Direction.LEFT;
		inky.dir = inky.wishDir = Direction.LEFT;
		pinky.setPosition(t(-5), middleY);
		msPac.setPosition(t(-2), middleY);
		msPac.dir = Direction.RIGHT;
		pinky.dir = pinky.wishDir = Direction.RIGHT;
		enter(Phase.COMING_TOGETHER, Long.MAX_VALUE);
	}

	@Override
	public void render(Graphics2D g) {
		flap.draw(g);
		rendering.drawPlayer(g, msPac);
		rendering.drawSpouse(g, pacMan);
		rendering.drawGhost(g, inky, false);
		rendering.drawGhost(g, pinky, false);
		heart.draw(g);
	}
}