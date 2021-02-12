package de.amr.games.pacman.ui.fx.scene.mspacman;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.scene.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.scene.common.PlayScene;

public class MsPacManGameScenes {

	public final SoundManager soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getMsPacManSoundURL);

	private PacManGameScene introScene;
	private PacManGameScene playScene;
	private PacManGameScene intermissionScene1;
	private PacManGameScene intermissionScene2;
	private PacManGameScene intermissionScene3;

	public void createScenes(MsPacManGame game, double sizeX, double sizeY, double scaling) {
		introScene = new MsPacManGameIntroScene(game, sizeX, sizeY, scaling);
		playScene = new PlayScene(game, sizeX, sizeY, scaling, true);
		intermissionScene1 = introScene; // TODO
		intermissionScene2 = introScene; // TODO
		intermissionScene3 = introScene; // TODO
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
