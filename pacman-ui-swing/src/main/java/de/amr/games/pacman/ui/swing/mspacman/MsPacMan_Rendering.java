package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.model.guys.GhostState.DEAD;
import static de.amr.games.pacman.model.guys.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;
import de.amr.games.pacman.ui.swing.rendering.DefaultGameRendering;

/**
 * Rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_Rendering extends DefaultGameRendering {

	public static final MsPacMan_Assets assets = new MsPacMan_Assets();

	@Override
	public Font scoreFont() {
		return assets.scoreFont;
	}

	@Override
	public Spritesheet spritesheet() {
		return assets;
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return assets.energizerBlinkingAnim;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		// TODO this is silly
		return assets.mazesFlashingAnims.stream().map(Animation.class::cast);
	}

	@Override
	public Animation<BufferedImage> mazeFlashing(int mazeNumber) {
		return assets.mazesFlashingAnims.get(mazeNumber - 1);
	}

	@Override
	public BufferedImage bonusSprite(Bonus bonus, GameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return assets.symbolSprites[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return assets.bonusValueSprites.get(bonus.points);
		}
		return null;
	}

	@Override
	public BufferedImage lifeSprite() {
		return assets.lifeSprite;
	}

	@Override
	public BufferedImage pacSprite(Pac pac, GameModel game) {
		if (pac.dead) {
			return playerDying().hasStarted() ? playerDying().animate() : playerMunching(pac, pac.dir).frame();
		}
		return pac.speed == 0 || !pac.couldMove ? playerMunching(pac, pac.dir).frame(1)
				: playerMunching(pac, pac.dir).animate();
	}

	@Override
	public BufferedImage ghostSprite(Ghost ghost, GameModel game) {
		if (ghost.bounty > 0) {
			return assets.bountyNumberSprites.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		return ghostKicking(ghost, ghost.wishDir).animate();
	}

	@Override
	public Animation<BufferedImage> playerMunching(Pac pac, Direction dir) {
		return assets.msPacManMunchingAnimByDir.get(dir);
	}

	@Override
	public Animation<BufferedImage> playerDying() {
		return assets.msPacManSpinningAnim;
	}

	@Override
	public Animation<BufferedImage> ghostKicking(Ghost ghost, Direction dir) {
		return assets.ghostsKickingAnimsByGhost.get(ghost.id).get(dir);
	}

	@Override
	public Animation<BufferedImage> ghostFrightened(Ghost ghost, Direction dir) {
		return assets.ghostBlueAnim;
	}

	@Override
	public Animation<BufferedImage> ghostFlashing() {
		return assets.ghostFlashingAnim;
	}

	@Override
	public Animation<BufferedImage> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return assets.ghostEyesAnimByDir.get(dir);
	}

	@Override
	public Animation<?> storkFlying() {
		return null; // TODO
	}

	@Override
	public void drawFullMaze(Graphics2D g, GameModel game, int mazeNumber, int x, int y) {
		g.drawImage(assets.mazeFullImages.get(mazeNumber - 1), x, y, null);
	}

	@Override
	public void drawEmptyMaze(Graphics2D g, GameModel game, int mazeNumber, int x, int y) {
		g.drawImage(assets.mazeEmptyImages.get(mazeNumber - 1), x, y, null);
	}

	@Override
	public void drawScore(Graphics2D g, GameModel game, int x, int y) {
		g.setFont(assets.getScoreFont());
		g.translate(0, assets.scoreFont.getSize() + 1);
		g.setColor(Color.WHITE);
		g.drawString("SCORE", x, y);
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setColor(assets.getMazeWallColor(game.level.mazeNumber - 1));
			g.drawString(String.format("%08d", game.score), x, y + t(1));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.currentLevelNumber), x + t(8), y + t(1));
		}
		g.translate(0, -(assets.scoreFont.getSize() + 2));
	}

	@Override
	public void drawHiScore(Graphics2D g, GameModel game, int x, int y) {
		g.setFont(assets.getScoreFont());
		g.translate(0, assets.scoreFont.getSize() + 1);
		g.setColor(Color.WHITE);
		g.drawString("HIGHSCORE", x, y);
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setColor(assets.getMazeWallColor(game.level.mazeNumber - 1));
			g.drawString(String.format("%08d", game.highscorePoints), x, y + t(1));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(String.format("L%02d", game.highscoreLevel), x + t(8), y + t(1));
		}
		g.translate(0, -(assets.scoreFont.getSize() + 2));
	}

	@Override
	public void drawLevelCounter(Graphics2D g, GameModel game, int rightX, int y) {
		Graphics2D g2 = smoothGC(g);
		int x = rightX;
		for (int levelNumber = 1; levelNumber <= Math.min(game.currentLevelNumber, 7); ++levelNumber) {
			byte symbol = game.levelSymbols.get(levelNumber - 1);
			g2.drawImage(assets.symbolSprites[symbol], x, y, null);
			x -= t(2);
		}
		g2.dispose();
	}

	@Override
	public void drawBonus(Graphics2D g, Bonus bonus, GameModel game) {
		// Ms. Pac.Man bonus is jumping while wandering the maze
		int dy = game.bonus.edibleTicksLeft > 0 ? assets.bonusJumpAnim.animate() : 0;
		g.translate(0, dy);
		drawGuy(g, game.bonus, game);
		g.translate(0, -dy);
	}

	public void drawMrPacMan(Graphics2D g, Pac pacMan) {
		if (pacMan.visible) {
			Animation<BufferedImage> munching = assets.pacManMunching.get(pacMan.dir);
			drawImage(g, pacMan.speed > 0 ? munching.animate() : munching.frame(1), pacMan.position.x - 4,
					pacMan.position.y - 4, true);
		}
	}

	public void drawHeart(Graphics2D g, float x, float y) {
		drawImage(g, assets.s(2, 10), x, y, true);
	}

	public void drawBirdAnim(Graphics2D g, float x, float y) {
		BufferedImage frame = assets.birdAnim.animate();
		drawImage(g, frame, x + 4 - frame.getWidth() / 2, y + 4 - frame.getHeight() / 2, true);
	}

	public void drawJunior(Graphics2D g, float x, float y) {
		BufferedImage frame = assets.junior;
		drawImage(g, frame, x + 4 - frame.getWidth() / 2, y + 4 - frame.getHeight() / 2, true);
	}

	public void drawBlueBag(Graphics2D g, float x, float y) {
		BufferedImage frame = assets.blueBag;
		drawImage(g, frame, x + 4 - frame.getWidth() / 2, y + 4 - frame.getHeight() / 2, true);
	}
}