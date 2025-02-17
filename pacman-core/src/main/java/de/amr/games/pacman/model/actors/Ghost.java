/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.House;
import org.tinylog.Logger;

import java.util.function.Supplier;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	private final byte id;
	private GhostState state;
	private Supplier<Vector2i> fnChasingTarget = () -> null;
	private Vector2f initialPosition = Vector2f.ZERO;
	private Vector2f revivalPosition = Vector2f.ZERO;
	private Vector2i scatterTile = Vector2i.ZERO;
	private Direction initialDirection = Direction.UP;
	private int killedIndex;

	public Ghost(byte id, String name) {
		super(name);
		checkGhostID(id);
		this.id = id;
		reset();
	}

	@Override
	public String toString() {
		return "Ghost [id=" + id + ", state=" + state + ", initialPosition=" + initialPosition + ", revivalPosition="
				+ revivalPosition + ", scatterTile=" + scatterTile + ", initialDirection=" + initialDirection + ", killedIndex="
				+ killedIndex + ", newTileEntered=" + newTileEntered + ", gotReverseCommand=" + gotReverseCommand
				+ ", canTeleport=" + canTeleport + ", corneringSpeedUp=" + corneringSpeedUp + ", name=" + name() + ", visible="
				+ visible + ", position=" + position + ", velocity=" + velocity + ", acceleration=" + acceleration + "]";
	}

	/**
	 * The ghost ID. One of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST}, {@link GameModel#CYAN_GHOST},
	 * {@link GameModel#ORANGE_GHOST}.
	 */
	public byte id() {
		return id;
	}

	@Override
	public void reset() {
		super.reset();
		setKilledIndex(-1);
	}

	/**
	 * Sets the function that provides the target tile of this ghost when chasing Pac-Man.
	 * 
	 * @param fnChasingTarget function providing the chasing target tile
	 */
	public void setChasingTarget(Supplier<Vector2i> fnChasingTarget) {
		checkNotNull(fnChasingTarget);
		this.fnChasingTarget = fnChasingTarget;
	}

	public Vector2f initialPosition() {
		return initialPosition;
	}

	public void setInitialPosition(Vector2f pos) {
		checkNotNull(pos);
		this.initialPosition = pos;
	}

	public Vector2f revivalPosition() {
		return revivalPosition;
	}

	public void setRevivalPosition(Vector2f pos) {
		checkNotNull(pos);
		this.revivalPosition = pos;
	}

	public Vector2i scatterTile() {
		return scatterTile;
	}

	public void setScatterTile(Vector2i tile) {
		checkNotNull(tile);
		this.scatterTile = tile;
	}

	public Direction initialDirection() {
		return initialDirection;
	}

	public void setInitialDirection(Direction dir) {
		checkNotNull(dir);
		this.initialDirection = dir;
	}

	/**
	 * @return Index <code>(0,1,2,3)</code> telling when this ghost was killed during Pac-Man power phase. If not killed,
	 *         value is -1.
	 */
	public int killedIndex() {
		return killedIndex;
	}

	public void setKilledIndex(int index) {
		if (index < -1 || index > 3) {
			throw new IllegalArgumentException("Killed index must be one of -1, 0, 1, 2, 3, but is: " + index);
		}
		this.killedIndex = index;
	}

	@Override
	public boolean canAccessTile(Vector2i targetTile) {
		checkTileNotNull(targetTile);
		var currentTile = tile();
		for (var dir : Direction.values()) {
			if (targetTile.equals(currentTile.plus(dir.vector())) && !level.isSteeringAllowed(this, dir)) {
				Logger.trace("{} cannot access tile {} because he cannot move to %s at {}", name(), targetTile, dir,
						currentTile);
				return false;
			}
		}
		if (level.world().house().door().occupies(targetTile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(targetTile);
	}

	@Override
	public boolean canReverse() {
		return isNewTileEntered() && is(HUNTING_PAC, FRIGHTENED);
	}

	/**
	 * While "scattering", a ghost aims to "his" maze corner and circles around the wall block in that corner.
	 */
	public void scatter() {
		setTargetTile(scatterTile);
		navigateTowardsTarget();
		tryMoving();
	}

	/**
	 * While chasing Pac-Man, a ghost aims toward his chasing target (depends on ghost personality).
	 */
	public void chase() {
		setTargetTile(fnChasingTarget.get());
		navigateTowardsTarget();
		tryMoving();
	}

	/**
	 * Frightened ghosts choose a "random" direction when they enter a new tile. If the chosen direction
	 * can be taken, it is stored and taken as soon as possible.
	 * Otherwise, the remaining directions are checked in clockwise order.
	 *
	 * @see <a href="https://www.youtube.com/watch?v=eFP0_rkjwlY">YouTube: How Frightened Ghosts Decide Where to Go</a>
	 */
	public void roam() {
		if (!level.world().belongsToPortal(tile()) && (isNewTileEntered() || !moved())) {
			setWishDir(chooseFrightenedDirection());
		}
		tryMoving();
	}

	private Direction chooseFrightenedDirection() {
		Direction opposite = moveDir().opposite();
		Direction dir = pseudoRandomDirection();
		while (dir == opposite || !canAccessTile(tile().plus(dir.vector()))) {
			dir = dir.succClockwise();
		}
		return dir;
	}

	private Direction pseudoRandomDirection() {
		float rnd = Globals.randomFloat(0, 100);
		if (rnd < 16.3) return UP;
		if (rnd < 16.3 + 25.2) return RIGHT;
		if (rnd < 16.3 + 25.2 + 28.5) return DOWN;
		return LEFT;
	}

	// Here begins the state machine part

	/** The current state of this ghost. */
	public GhostState state() {
		return state;
	}

	/**
	 * @param alternatives ghost states to be checked
	 * @return <code>true</code> if this ghost is in any of the given states. If no alternatives are given, returns
	 *         <code>false</code>
	 */
	public boolean is(GhostState... alternatives) {
		return oneOf(state, alternatives);
	}

	/**
	 * Executes a single simulation step for this ghost in the current game level.
	 */
	public void update() {
		switch (state) {
		case LOCKED:
			updateStateLocked();
			break;
		case LEAVING_HOUSE:
			updateStateLeavingHouse();
			break;
		case HUNTING_PAC:
			updateStateHuntingPac();
			break;
		case FRIGHTENED:
			updateStateFrightened();
			break;
		case EATEN:
			updateStateEaten();
			break;
		case RETURNING_TO_HOUSE:
			updateStateReturningToHouse();
			break;
		case ENTERING_HOUSE:
			updateStateEnteringHouse();
			break;
		default:
			throw new IllegalArgumentException(String.format("Unknown ghost state: '%s'", state));
		}
	}

	// --- LOCKED ---

	/**
	 * In locked state, ghosts inside the house are bouncing up and down. They become blue and blink if Pac-Man gets/loses
	 * power. After that, they return to their normal color.
	 */
	public void enterStateLocked() {
		state = LOCKED;
		setPixelSpeed(0);
		selectAnimation(GhostAnimations.GHOST_NORMAL);
	}

	private void updateStateLocked() {
		if (insideHouse()) {
			var minY = initialPosition.y() - HTS;
			var maxY = initialPosition.y() + HTS;
			if (position.y() <= minY) {
				setMoveAndWishDir(DOWN);
			} else if (position.y() >= maxY) {
				setMoveAndWishDir(UP);
			}
			setPixelSpeed(GameModel.SPEED_PX_INSIDE_HOUSE);
			move();
		}
		if (killable()) {
			selectFrightenedAnimation();
		} else {
			selectAnimation(GhostAnimations.GHOST_NORMAL);
		}
	}

	private boolean killable() {
		return level.pac().powerTimer().isRunning() && killedIndex == -1;
	}

	// --- LEAVING_HOUSE ---

	/**
	 * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
	 * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
	 * and then raises up until he has passed the door on top of the house.
	 * <p>
	 * The ghost speed is slower than outside, but I do not know the exact value.
	 */
	public void enterStateLeavingHouse() {
		state = LEAVING_HOUSE;
		setPixelSpeed(GameModel.SPEED_PX_INSIDE_HOUSE);
	}

	private void updateStateLeavingHouse() {
		if (killable()) {
			selectFrightenedAnimation();
		} else {
			selectAnimation(GhostAnimations.GHOST_NORMAL);
		}
		var outOfHouse = leaveHouse(level.world().house());
		if (outOfHouse) {
			setMoveAndWishDir(LEFT);
			newTileEntered = false; // keep moving left until new tile is entered
			if (killable()) {
				enterStateFrightened();
			} else {
				killedIndex = -1; // TODO check this
				enterStateHuntingPac();
			}
		}
	}

	/**
	 * Ghosts first move sidewards to the center, then they raise until the house entry/exit position outside is reached.
	 */
	private boolean leaveHouse(House house) {
		var endPosition = house.door().entryPosition();
		if (position().y() <= endPosition.y()) {
			setPosition(endPosition); // valign at house entry
			return true;
		}
		float houseCenterX = house.center().x();
		float speed = GameModel.SPEED_PX_INSIDE_HOUSE;
		if (differsAtMost(0.5 * speed, center().x(), houseCenterX)) {
			// valign and raise
			setX(houseCenterX - HTS);
			setMoveAndWishDir(UP);
		} else {
			// move sidewards until center axis is reached
			setMoveAndWishDir(center().x() < houseCenterX ? RIGHT : LEFT);
		}
		move();
		return false;
	}

	/**
	 * Ghost moves down on the vertical axis to the center, then returns or moves sidewards to its seat.
	 */
	private boolean moveInsideHouse(House house, Vector2f targetPosition) {
		var entryPosition = house.door().entryPosition();
		if (position().almostEquals(entryPosition, velocity().length() / 2, 0) && moveDir() != Direction.DOWN) {
			// near entry, start entering
			setPosition(entryPosition);
			setMoveAndWishDir(Direction.DOWN);
		} else if (position().y() >= house.center().y()) {
			setY(house.center().y());
			if (targetPosition.x() < house.center().x()) {
				setMoveAndWishDir(LEFT);
			} else if (targetPosition.x() > house.center().x()) {
				setMoveAndWishDir(RIGHT);
			}
		}
		move();
		boolean reachedTarget = differsAtMost(1, position().x(), targetPosition.x())
				&& position().y() >= targetPosition.y();
		if (reachedTarget) {
			setPosition(targetPosition);
		}
		return reachedTarget;
	}

	// --- HUNTING_PAC ---

	/**
	 * In each game level there are 4 alternating (scattering vs. chasing) hunting phases of different duration. The first
	 * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
	 * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase
	 * is an "infinite" chasing phase.
	 * <p>
	 */
	public void enterStateHuntingPac() {
		state = HUNTING_PAC;
		selectAnimation(GhostAnimations.GHOST_NORMAL);
	}

	private void updateStateHuntingPac() {
		setRelSpeed(level.huntingSpeed(this));
		level.doGhostHuntingAction(this);
	}

	// --- FRIGHTENED ---

	/**
	 * When frightened, a ghost moves randomly through the world, at each new tile he randomly decides where to move next.
	 * Reversing the move direction is not allowed in this state either.
	 * <p>
	 * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
	 * his power. Speed is about half of the normal speed.
	 */
	public void enterStateFrightened() {
		state = FRIGHTENED;
	}

	private void updateStateFrightened() {
		var speed = level.world().isTunnel(tile()) ? level.ghostSpeedTunnel : level.ghostSpeedFrightened;
		setRelSpeed(speed);
		roam();
		selectFrightenedAnimation();
	}

	// --- EATEN ---

	/**
	 * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
	 * The value doubles for each ghost eaten using the power of the same energizer.
	 */
	public void enterStateEaten() {
		state = EATEN;
		selectAnimation(GhostAnimations.GHOST_NUMBER, killedIndex);
	}

	private void updateStateEaten() {
		// wait for timeout
	}

	// --- RETURNING_TO_HOUSE ---

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns to the
	 * ghost house to be revived. Hallelujah!
	 */
	public void enterStateReturningToHouse() {
		state = RETURNING_TO_HOUSE;
		setTargetTile(level.world().house().door().leftWing());
		selectAnimation(GhostAnimations.GHOST_EYES);
	}

	private void updateStateReturningToHouse() {
		var houseEntry = level.world().house().door().entryPosition();
		// TODO should this check for difference by speed instead of 1?
		if (position().almostEquals(houseEntry, 1, 0)) {
			setPosition(houseEntry);
			enterStateEnteringHouse();
		} else {
			setPixelSpeed(GameModel.SPEED_PX_RETURNING_TO_HOUSE);
			navigateTowardsTarget();
			tryMoving();
		}
	}

	// --- ENTERING_HOUSE ---

	/**
	 * When an eaten ghost reaches the ghost house, he enters and moves (is lead) to his revival position. Because the
	 * exact route from the house entry to the revival tile is house-specific, this logic is in the house implementation.
	 */
	public void enterStateEnteringHouse() {
		state = ENTERING_HOUSE;
		setTargetTile(null);
		setPixelSpeed(GameModel.SPEED_PX_ENTERING_HOUSE);
	}

	private void updateStateEnteringHouse() {
		boolean atRevivalPosition = moveInsideHouse(level.world().house(), revivalPosition);
		if (atRevivalPosition) {
			setMoveAndWishDir(UP);
			enterStateLocked();
		}
	}

	private void selectFrightenedAnimation() {
		var timer = level.pac().powerTimer();
		if (timer.remaining() == GameModel.PAC_POWER_FADES_TICKS
				|| timer.duration() < GameModel.PAC_POWER_FADES_TICKS && timer.tick() == 1) {
			selectAnimation(GhostAnimations.GHOST_FLASHING);
		} else if (timer.remaining() > GameModel.PAC_POWER_FADES_TICKS) {
			selectAnimation(GhostAnimations.GHOST_FRIGHTENED);
		}
	}
}