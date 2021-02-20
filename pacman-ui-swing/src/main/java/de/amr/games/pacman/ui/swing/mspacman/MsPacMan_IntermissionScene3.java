package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.heaven.God.differsAtMost;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacMan_GameRendering.assets;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.common.AbstractGameScene;

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
public class MsPacMan_IntermissionScene3 extends AbstractGameScene {

	static class Bird extends GameEntity {

		Animation<BufferedImage> animation;

		public void draw(Graphics2D g) {
			if (visible) {
				rendering.drawBirdAnim(g, position.x, position.y);
			}
		}
	}

	static class Bag extends GameEntity {

		boolean released;
		boolean open;
		int bounces;

		@Override
		public void move() {
			if (released) {
				velocity = velocity.sum(GRAVITY);
			}
			super.move();
		}

		public void draw(Graphics2D g) {
			if (open) {
				rendering.drawJunior(g, position.x, position.y);
			} else {
				rendering.drawBlueBag(g, position.x, position.y);
			}
		}
	}

	enum Phase {

		ANIMATION, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final int BIRD_Y = t(12), GROUND_Y = t(24);
	private static final V2f GRAVITY = new V2f(0, 0.04f);

	private static final MsPacMan_GameRendering rendering = PacManGameSwingUI.msPacManGameRendering;
	private static final SoundManager sounds = PacManGameSwingUI.msPacManGameSounds;

	private Pac pacMan;
	private Pac msPac;
	private Bird bird;
	private Bag bag;
	private boolean flapVisible;

	private Phase phase;

	public MsPacMan_IntermissionScene3(Dimension size) {
		super(size);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {
		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		pacMan.setPosition(t(3), GROUND_Y - 4);

		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		msPac.setPosition(t(5), GROUND_Y - 4);

		bird = new Bird();
		bird.setPosition(t(30), BIRD_Y);
		bird.animation = assets.birdAnim;
		bird.animation.restart();

		bag = new Bag();
		bag.setPosition(bird.position.sum(-14, 3));

		flapVisible = true;
		assets.flapAnim.restart();

		sounds.play(PacManGameSound.INTERMISSION_3);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			bird.move();
			bag.move();
			if (phase.timer.running() == clock.sec(1)) {
				flapVisible = false;
				pacMan.visible = true;
				msPac.visible = true;
				bird.visible = true;
				bird.velocity = new V2f(-1.25f, 0);
				bag.visible = true;
				bag.velocity = bird.velocity;
			}
			// drop bag?
			if (differsAtMost(bird.position.x, t(22), 1)) {
				bag.released = true;
			}
			// ground contact?
			if (!bag.open && bag.released && bag.position.y > GROUND_Y) {
				++bag.bounces;
				if (bag.bounces < 3) {
					bag.velocity = new V2f(-0.2f, -0.9f / bag.bounces);
					bag.setPosition(bag.position.x, GROUND_Y);
				} else {
					bag.velocity = V2f.NULL;
					bag.open = true;
				}
			}
			if (bag.open) {
				enter(Phase.READY_TO_PLAY, clock.sec(3));
			}
			break;
		case READY_TO_PLAY:
			bird.move();
			if (phase.timer.expired()) {
				game.state.timer.setDuration(0); // expires game state
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
		bird.draw(g);
		bag.draw(g);
	}
}