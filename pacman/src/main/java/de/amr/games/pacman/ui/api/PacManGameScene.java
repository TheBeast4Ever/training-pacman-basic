package de.amr.games.pacman.ui.api;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import de.amr.games.pacman.lib.V2i;

/**
 * Implemented by all scenes of the Pac-Man and Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameScene {

	void draw(Graphics2D g);

	V2i size();

	Optional<PacManGameAnimations> animations();

	default void start() {
	}

	default void end() {
	}

	// convenience methods

	default void drawHCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size().x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	default void drawHCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size().x - image.getWidth()) / 2, y, null);
	}
}