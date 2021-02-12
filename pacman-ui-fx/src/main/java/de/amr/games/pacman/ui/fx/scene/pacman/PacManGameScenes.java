package de.amr.games.pacman.ui.fx.scene.pacman;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.scene.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.scene.common.PlayScene;

public class PacManGameScenes {

	public final SoundManager soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);;

	private PacManGameScene introScene;
	private PacManGameScene playScene;
	private PacManGameScene intermissionScene1;
	private PacManGameScene intermissionScene2;
	private PacManGameScene intermissionScene3;

	public void createScenes(PacManGame game, double sizeX, double sizeY, double scaling) {
		introScene = new PacManGameIntroScene(game, sizeX, sizeY, scaling);
		playScene = new PlayScene(game, sizeX, sizeY, scaling, false);
		intermissionScene1 = new PacManGameIntermissionScene1(game, soundManager, sizeX, sizeY, scaling);
		intermissionScene2 = new PacManGameIntermissionScene2(game, soundManager, sizeX, sizeY, scaling);
		intermissionScene3 = new PacManGameIntermissionScene3(game, soundManager, sizeX, sizeY, scaling);
	}

	public PacManGameScene selectScene(PacManGameModel game) {
		if (game.state == PacManGameState.INTRO) {
			return introScene;
		}
		if (game.state == PacManGameState.INTERMISSION) {
			if (game.intermissionNumber == 1) {
				return intermissionScene1;
			}
			if (game.intermissionNumber == 2) {
				return intermissionScene2;
			}
			if (game.intermissionNumber == 3) {
				return intermissionScene3;
			}
		}
		return playScene;
	}
}
