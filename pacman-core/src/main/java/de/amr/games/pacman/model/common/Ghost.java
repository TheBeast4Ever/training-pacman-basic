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
package de.amr.games.pacman.model.common;

import static de.amr.games.pacman.lib.Misc.insideRange;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	/** ID of red ghost. */
	public static final int RED_GHOST = 0;

	/** ID of pink ghost. */
	public static final int PINK_GHOST = 1;

	/** ID of cyan ghost. */
	public static final int CYAN_GHOST = 2;

	/** ID of orange ghost. */
	public static final int ORANGE_GHOST = 3;

	/** The ID (color) of the ghost, see {@link GameModel#RED_GHOST} etc. */
	public final int id;

	/** The current state of the ghost. */
	public GhostState state;

	/** The home location of the ghost. For the red ghost, this is outside of the house. */
	public V2i homeTile;

	/** The revival location inside the house. For the red ghost, this is different from the home location. */
	public V2i revivalTile;

	/** The bounty paid for this ghost. */
	public int bounty;

	/** The function computing the target tile when this ghost is in chasing mode. */
	public Supplier<V2i> fnChasingTargetTile;

	/** Individual food counter, used to determine when the ghost can leave the house. */
	public int dotCounter;

	/** Global number of "dots" Pac-Man has to eat until ghost gets unlocked. */
	public int globalDotLimit;

	/** Individual number of "dots" Pac-Man has to eat until ghost gets unlocked. */
	public int privateDotLimit;

	/** "Cruise Elroy" mode. Values: 0 (off), 1, -1, 2, -2 (negative means disabled). */
	public int elroy;

	public Ghost(int id, String name) {
		super(name);
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("[Ghost %s: state=%s, position=%s, tile=%s, offset=%s, velocity=%s, dir=%s, wishDir=%s]", name,
				state, position, tile(), offset(), velocity, moveDir, wishDir);
	}

	public boolean is(GhostState ghostState) {
		return state == ghostState;
	}

	@Override
	public boolean canAccessTile(V2i tile) {
		if (world.ghostHouse().doorTileLeft.equals(tile) || world.ghostHouse().doorTileRight.equals(tile)) {
			return is(GhostState.ENTERING_HOUSE) || is(GhostState.LEAVING_HOUSE);
		}
		if (world.isOneWayDown(tile)) {
			if (offset().y != 0) {
				return true; // allow if already on the way up
			}
			return !is(GhostState.HUNTING_PAC);
		}
		return super.canAccessTile(tile);
	}

	/**
	 * @return {@code true} if the ghost is near the ghosthouse door.
	 */
	public boolean atGhostHouseDoor(ArcadeGhostHouse house) {
		return tile().equals(house.doorTileLeft.minus(0, 1)) && insideRange(offset().x, HTS, 2);
	}

	/**
	 * Lets the ghost head for its current chasing target.
	 */
	public void chase() {
		headForTile(fnChasingTargetTile.get());
		tryMoving();
	}

	/**
	 * Lets the ghost head for its scatter tile.
	 */
	public void scatter() {
		headForTile(world.ghostScatterTile(id));
		tryMoving();
	}

	/**
	 * Lets the ghost choose some random direction whenever it enters a new tile.
	 * 
	 * TODO: this is not 100% what the Pac-Man dossier says.
	 */
	public void roam() {
		if (newTileEntered) {
			wishDir = Direction.shuffled().stream()
					.filter(dir -> dir != moveDir.opposite() && canAccessTile(tile().plus(dir.vec))).findAny().orElse(wishDir);
		}
		tryMoving();
	}

	/**
	 * Lets the ghost return back to the ghost house.
	 * 
	 * @param house the ghost house
	 * @return {@code true} if the ghost has reached the house
	 */
	public boolean returnHome(ArcadeGhostHouse house) {
		if (atGhostHouseDoor(house) && moveDir != Direction.DOWN) {
			// house reached, start entering
			setOffset(HTS, 0);
			setMoveDir(Direction.DOWN);
			setWishDir(Direction.DOWN);
			forcedOnTrack = false;
			targetTile = revivalTile;
			state = GhostState.ENTERING_HOUSE;
			return true;
		}
		headForTile(targetTile);
		tryMoving();
		return false;
	}

	/**
	 * Lets the ghost enter the house and moving to its revival position.
	 * 
	 * @param house the ghost house
	 * @return {@code true} if the ghost has reached its revival position
	 */
	public boolean enterHouse(ArcadeGhostHouse house) {
		V2i tile = tile();
		V2d offset = offset();
		if (tile.equals(targetTile) && offset.y >= 0) {
			// Target position inside house reached. Turn around and start leaving house.
			Direction backwards = moveDir.opposite();
			setMoveDir(backwards);
			setWishDir(backwards);
			state = GhostState.LEAVING_HOUSE;
			return true;
		}
		if (tile.equals(house.seatTileMiddle) && offset.y >= 0) {
			// Center seat reached, move towards left or right seat.
			if (targetTile.x < house.seatTileMiddle.x) {
				setMoveDir(Direction.LEFT);
				setWishDir(Direction.LEFT);
			} else if (targetTile.x > house.seatTileMiddle.x) {
				setMoveDir(Direction.RIGHT);
				setWishDir(Direction.RIGHT);
			}
		}
		move();
		return false;
	}

	/**
	 * Lets the ghost leave the house from its home position towards the middle of the house and then upwards towards the
	 * house door.
	 * 
	 * @param house the ghost house
	 * @return {@code true} if the ghost has left the house
	 */
	public boolean leaveHouse(ArcadeGhostHouse house) {
		V2i tile = tile();
		V2d offset = offset();
		// House left? Resume hunting.
		if (tile.equals(house.doorTileLeft.minus(0, 1)) && insideRange(offset.y, 0, 1)) {
			setOffset(HTS, 0);
			// TODO not quite working:
			if (id == CYAN_GHOST) {
				setBothDirs(Direction.RIGHT);
			} else {
				setBothDirs(Direction.LEFT);
			}
			forcedOnTrack = true;
			state = GhostState.HUNTING_PAC;
			return true;
		}
		int centerX = t(house.seatTileMiddle.x) + HTS;
		int groundY = t(house.seatTileMiddle.y) + HTS;
		if (insideRange(position.x, centerX, 1)) {
			setOffset(HTS, offset.y);
			setBothDirs(Direction.UP);
		} else if (position.y < groundY) {
			setBothDirs(Direction.DOWN);
		} else {
			setBothDirs(position.x < centerX ? Direction.RIGHT : Direction.LEFT);
		}
		move();
		return false;
	}

	/**
	 * Lets the ghost bounce at its home position inside the house.
	 * 
	 * @return {@code true}
	 */
	public boolean bounce(ArcadeGhostHouse house) {
		double zeroLevel = t(house.seatTileMiddle.y);
		if (!insideRange(position.y, zeroLevel, HTS)) {
			setBothDirs(moveDir.opposite());
		}
		move();
		return true;
	}
}