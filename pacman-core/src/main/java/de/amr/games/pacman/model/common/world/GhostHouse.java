/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

package de.amr.games.pacman.model.common.world;

import java.util.List;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.actors.Creature;

/**
 * @author Armin Reichert
 */
public interface GhostHouse {

	/**
	 * @return tile position (top-left corner)
	 */
	Vector2i position();

	/**
	 * @return size in tiles
	 */
	Vector2i size();

	/**
	 * @return the doors
	 */
	List<Door> doors();

	default boolean hasDoorAt(Vector2i tile) {
		return doors().stream().anyMatch(door -> door.contains(tile));
	}

	/**
	 * @return the positions inside the house where ghosts can take a seat
	 */
	List<Vector2f> seatPositions();

	/**
	 * @param tile some tile
	 * @return tells if the given tile is part of this house
	 */
	default boolean contains(Vector2i tile) {
		Vector2i topLeft = position();
		Vector2i bottomRightExclusive = topLeft.plus(size());
		return tile.x() >= topLeft.x() && tile.x() < bottomRightExclusive.x() //
				&& tile.y() >= topLeft.y() && tile.y() < bottomRightExclusive.y();
	}

	/**
	 * Leads a ghost from his seat in the house to the exit.
	 * 
	 * @param ghost a ghost inside the house
	 * @return <code>true</code> if the ghost reached the house exit
	 */
	boolean leadOutside(Creature ghost);

	/**
	 * Leads a ghost from the house entry to his seat inside the house.
	 * 
	 * @param ghost          a ghost
	 * @param targetPosition target position inside the house
	 * @return
	 */
	boolean leadInside(Creature ghost, Vector2f targetPosition);
}