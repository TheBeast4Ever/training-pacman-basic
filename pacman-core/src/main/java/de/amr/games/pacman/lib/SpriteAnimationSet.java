/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.lib;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 * 
 * @param <ENTITY> entity type for example Ghost or Pac
 * @param <KEY>    Key enum type
 * @param <SPRITE> Sprite type (Rectangle, Image)
 */
public abstract class SpriteAnimationSet<ENTITY, KEY extends Enum<KEY>, SPRITE> {

	protected KEY selectedKey;

	public abstract ISpriteAnimation animation(KEY key);

	public abstract Stream<ISpriteAnimation> animations();

	public abstract SPRITE currentSprite(ENTITY entity);

	public void selectAnimation(KEY key) {
		animation(key).ensureRunning();
		selectedKey = key;
	}

	public KEY selectedKey() {
		return selectedKey;
	}

	public ISpriteAnimation selectedAnimation() {
		return animation(selectedKey);
	}

	public void stop(KEY key) {
		animation(key).stop();
	}

	public void run(KEY key) {
		animation(key).run();
	}

	public void restart(KEY key) {
		animation(key).restart();
	}

	public void reset() {
		animations().forEach(ISpriteAnimation::reset);
	}

	public void stop() {
		animations().forEach(ISpriteAnimation::stop);
	}

	public void run() {
		animations().forEach(ISpriteAnimation::run);
	}

	public void restart() {
		animations().forEach(ISpriteAnimation::restart);
	}
}