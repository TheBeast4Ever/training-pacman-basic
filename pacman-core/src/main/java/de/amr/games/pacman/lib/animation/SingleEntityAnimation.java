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
package de.amr.games.pacman.lib.animation;

/**
 * Time-controlled sequence of things, for example of boolean values, numbers, images, spritesheet regions etc.
 * 
 * @param <T> type of things to be animated
 * 
 * @author Armin Reichert
 */
public class SingleEntityAnimation<T> implements EntityAnimation {

	public static final int INDEFINITE = -1;

	/**
	 * @param ticks duration (in ticks) of a single pulse
	 * @return an endless sequence of {@code (true, false)}, each value taking {@code ticks} ticks
	 */
	public static SingleEntityAnimation<Boolean> pulse(int ticks) {
		var pulse = new SingleEntityAnimation<>(true, false);
		pulse.setFrameDuration(ticks);
		pulse.repeatForever();
		return pulse;
	}

	protected T[] things;
	protected int repetitions;
	protected long totalRunningTicks;
	protected long frameDurationTicks;
	protected long frameRunningTicks;
	protected int frameIndex;
	protected long loopIndex;
	protected boolean running;
	protected boolean complete;

	@SafeVarargs
	public SingleEntityAnimation(T... things) {
		if (things.length == 0) {
			throw new IllegalArgumentException("Sequence must have at least contain one thing");
		}
		this.things = things;
		repetitions = 1;
		frameDurationTicks = 6; // 0.1 sec
		reset();
	}

	@Override
	public void reset() {
		totalRunningTicks = 0;
		frameRunningTicks = 0;
		frameIndex = 0;
		loopIndex = 0;
		running = false;
		complete = false;
	}

	@Override
	public void setFrameDuration(long ticks) {
		frameDurationTicks = ticks;
	}

	public int repetitions() {
		return repetitions;
	}

	@Override
	public void setRepetions(int n) {
		repetitions = n;
	}

	public void repeatForever() {
		repetitions = INDEFINITE;
	}

	@Override
	public void run() {
		running = true;
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public T animate() {
		T currentThing = things[frameIndex];
		advance();
		return currentThing;
	}

	@Override
	public T frame() {
		return things[frameIndex];
	}

	@Override
	public void advance() {
		if (running) {
			if (frameRunningTicks + 1 < frameDurationTicks) {
				frameRunningTicks++;
			} else if (frameIndex + 1 < things.length) {
				// start next frame
				frameIndex++;
				frameRunningTicks = 0;
			} else if (loopIndex + 1 < repetitions) {
				// start next loop
				loopIndex++;
				frameIndex = 0;
				frameRunningTicks = 0;
			} else if (repetitions != INDEFINITE) {
				// last loop complete
				complete = true;
				running = false;
			} else {
				loopIndex = 0;
				frameIndex = 0;
				frameRunningTicks = 0;
			}
		}
	}

	@Override
	public T frame(int i) {
		return things[i];
	}

	public int frameIndex() {
		return frameIndex;
	}

	@Override
	public void setFrameIndex(int i) {
		frameIndex = i;
		frameRunningTicks = 0;
	}

	public long getFrameDuration() {
		return frameDurationTicks;
	}

	public long duration() {
		return things.length * frameDurationTicks;
	}

	@Override
	public int numFrames() {
		return things.length;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void ensureRunning() {
		if (!running) {
			complete = false;
			running = true;
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean hasStarted() {
		return running || complete;
	}
}