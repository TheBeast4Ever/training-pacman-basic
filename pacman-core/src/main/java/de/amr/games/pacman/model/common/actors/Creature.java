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

import static de.amr.games.pacman.lib.steering.Direction.DOWN;
import static de.amr.games.pacman.lib.steering.Direction.LEFT;
import static de.amr.games.pacman.lib.steering.Direction.RIGHT;
import static de.amr.games.pacman.lib.steering.Direction.UP;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.tileAt;

import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameModel;

/**
 * Base class for all creatures which can move through the world.
 * 
 * @author Armin Reichert
 */
public class Creature extends Entity {

	protected static final Logger LOGGER = LogManager.getFormatterLogger();

	protected static final String MSG_GAME_NULL = "Game must not be null";
	protected static final String MSG_TILE_NULL = "Tile must not be null";
	protected static final String MSG_DIR_NULL = "Direction must not be null";

	protected static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	private final String name;
	private Direction moveDir;
	private Direction wishDir;
	private Vector2i targetTile;
	private boolean newTileEntered;
	private boolean stuck;
	protected boolean shouldReverse;
	protected boolean canTeleport;

	protected Creature(String name) {
		this.name = (name != null) ? name : "Creature@%d".formatted(hashCode());
	}

	public void reset() {
		visible = false;
		position = Vector2f.ZERO;
		velocity = Vector2f.ZERO;
		acceleration = Vector2f.ZERO;
		moveDir = RIGHT;
		wishDir = RIGHT;
		targetTile = null;
		newTileEntered = true;
		shouldReverse = false;
		stuck = false;
		canTeleport = true;
	}

	@Override
	public String toString() {
		return "%s: pos=%s, tile=%s, velocity=%s, speed=%.2f, moveDir=%s, wishDir=%s".formatted(name, position, tile(),
				velocity, velocity.length(), moveDir(), wishDir());
	}

	/** Readable name, for display and logging purposes. */
	public String name() {
		return name;
	}

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean isNewTileEntered() {
		return newTileEntered;
	}

	public void setNewTileEntered(boolean newTileEntered) {
		this.newTileEntered = newTileEntered;
	}

	/** Tells if the creature got stuck. */
	public boolean isStuck() {
		return stuck;
	}

	/**
	 * Sets the tile this creature tries to reach. May be an unreachable tile or <code>null</code>.
	 * 
	 * @param tile some tile or <code>null</code>
	 */
	public void setTargetTile(Vector2i tile) {
		targetTile = tile;
	}

	/** (Optional) target tile. Can be inaccessible or outside of the world. */
	public Optional<Vector2i> targetTile() {
		return Optional.ofNullable(targetTile);
	}

	public void placeAtTile(int tx, int ty, float ox, float oy) {
		var prevTile = tile();
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = !tile().equals(prevTile);
	}

