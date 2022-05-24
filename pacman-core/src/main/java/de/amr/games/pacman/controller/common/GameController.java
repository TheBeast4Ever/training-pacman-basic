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
package de.amr.games.pacman.controller.common;

import static de.amr.games.pacman.controller.common.GameState.INTERMISSION_TEST;
import static de.amr.games.pacman.controller.common.GameState.INTRO;
import static de.amr.games.pacman.controller.common.GameState.READY;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.HUNTING_PAC;
import static java.util.function.Predicate.not;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.event.GameEventType;
import de.amr.games.pacman.controller.common.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Fsm;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Controller (in the sense of MVC) for both (Pac-Man, Ms. Pac-Man) game variants.
 * <p>
 * This is a finite-state machine with states defined in {@link GameState}. The game data are stored in the model of the
 * selected game, see {@link MsPacManGame} and {@link PacManGame}. Scene selection is not controlled by this class but
 * left to the specific user interface implementations.
 * <p>
 * Missing functionality:
 * <ul>
 * <li><a href= "https://pacman.holenet.info/#CH2_Cornering"><em>Cornering</em></a>: I do not consider cornering as
 * important when the player is controlled by keyboard keys, for a joystick that probably would be more important.</li>
 * <li>Exact level data for Ms. Pac-Man still unclear. Any hints appreciated!
 * <li>Multiple players.</li>
 * </ul>
 * 
 * @author Armin Reichert
 * 
 * @see <a href="https://github.com/armin-reichert">GitHub</a>
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href= "https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 *      behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man</a>
 */
public class GameController extends Fsm<GameState, GameModel> {

	private final Map<GameVariant, GameModel> games = Map.of( //
			GameVariant.MS_PACMAN, new MsPacManGame(), //
			GameVariant.PACMAN, new PacManGame());

	private GameVariant selectedGameVariant;
	private Consumer<Pac> playerControl;
	private final Consumer<Pac> autopilot = new Autopilot(this::game);

	private int credit;
	public boolean gameRunning;

	public GameController(GameVariant variant) {
		super(GameState.values());
		for (var gameVariant : GameVariant.values()) {
			var game = games.get(gameVariant);
			addStateChangeListener(
					(oldState, newState) -> game.publishEvent(new GameStateChangeEvent(game, oldState, newState)));
		}
		selectGameVariant(variant);
		logging = true;
	}

	public int credit() {
		return credit;
	}

	public void addCredit() {
		if (state() == INTRO) {
			++credit;
		}
	}

	public void consumeCredit() {
		if (credit > 0) {
			--credit;
		}
	}

	public void setPlayerControl(Consumer<Pac> playerControl) {
		this.playerControl = playerControl;
	}

	Consumer<Pac> currentPlayerControl() {
		return game().player.autoMoving || credit == 0 ? autopilot : playerControl;
	}

	public GameVariant gameVariant() {
		return selectedGameVariant;
	}

	public void selectGameVariant(GameVariant variant) {
		selectedGameVariant = Objects.requireNonNull(variant);
		// ensure only selected game model fires events
		for (var gv : GameVariant.values()) {
			games.get(gv).setEventingEnabled(gv == selectedGameVariant);
		}
		reset(INTRO);
	}

	@Override
	public GameModel getContext() {
		return game();
	}

	public Stream<GameModel> games() {
		return games.values().stream();
	}

	public GameModel game() {
		return games.get(selectedGameVariant);
	}

	// actions

	public void requestGame() {
		if (state() == INTRO && credit > 0) {
			changeState(READY);
		}
	}

	public void startIntermissionTest() {
		if (state() == INTRO) {
			game().intermissionTestNumber = 1;
			changeState(INTERMISSION_TEST);
		}
	}

	public void cheatEatAllPellets() {
		if (gameRunning) {
			game().world.tiles().filter(not(game().world::isEnergizerTile)).forEach(game().world::removeFood);
			game().publishEvent(GameEventType.PLAYER_FOUND_FOOD, null);
		}
	}

	public void cheatKillAllPossibleGhosts() {
		if (gameRunning && state() != GameState.GHOST_DYING) {
			game().ghostBounty = game().firstGhostBounty;
			game().ghosts().filter(ghost -> ghost.is(HUNTING_PAC) || ghost.is(FRIGHTENED)).forEach(game()::killGhost);
			changeState(GameState.GHOST_DYING);
		}
	}

	public void cheatEnterNextLevel() {
		if (gameRunning) {
			game().world.tiles().forEach(game().world::removeFood);
			changeState(GameState.LEVEL_COMPLETE);
		}
	}
}