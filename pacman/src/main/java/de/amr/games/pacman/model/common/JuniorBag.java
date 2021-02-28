package de.amr.games.pacman.model.common;

/**
 * Blue bag dropped by the stork in intermission scene 3, contains Pac-Man junior.
 * 
 * @author Armin Reichert
 */
public class JuniorBag extends GameEntity {

	public boolean released = false;
	public boolean open = false;
	public int bounces = 0;

	@Override
	public void move() {
		if (released) {
			velocity = velocity.plus(0, 0.04f); // gravity
		}
		super.move();
	}
}