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

package de.amr.games.pacman.controller.common;

import java.util.Objects;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;

/**
 * @author Armin Reichert
 */
public class SceneControllerContext {

	protected final GameController gameController;

	public SceneControllerContext(GameController gameController) {
		this.gameController = Objects.requireNonNull(gameController, "Game controller must not be NULL");
	}

	public GameController gameController() {
		return gameController;
	}

	public GameModel game() {
		return gameController.game();
	}

	public GameVariant gameVariant() {
		return game().variant();
	}

	public GameLevel level() {
		return game().level();
	}

	public GameState state() {
		return gameController.state();
	}

	public GameSoundController sounds() {
		return gameController.sounds();
	}

	public World world() {
		return game().level().world();
	}

	public boolean hasCredit() {
		return game().hasCredit();
	}
}