package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.GhostState.DEAD;
import static de.amr.games.pacman.model.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class PacManGameRendering implements RenderingWithAnimatedSprites {

	private final GraphicsContext g;
	private final Image spritesheet = new Image("/pacman/graphics/sprites.png", false);

	private final Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);
	private final Image mazeEmpty = new Image("/pacman/graphics/maze_empty.png", false);

	private final Font scoreFont;

	private final Rectangle2D[] symbols;
	private final Map<Integer, Rectangle2D> numbers;

	private final Map<Direction, Animation<Rectangle2D>> pacMunching;
	private final Animation<Rectangle2D> pacCollapsing;
	private final List<EnumMap<Direction, Animation<Rectangle2D>>> ghostsKicking;
	private final EnumMap<Direction, Animation<Rectangle2D>> ghostEyes;
	private final Animation<Rectangle2D> ghostBlue;
	private final Animation<Rectangle2D> ghostFlashing;

	private final Animation<Image> mazeFlashing;
	private final Animation<Boolean> energizerBlinking;

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	private Direction ensureNotNull(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	private Rectangle2D s(int col, int row) {
		return new Rectangle2D(col, row, 1, 1);
	}

	private Rectangle2D r(double x, double y, double width, double height) {
		return new Rectangle2D(x, y, width, height);
	}

	private void drawRegion(Creature guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(spritesheet, region.getMinX() * 16, region.getMinY() * 16, region.getWidth() * 16,
					region.getHeight() * 16, guy.position.x - 4, guy.position.y - 4, region.getWidth() * 16,
					region.getHeight() * 16);
		}
	}

	public PacManGameRendering(GraphicsContext g) {
		this.g = g;

		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);

		symbols = new Rectangle2D[] { s(2, 3), s(3, 3), s(4, 3), s(5, 3), s(6, 3), s(7, 3), s(8, 3), s(9, 3) };

		//@formatter:off
		numbers = new HashMap<>();
		numbers.put(200,  r(0, 8, 1, 1));
		numbers.put(400,  r(1, 8, 1, 1));
		numbers.put(800,  r(2, 8, 1, 1));
		numbers.put(1600, r(3, 8, 1, 1));
		
		numbers.put(100,  r(0, 9, 1, 1));
		numbers.put(300,  r(1, 9, 1, 1));
		numbers.put(500,  r(2, 9, 1, 1));
		numbers.put(700,  r(3, 9, 1, 1));
		
		numbers.put(1000, r(4, 9, 2, 1)); // left-aligned 
		numbers.put(2000, r(3, 10, 3, 1));
		numbers.put(3000, r(3, 11, 3, 1));
		numbers.put(5000, r(3, 12, 3, 1));
		//@formatter:on

		// Animations

		Image mazeEmptyBright = RenderingWithAnimatedSprites.exchangeColors(mazeEmpty,
				Map.of(Color.rgb(33, 33, 255), Color.WHITE));
		mazeFlashing = Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15);

		energizerBlinking = Animation.pulse().frameDuration(15);

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<Rectangle2D> animation = Animation.of(s(2, 0), s(1, index(dir)), s(0, index(dir)), s(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacMunching.put(dir, animation);
		}

		pacCollapsing = Animation.of(s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0), s(10, 0), s(11, 0),
				s(12, 0), s(13, 0));
		pacCollapsing.frameDuration(8);

		ghostsKicking = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<Rectangle2D> animation = Animation.of(s(2 * index(dir), 4 + id), s(2 * index(dir) + 1, 4 + id));
				animation.frameDuration(10).endless();
				walkingTo.put(dir, animation);
			}
			ghostsKicking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, Animation.ofSingle(s(8 + index(dir), 5)));
		}

		ghostBlue = Animation.of(s(8, 4), s(9, 4));
		ghostBlue.frameDuration(20).endless();

		ghostFlashing = Animation.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4));
		ghostFlashing.frameDuration(5).endless();
	}

	@Override
	public Image spritesheet() {
		return spritesheet;
	}

	@Override
	public GraphicsContext gc() {
		return g;
	}

	@Override
	public Font getScoreFont() {
		return scoreFont;
	}

	@Override
	public void signalReadyState(PacManGameModel game) {
		g.setFont(scoreFont);
		g.setFill(Color.YELLOW);
		g.fillText("READY", t(11), t(21));
	}

	@Override
	public void signalGameOverState(PacManGameModel game) {
		g.setFont(scoreFont);
		g.setFill(Color.RED);
		g.fillText("GAME", t(9), t(21));
		g.fillText("OVER", t(15), t(21));
	}

	@Override
	public void hideTile(V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	@Override
	public void drawMaze(int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			g.drawImage(mazeFull, x, y);
		}
	}

	@Override
	public void drawScore(PacManGameModel game) {
		g.setFont(scoreFont);
		g.translate(0, 1);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGHSCORE", t(15), t(1));
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
			g.setFill(Color.YELLOW);
			g.fillText(String.format("%08d", game.score), t(1), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setFill(Color.YELLOW);
			g.fillText(String.format("%08d", game.highscorePoints), t(15), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		}
		g.translate(0, -2);
	}

	@Override
	public void drawLevelCounter(PacManGameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Rectangle2D region = symbols[game.levelSymbols.get(level - 1)];
			g.drawImage(spritesheet, region.getMinX() * 16, region.getMinY() * 16, 16, 16, x, y, 16, 16);
			x -= t(2);
		}
	}

	@Override
	public void drawLivesCounter(PacManGameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		Rectangle2D region = s(8, 1);
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(spritesheet, region.getMinX() * 16, region.getMinY() * 16, 16, 16, x + t(2 * i), y, 16, 16);
		}
	}

	@Override
	public void drawFoodTiles(Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(this::hideTile);
	}

	@Override
	public void drawEnergizerTiles(Stream<V2i> energizerTiles) {
		if (energizerBlinking.animate()) {
			energizerTiles.forEach(this::hideTile);
		}
	}

	@Override
	public void drawPac(Pac pac, PacManGameModel game) {
		drawRegion(pac, pacSprite(pac, game));
	}

	@Override
	public void drawGhost(Ghost ghost, PacManGameModel game) {
		drawRegion(ghost, ghostSprite(ghost, game));
	}

	@Override
	public void drawBonus(Bonus bonus, PacManGameModel game) {
		drawRegion(bonus, bonusSprite(bonus, game));
	}

	@Override
	public Rectangle2D bonusSprite(Bonus bonus, PacManGameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return symbols[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return numbers.get(bonus.points);
		}
		return null;
	}

	@Override
	public Rectangle2D pacSprite(Pac pac, PacManGameModel game) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunchingToDir(pac.dir).frame();
		}
		if (pac.speed == 0) {
			return pacMunchingToDir(pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunchingToDir(pac.dir).frame(1);
		}
		return pacMunchingToDir(pac.dir).animate();
	}

	@Override
	public Rectangle2D ghostSprite(Ghost ghost, PacManGameModel game) {
		if (ghost.bounty > 0) {
			return numbers.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		return ghostKickingToDir(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	@Override
	public Animation<Rectangle2D> pacMunchingToDir(Direction dir) {
		return pacMunching.get(ensureNotNull(dir));
	}

	@Override
	public Animation<Rectangle2D> pacDying() {
		return pacCollapsing;
	}

	@Override
	public Animation<Rectangle2D> ghostKickingToDir(Ghost ghost, Direction dir) {
		return ghostsKicking.get(ghost.id).get(ensureNotNull(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return ghostBlue;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashing;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return ghostEyes.get(ensureNotNull(dir));
	}

	@Override
	public Animation<Image> mazeFlashing(int mazeNumber) {
		return mazeFlashing;
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}
}