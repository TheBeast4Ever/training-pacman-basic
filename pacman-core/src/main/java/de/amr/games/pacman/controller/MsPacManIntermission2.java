/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission2 {

	public static final byte STATE_FLAP = 0;
	public static final byte STATE_CHASING = 1;

	public static final int UPPER_LANE_Y = TS * 12;
	public static final int MIDDLE_LANE_Y = TS * 18;
	public static final int LOWER_LANE_Y = TS * 24;

	public final Pac pacMan;
	public final Pac msPac;

	private byte state;
	private final TickTimer stateTimer = new TickTimer("MsPacManIntermission2");

	public void changeState(byte state, long ticks) {
		this.state = state;
		stateTimer.reset(ticks);
		stateTimer.start();
	}

	public MsPacManIntermission2() {
		pacMan = new Pac("Pac-Man");
		msPac = new Pac("Ms. Pac-Man");
	}

	public void tick() {
		switch (state) {
		case STATE_FLAP:
			updateStateFlap();
			break;
		case STATE_CHASING:
			updateStateChasing();
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);

		}
		stateTimer.advance();
	}

	private void updateStateFlap() {
		if (stateTimer.hasExpired()) {
			GameController.it().publishGameEvent(GameEventType.INTERMISSION_STARTED);
			enterStateChasing();
		}
	}

	private void enterStateChasing() {
		pacMan.setMoveDir(Direction.RIGHT);
		pacMan.selectAnimation(PacAnimations.HUSBAND_MUNCHING);
		pacMan.startAnimation();
		msPac.setMoveDir(Direction.RIGHT);
		msPac.selectAnimation(PacAnimations.MUNCHING);
		msPac.startAnimation();

		changeState(STATE_CHASING, TickTimer.INDEFINITE);
	}

	private void updateStateChasing() {
		if (stateTimer.atSecond(4.5)) {
			pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPixelSpeed(2.0f);
			pacMan.show();
			msPac.setPosition(TS * (-8), UPPER_LANE_Y);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.setPixelSpeed(2.0f);
			msPac.show();
		} else if (stateTimer.atSecond(9)) {
			pacMan.setPosition(TS * 36, LOWER_LANE_Y);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.setPixelSpeed(2.0f);
			msPac.setPosition(TS * 30, LOWER_LANE_Y);
			msPac.setMoveDir(Direction.LEFT);
			msPac.setPixelSpeed(2.0f);
		} else if (stateTimer.atSecond(13.5)) {
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPixelSpeed(2.0f);
			msPac.setPosition(TS * (-8), MIDDLE_LANE_Y);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.setPixelSpeed(2.0f);
			pacMan.setPosition(TS * (-2), MIDDLE_LANE_Y);
		} else if (stateTimer.atSecond(17.5)) {
			pacMan.setPosition(TS * 42, UPPER_LANE_Y);
			pacMan.setMoveDir(Direction.LEFT);
			pacMan.setPixelSpeed(4.0f);
			msPac.setPosition(TS * 30, UPPER_LANE_Y);
			msPac.setMoveDir(Direction.LEFT);
			msPac.setPixelSpeed(4.0f);
		} else if (stateTimer.atSecond(18.5)) {
			pacMan.setPosition(TS * (-2), LOWER_LANE_Y);
			pacMan.setMoveDir(Direction.RIGHT);
			pacMan.setPixelSpeed(4.0f);
			msPac.setPosition(TS * (-14), LOWER_LANE_Y);
			msPac.setMoveDir(Direction.RIGHT);
			msPac.setPixelSpeed(4.0f);
		} else if (stateTimer.atSecond(23)) {
			GameController.it().terminateCurrentState();
			return;
		}
		pacMan.move();
		msPac.move();
	}
}