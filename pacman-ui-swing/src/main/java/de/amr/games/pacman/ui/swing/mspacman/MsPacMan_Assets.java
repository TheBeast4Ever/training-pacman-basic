package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.font;
import static de.amr.games.pacman.ui.swing.assets.AssetLoader.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.swing.assets.Spritesheet;

/**
 * Sprites, animations, images etc. used in Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_Assets extends Spritesheet {

	/** Sprite sheet order of directions. */
	static final List<Direction> order = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	static int index(Direction dir) {
		return order.indexOf(dir);
	}

	//@formatter:off
	static final Color[] mazeWallColors = { 
		new Color(255, 183, 174), 
		new Color(71, 183, 255), 
		new Color(222, 151, 81),
		new Color(33, 33, 255), 
		new Color(255, 183, 255), 
		new Color(255, 183, 174)
	};

	static final Color[] mazeWallBorderColors = { 
		new Color(255, 0, 0), 
		new Color(222, 222, 255),
		new Color(222, 222, 255), 
		new Color(255, 183, 81), 
		new Color(255, 255, 0), 
		new Color(255, 0, 0),
	};
	//@formatter:on

	final Font scoreFont;

	final BufferedImage[] symbolSprites;
	final Map<Integer, BufferedImage> bonusValueSprites;
	final Map<Integer, BufferedImage> bountyNumberSprites;

	final BufferedImage lifeSprite;
	final List<BufferedImage> mazeEmptyImages;
	final List<BufferedImage> mazeFullImages;
	final List<Animation<BufferedImage>> mazesFlashingAnims;
	final Animation<Boolean> energizerBlinkingAnim;
	final EnumMap<Direction, Animation<BufferedImage>> msPacManMunchingAnimByDir;
	final Animation<BufferedImage> msPacManSpinningAnim;
	final Map<Direction, Animation<BufferedImage>> pacManMunching; // used in intermission scene
	final List<EnumMap<Direction, Animation<BufferedImage>>> ghostsKickingAnimsByGhost;
	final EnumMap<Direction, Animation<BufferedImage>> ghostEyesAnimByDir;
	final Animation<BufferedImage> ghostBlueAnim;
	final Animation<BufferedImage> ghostFlashingAnim;
	final Animation<Integer> bonusJumpAnim;
	final Animation<BufferedImage> birdAnim;
	final BufferedImage blueBag;
	final BufferedImage junior;

	public MsPacMan_Assets() {
		super(image("/mspacman/graphics/sprites.png"), 16);

		scoreFont = font("/emulogic.ttf", 8);

		// Left part of spritesheet contains the 6 mazes, rest is on the right
		mazeEmptyImages = new ArrayList<>(6);
		mazeFullImages = new ArrayList<>(6);
		mazesFlashingAnims = new ArrayList<>(6);
		for (int i = 0; i < 6; ++i) {
			mazeFullImages.add(sheet.getSubimage(0, i * 248, 226, 248));
			mazeEmptyImages.add(sheet.getSubimage(226, i * 248, 226, 248));
			BufferedImage mazeEmpzyBright = createBrightEffect(mazeEmptyImages.get(i), getMazeWallBorderColor(i),
					getMazeWallColor(i));
			mazesFlashingAnims.add(Animation.of(mazeEmpzyBright, mazeEmptyImages.get(i)).frameDuration(15));
		}

		energizerBlinkingAnim = Animation.pulse().frameDuration(10);

		// Switch to right part of spritesheet
		int originX = 456;

		lifeSprite = sprite(originX, 0, 1, 0);
		symbolSprites = new BufferedImage[] { sprite(originX, 0, 3, 0), sprite(originX, 0, 4, 0), sprite(originX, 0, 5, 0),
				sprite(originX, 0, 6, 0), sprite(originX, 0, 7, 0), sprite(originX, 0, 8, 0), sprite(originX, 0, 9, 0) };

		//@formatter:off
		bonusValueSprites = new HashMap<>();
		bonusValueSprites.put(100,  sprite(originX, 0, 3, 1));
		bonusValueSprites.put(200,  sprite(originX, 0, 4, 1));
		bonusValueSprites.put(500,  sprite(originX, 0, 5, 1));
		bonusValueSprites.put(700,  sprite(originX, 0, 6, 1));
		bonusValueSprites.put(1000, sprite(originX, 0, 7, 1));
		bonusValueSprites.put(2000, sprite(originX, 0, 8, 1));
		bonusValueSprites.put(5000, sprite(originX, 0, 9, 1));
		
		bountyNumberSprites = new HashMap<>();
		bountyNumberSprites.put(200, sprite(originX, 0, 0,8));
		bountyNumberSprites.put(400, sprite(originX, 0, 1,8));
		bountyNumberSprites.put(800, sprite(originX, 0, 2,8));
		bountyNumberSprites.put(1600, sprite(originX, 0, 3,8));
		//@formatter:on

		msPacManMunchingAnimByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<BufferedImage> munching = Animation.of(sprite(originX, 0, 0, d), sprite(originX, 0, 1, d),
					sprite(originX, 0, 2, d), sprite(originX, 0, 1, d));
			munching.frameDuration(2).endless();
			msPacManMunchingAnimByDir.put(dir, munching);
		}

		msPacManSpinningAnim = Animation.of(sprite(originX, 0, 0, 3), sprite(originX, 0, 0, 0), sprite(originX, 0, 0, 1),
				sprite(originX, 0, 0, 2));
		msPacManSpinningAnim.frameDuration(10).repetitions(2);

		pacManMunching = new EnumMap<>(Direction.class);
		pacManMunching.put(Direction.RIGHT, Animation
				.of(sprite(originX, 0, 0, 9), sprite(originX, 0, 1, 9), sprite(originX, 0, 2, 9)).endless().frameDuration(2));
		pacManMunching.put(Direction.LEFT, Animation
				.of(sprite(originX, 0, 0, 10), sprite(originX, 0, 1, 10), sprite(originX, 0, 2, 9)).endless().frameDuration(2));
		pacManMunching.put(Direction.UP, Animation
				.of(sprite(originX, 0, 0, 11), sprite(originX, 0, 1, 11), sprite(originX, 0, 2, 9)).endless().frameDuration(2));
		pacManMunching.put(Direction.DOWN, Animation
				.of(sprite(originX, 0, 0, 12), sprite(originX, 0, 1, 12), sprite(originX, 0, 2, 9)).endless().frameDuration(2));

		ghostsKickingAnimsByGhost = new ArrayList<>(4);
		for (int g = 0; g < 4; ++g) {
			EnumMap<Direction, Animation<BufferedImage>> kickingByDir = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<BufferedImage> kicking = Animation.of(sprite(originX, 0, 2 * d, 4 + g),
						sprite(originX, 0, 2 * d + 1, 4 + g));
				kicking.frameDuration(4).endless();
				kickingByDir.put(dir, kicking);
			}
			ghostsKickingAnimsByGhost.add(kickingByDir);
		}

		ghostEyesAnimByDir = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnimByDir.put(dir, Animation.ofSingle(sprite(originX, 0, 8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(sprite(originX, 0, 8, 4), sprite(originX, 0, 9, 4));
		ghostBlueAnim.frameDuration(20).endless().run();

		ghostFlashingAnim = Animation.of(sprite(originX, 0, 8, 4), sprite(originX, 0, 9, 4), sprite(originX, 0, 10, 4),
				sprite(originX, 0, 11, 4));
		ghostFlashingAnim.frameDuration(5).endless();

		bonusJumpAnim = Animation.of(2, -2).frameDuration(15).endless().run();

		birdAnim = Animation.of(//
				region(489, 176, 32, 16), //
				region(521, 176, 32, 16));
		birdAnim.endless().frameDuration(10).restart();

		blueBag = region(488, 199, 8, 8);
		junior = region(509, 200, 8, 8);
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex 0-based maze index
	 * @return color of maze walls
	 */
	public Color getMazeWallColor(int mazeIndex) {
		return mazeWallColors[mazeIndex];
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex 0-based maze index
	 * @return color of maze wall borders
	 */
	public Color getMazeWallBorderColor(int mazeIndex) {
		return mazeWallBorderColors[mazeIndex];
	}

	public Font getScoreFont() {
		return scoreFont;
	}
}