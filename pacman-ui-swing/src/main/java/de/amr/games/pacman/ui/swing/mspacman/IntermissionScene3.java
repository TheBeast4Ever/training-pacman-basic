package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.rendering;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.soundManager;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.GameEntity;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.GameScene;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class IntermissionScene3 implements GameScene {

	enum Phase {

		ANIMATION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final Dimension size;
	private final PacManGameModel game;

	private static final int BIRD_Y = t(12), GROUND_Y = t(24);
	private static final float BIRD_SPEED = 1.2f;
	private static final V2f GRAVITY = new V2f(0, 0.04f);

	private boolean flapVisible;

	private Pac pacMan, msPac;
	private GameEntity bird = new GameEntity();
	private GameEntity bag = new GameEntity();
	private boolean bagDropped;
	private long bagOpenTimer;
	private int bounces;

	private Phase phase;

	public IntermissionScene3(Dimension size, PacManGameModel game) {
		this.size = size;
		this.game = game;
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		flapVisible = true;
		MsPacManGameRendering.assets.flapAnim.restart();
		pacMan.position = new V2f(t(3), GROUND_Y);
		msPac.position = new V2f(t(5), GROUND_Y);
		bird.position = new V2f(t(29), BIRD_Y);
		bag.position = bird.position.sum(-2, 6);
		soundManager.play(PacManGameSound.INTERMISSION_3);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			bird.move();
			bag.move();
			if (bagDropped) {
				bag.velocity = bag.velocity.sum(GRAVITY);
			}
			if (phase.timer.running() == clock.sec(1)) {
				flapVisible = false;
				pacMan.visible = true;
				msPac.visible = true;
				bird.visible = true;
				bird.velocity = new V2f(-BIRD_SPEED, 0);
				bag.visible = true;
				bag.velocity = new V2f(-BIRD_SPEED, 0);
			}
			// drop bag?
			if (differsAtMost(bird.position.x, t(22), 1)) {
				bagDropped = true;
			}
			// ground contact?
			if (bagDropped && bag.position.y > GROUND_Y) {
				++bounces;
				if (bounces < 3) {
					bag.velocity = new V2f(-0.2f, -0.8f / bounces);
					bag.position = new V2f(bag.position.x, GROUND_Y);
				} else {
					bag.velocity = V2f.NULL;
					bagOpenTimer = clock.sec(2);
				}
			}
			// bag open long enough?
			if (bagOpenTimer > 0) {
				--bagOpenTimer;
				if (bagOpenTimer == 0) {
					enter(Phase.READY_TO_PLAY, clock.sec(3));
				}
			}
			break;
		case READY_TO_PLAY:
			if (phase.timer.expired()) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		phase.timer.run();
	}

	@Override
	public void render(Graphics2D g) {
		if (flapVisible) {
			rendering.drawFlapAnimation(g, t(3), t(10), "3", "JUNIOR");
		}
		rendering.drawPac(g, msPac, game);
		rendering.drawMrPacMan(g, pacMan);
		if (bird.visible) {
			rendering.drawBirdAnim(g, bird.position.x, bird.position.y);
		}
		if (bagOpenTimer > 0) {
			rendering.drawJunior(g, bag.position.x, bag.position.y);
		} else {
			rendering.drawBlueBag(g, bag.position.x, bag.position.y);
		}
	}
}