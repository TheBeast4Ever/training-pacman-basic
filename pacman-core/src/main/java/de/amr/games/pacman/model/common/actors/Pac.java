/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.model.common.actors;

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.animation.AnimatedEntity;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameModel;

/**
 * (Ms.) Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity<AnimKeys> {

	private boolean dead;

	/* Number of ticks Pac is resting and not moving. */
	private int restingTicks;

	/* Number of ticks since Pac has has eaten a pellet or energizer. */
	private int starvingTicks;

	private EntityAnimationSet<AnimKeys> animationSet;

	public Pac(String name) {
		super(name);
	}

	public void update(GameModel game) {
		Objects.requireNonNull(game, "Game must not be null");
		if (dead) {
			// nothing to do
		} else {
			if (restingTicks == 0) {
				setRelSpeed(game.powerTimer().isRunning() ? game.level().playerSpeedPowered() : game.level().playerSpeed());
				tryMoving(game);
			} else {
				--restingTicks;
				setAbsSpeed(0);
			}
			selectedAnimation().ifPresent(animation -> {
				if (stuck) {
					animation.stop();
				} else {
					animation.start();
				}
			});
		}
		animate();
	}

	public void setAnimationSet(EntityAnimationSet<AnimKeys> animationSet) {
		this.animationSet = animationSet;
	}

	@Override
	public Optional<EntityAnimationSet<AnimKeys>> animationSet() {
		return Optional.ofNullable(animationSet);
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		selectAndResetAnimation(AnimKeys.PAC_MUNCHING);
	}

	public void rest(int ticks) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Resting time cannot be negative, but is: %d".formatted(ticks));
		}
		restingTicks = ticks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}

	public int starvingTicks() {
		return starvingTicks;
	}

	public void die() {
		dead = true;
		setRelSpeed(0);
		stopAnimation();
	}
}