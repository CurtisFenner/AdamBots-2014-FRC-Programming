/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subsystems;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frcclasses.Gamepad;
import edu.wpi.first.wpilibj.templates.MainRobot;
import auxiliary.MathUtils;
import auxiliary.StopWatch;
import edu.wpi.first.wpilibj.templates.RobotActuators;
import edu.wpi.first.wpilibj.templates.RobotSensors;

/**
 *
 *
 * @author Tyler
 */
public abstract class RobotShoot {
	////VARIABLES---------------------------------------------------------------

	//// ADDED: SWITCHED THE SIGNS ON THE WIND AND UNWIND SPEED
	public static final double UNWIND_SPEED = -0.3; // TODO: may change
	public static final double WAIT_TIME = 0.75;
	public static final double WIND_SPEED = 1.0;
	public static final double MAX_REVS = 1500;
	public static final double QUICK_SHOOT_REVS = .8 * MAX_REVS;
	public static final double BACKWARDS_REV = -(MAX_REVS + 500.0);
	public static final double TENSION_TOLERANCE = 15;
	//private static double tensionTargetTicks = 1200; // Practice robot
	private static double tensionTargetTicks = 1075; // WONT CHANGE AUTON VALUE, GO TO THE AUTON CLASS
	private static double givenTensionTargetTicks = 1165;
	private static double currentSpeed;
	private static boolean inManualMode = true;
	private static boolean latch;
	private static int stage;
	private static StopWatch stopWatch = new StopWatch();
	private static StopWatch.Time stageStartTime = null;


	// unwindes the shooter until it hits the back limit switch or reaches max revolutions
	//and returns the limit value
	public static boolean zeroedBefore = false;

	public static void setTargetTicks(double ticks) {
		ticks = Math.max(500, Math.min(1400, ticks));
		givenTensionTargetTicks = ticks;
		tensionTargetTicks = ticks;
		tensionTargetTicks = Math.max(500, Math.min(1400, tensionTargetTicks));
		SmartDashboard.putNumber("shooter TICKS", ticks);
		SmartDashboard.putNumber("shooter TARGET TICKS", tensionTargetTicks);
		SmartDashboard.putNumber("shooter GIVEN TICKS", givenTensionTargetTicks);
	}

	public static void adjustTargetUp() {
		setTargetTicks(givenTensionTargetTicks + 25);
	}

	public static void adjustTargetDown() {
		setTargetTicks(givenTensionTargetTicks - 25);
	}

	//// INIT ------------------------------------------------------------------
	public static void initialize() {
		closeLatch();
		stopSpeed();
		stage = 0;
		RobotSensors.shooterWinchEncoder.start();
	}

	public static void startShoot() {
		changeStage(2);
		zeroedBefore = false;
	}

	public static void useManual() {
		inManualMode = true;
	}

	public static void useAutomatic() {
		inManualMode = false;
	}

	public static boolean isManual() {
		return inManualMode;
	}

	public static boolean isReadyToShoot() {
		return getStage() == 6 && MathUtils.inRange(getEncoder(), tensionTargetTicks, TENSION_TOLERANCE * 1.5);
	}

	//// STAGES ----------------------------------------------------------------
	// releases the latch
	public static void stage30() {

		releaseLatch();
		if (deltaTime() >= WAIT_TIME) {
			changeStage(40);
		}
	}

	public static void stage40() {
		releaseLatch();
		automatedUnwind();
		if (getAtBack()) {
			changeStage(45);
		} else if (deltaTime() >= 3) {
			changeStage(50);
		}
	}

	public static void stage45() {
		releaseLatch();
		automatedUnwind();
		if (deltaTime() >= 0.5 || getEncoder() < -200) {
			stopSpeed();
			changeStage(50);
		}
	}

	public static void stage50() {
		closeLatch();
		stopSpeed();
		if (deltaTime() >= 0.5) {
			changeStage(60);
		}
	}

	public static void stage60() {
		if (getEncoder() <= tensionTargetTicks - TENSION_TOLERANCE && RobotSensors.shooterLoadedLim.get()) {
			automatedWind();
		} else if (getEncoder() >= tensionTargetTicks + TENSION_TOLERANCE && !getAtBack()) {
			automatedUnwind();
			if (Math.abs(getEncoder() - tensionTargetTicks) < TENSION_TOLERANCE * 3) {
				multiplySpeed(1.0 / 5.0);
			}
		} else {
			stopSpeed();
		}
	}