	public void placeAtTile(Vector2i tile, float ox, float oy) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	public void placeAtTile(Vector2i tile) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		placeAtTile(tile.x(), tile.y(), 0, 0);
	}

	/**
	 * @param tile some tile inside or outside of the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(Vector2i tile, GameModel game) {
		Objects.requireNonNull(tile, MSG_TILE_NULL);
		Objects.requireNonNull(game, MSG_GAME_NULL);
		var world = game.level().world();
		if (world.insideMap(tile)) {
			return !world.isWall(tile) && !world.ghostHouse().isDoorTile(tile);
		}
		return world.belongsToPortal(tile);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		Objects.requireNonNull(dir, MSG_DIR_NULL);
		if (moveDir != dir) {
			moveDir = dir;
			LOGGER.trace("%-6s: New moveDir: %s. %s", name, moveDir, this);
			velocity = moveDir.vec.toFloatVec().scaled(velocity.length());
		}
	}

	/** The current move direction. */
	public Direction moveDir() {
		return moveDir;
	}

	public void setWishDir(Direction dir) {
		Objects.requireNonNull(dir, MSG_DIR_NULL);
		if (wishDir != dir) {
			wishDir = dir;
			LOGGER.trace("%-6s: New wishDir: %s. %s", name, wishDir, this);
		}
	}

	/** The wish direction. Will be taken as soon as possible. */
	public Direction wishDir() {
		return wishDir;
	}

	public void setMoveAndWishDir(Direction dir) {
		setWishDir(dir);
		setMoveDir(dir);
	}

	public void reverseDirectionASAP() {
		shouldReverse = true;
		LOGGER.trace("%s (moveDir=%s, wishDir=%s) got signal to reverse direction", name, moveDir, wishDir);
	}

	/**
	 * @param game game model
	 * @return if the creature can reverse its direction
	 */
	protected boolean canReverse(GameModel game) {
		return newTileEntered;
	}

	/**
	 * Sets the speed as a fraction of the game base speed (1.25 pixels/sec).
	 * 
	 * @param fraction fraction of base speed
	 */
	public void setRelSpeed(float fraction) {
		if (fraction < 0) {
			throw new IllegalArgumentException("Negative speed fraction: " + fraction);
		}
		setAbsSpeed(fraction * GameModel.SPEED_100_PERCENT_PX);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param speed speed in pixels per tick
	 */
	public void setAbsSpeed(float speed) {
		if (speed < 0) {
			throw new IllegalArgumentException("Negative speed: " + speed);
		}
		velocity = speed == 0 ? Vector2f.ZERO : moveDir.vec.toFloatVec().scaled(speed);
	}

	/**
	 * Sets the new wish direction for reaching the target tile.
	 * 
	 * @param game the game model
	 */
	public void navigateTowardsTarget(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		if (!newTileEntered && !stuck) {
			return; // we don't need no navigation, dim dit diddit diddit dim dit diddit diddit...
		}
		if (targetTile == null) {
			return;
		}
		if (game.level().world().belongsToPortal(tile())) {
			return; // inside portal, no navigation happens
		}
		computeTargetDirection(game).ifPresent(this::setWishDir);
	}

	/*
	 * For each neighbor tile, compute distance to target tile, select direction with smallest distance.
	 */
	private Optional<Direction> computeTargetDirection(GameModel game) {
		Direction targetDir = null;
		Vector2i currentTile = tile();
		float minDistance = Float.MAX_VALUE;
		for (var dir : DIRECTION_PRIORITY) {
			if (dir == moveDir.opposite()) {
				continue; // reversing the move direction is not allowed
			}
			var neighborTile = currentTile.plus(dir.vec);
			if (canAccessTile(neighborTile, game)) {
				float distance = neighborTile.euclideanDistance(targetTile);
				if (distance < minDistance) {
					minDistance = distance;
					targetDir = dir;
				}
			}
		}
		return Optional.ofNullable(targetDir);
	}

	private MoveResult tryTeleport(GameModel game) {
		MoveResult mr = MoveResult.notMoved("No teleport");
		if (canTeleport) {
			for (var portal : game.level().world().portals()) {
				mr = portal.teleport(this);
				if (mr.teleported()) {
					newTileEntered = true;
					break;
				}
			}
		}
		return mr;
	}

	/**
	 * Move through the world.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 */
	public void tryMoving(GameModel game) {
		Objects.requireNonNull(game, MSG_GAME_NULL);
		Vector2i tileBeforeMove = tile();
		MoveResult mr = tryTeleport(game);
		if (mr.teleported()) {
			return;
		}
		if (shouldReverse && canReverse(game)) {
			setWishDir(moveDir.opposite());
			shouldReverse = false;
		}
		mr = tryMoving(wishDir, game);
		if (mr.moved()) {
			setMoveDir(wishDir);
		} else {
			mr = tryMoving(moveDir, game);
		}
		stuck = !mr.moved();
		newTileEntered = !tileBeforeMove.equals(tile());
		if (mr.moved()) {
			LOGGER.trace("%-6s: %s %s", name, mr.message(), this);
		}
	}

	private MoveResult tryMoving(Direction dir, GameModel game) {
		float speed = velocity.length();
		Vector2f dirVector = dir.vec.toFloatVec();
		if (speed <= 1.0) {
			return trySmallMove(dir, game, dirVector, speed);
		}
		// split "large" move such that turns are not missed
		int steps = 4;
		float pixels = speed / steps;
		for (int i = 0; i < steps - 1; ++i) {
			var result = trySmallMove(dir, game, dirVector, pixels);
			if (!result.moved()) {
				return result;
			}
		}
		return trySmallMove(dir, game, dirVector, pixels);
	}

	private MoveResult trySmallMove(Direction dir, GameModel game, Vector2f dirVector, float pixels) {
		var turn = !dir.sameOrientation(moveDir);
		var newVelocity = dirVector.scaled(pixels);
		var touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);

		if (!canAccessTile(tileAt(touchPosition), game)) {
			if (!turn) {
				placeAtTile(tile()); // adjust exactly over tile if blocked
			}
			return MoveResult.notMoved("Not moved: Cannot access tile %s", tileAt(touchPosition));
		}

		if (turn && !atTurnPositionTo(dir)) {
			return MoveResult.notMoved("Wants to move %s but not at turn position", dir);
		}

		setVelocity(newVelocity);
		move();
		return MoveResult.moved("Moved %5s %.2f pixels", dir, pixels);
	}

	protected boolean atTurnPositionTo(Direction dir) {
		var offset = dir.isHorizontal() ? offset().y() : offset().x();
		return Math.abs(offset) <= 1;
	}
}