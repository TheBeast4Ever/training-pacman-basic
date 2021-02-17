package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.soundManager;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.heaven.God;
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

	private final Dimension size;
	private final PacManGameModel game;

	public IntermissionScene3(Dimension size, PacManGameModel game) {
		this.size = size;
		this.game = game;
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		soundManager.loop(PacManGameSound.INTERMISSION_3, 1);
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(6)) {
			game.state.duration(0);
		}
	}

	@Override
	public void render(Graphics2D g) {
	}
}