	public static void reset() {
		resetEncoder();
	}

	// reshoot method
	// needs to be called before reshooting
	public static void shoot() {
		if (RobotPickup.pickupCanShoot() && !(getStage() >= 30 && getStage() <= 50)) {
			SmartDashboard.putBoolean("Truss: ", RobotPickup.isPickupInTrussPosition());
			changeStage(30);
			MainRobot.logData += getEncoder() + "\t" + RobotVision.getDistance() + "\n";
		}
	}

	// Automated shoot
	public static void automatedShoot() {
		SmartDashboard.putString("Current Shooter Stage", getStage() + "");
		SmartDashboard.putNumber("Shooter Timer", deltaTime());
		// shoots
		switch (getStage()) {
			case 30:
				stage30();
				break;
			case 40:
				stage40();
				break;
			case 45:
				stage45();
				break;
			case 50:
				stage50();
				break;
			case 60:
				stage60();
				break;
			default:
				break;
		}

		SmartDashboard.putNumber("shooter LATCH", latch ? 1 + MathUtils.rand(1) / 1000 : 0 + MathUtils.rand(1) / 1000);
		SmartDashboard.putNumber("shooter STAGE", getStage() + MathUtils.rand(1) / 1000);

	}

	// used for calibration
	public static void manualShoot() {
		changeStage(-99);
		setSpeed(Gamepad.secondary.getRightY());

		if (Math.abs(Gamepad.secondary.getTriggers()) > .8 && RobotPickup.pickupCanShoot()) {
			releaseLatch();
		} else {
			closeLatch();
		}
	}

	// sets speed to the unwind speed
	private static void automatedUnwind() {
		setSpeed(UNWIND_SPEED);
	}

	// sets the speed to the wind speed
	private static void automatedWind() {
		setSpeed(WIND_SPEED);
	}

	// sets the speed to 0.0
	public static void stopSpeed() {
		setSpeed(0);
	}

	// Releases the pnuematic
	public static void releaseLatch() {
		latch = true;
	}

	// latches the pnuematic
	public static void closeLatch() {
		latch = false;
	}

	// get the limit switch
	public static boolean getAtBack() {
		return !RobotSensors.shooterAtBack.get();
	}

	// Zeroes the encoder
	// check to see if the encoder is bad with this
	/*private static void zeroEncoder() {
	 if (getAtBack()) {
	 beenZeroed = false;
	 }
	 }*/
	//// UPDATE METHODS --------------------------------------------------------
	public static void update() {
		if (inManualMode) {
			manualShoot();
		} else {
			automatedShoot();
		}

		if (getEncoder() <= BACKWARDS_REV && movingBackward()) {
			stopSpeed();
		}
		if (getEncoder() >= MAX_REVS && movingForward()) {
			stopSpeed();
		}



		if (!RobotSensors.shooterLoadedLim.get() && movingForward()) {
			stopSpeed();
		}
		// sets pnuematics
		RobotActuators.latchRelease.set(latch);

		// sets motor
		RobotActuators.shooterWinch.set(getCurrentSpeed());
	}

	public static int getStage() {
		return stage;
	}

	public static void changeStage(int nextStage) {
		stageStartTime = stopWatch.now();
		stage = nextStage;
	}

	public static double deltaTime() {
		return stopWatch.deltaSeconds(stageStartTime);
	}

	public static boolean movingBackward() {
		return getCurrentSpeed() <= 0;
	}

	public static boolean movingForward() {
		return getCurrentSpeed() >= 0;
	}

	public static void multiplySpeed(double amount) {
		currentSpeed *= amount;
	}

	public static void setSpeed(double speed) {
		currentSpeed = speed;
	}

	public static double getCurrentSpeed() {
		return currentSpeed;
	}

	public static double getEncoder() {
		return RobotSensors.shooterWinchEncoder.get();
	}

	public static void resetEncoder() {
		RobotSensors.shooterWinchEncoder.reset();
	}
}
