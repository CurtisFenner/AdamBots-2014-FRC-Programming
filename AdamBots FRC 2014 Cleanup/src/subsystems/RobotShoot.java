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
	private static Timer timer;
	private static double currentSpeed;
	private static boolean inManualMode = true;
	private static boolean latch;
	private static int stage;
	private static int returnStage = 0;
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
		latch();
		timer = new Timer();
		stopSpeed();
		stage = 0;
		RobotSensors.shooterWinchEncoder.start();
	}

	public static void startShoot() {
		stage = 2;
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
		return stage == 6 && MathUtils.inRange(getEncoder(), tensionTargetTicks, TENSION_TOLERANCE * 1.5);
	}

	//// STAGES ----------------------------------------------------------------
	// releases the latch
	public static void releaseBall() {
		if (RobotPickup.isPickupInShootPosition() || RobotPickup.isPickupInTrussPosition()) {
			releaseLatch();
			stage = 2;
		} else {
			stage = returnStage;
		}
	}

	// Is Shown in our diagram as the shooter head moving forward
	// Nothing that is controlled is happening nows
	public static void ballInMotion() {

		timer.stop();
		timer.reset();
		timer.start();
		releaseLatch();

		stage = 3;
	}

	// waiting the 0.5 seconds before unwinding the shooter motor
	public static void waitToUnwind() {
		double time = timer.get();
		if (time >= WAIT_TIME) {
			timer.stop();
			timer.reset();
			stage = 4;
		}
	}

	public static void unwind() {
		releaseLatch();
		if (getAtBack() && timer.get() <= .05) {
			if (timer.get() == 0) {
				SmartDashboard.putNumber("back limit enc value: ", getEncoder());
				resetEncoder();
				timer.start();
			}
			zeroedBefore = true;
			System.out.println("RobotShoot.java\tHIT BACK");
		}

		automatedUnwind();
		SmartDashboard.putNumber("STAGE 4 TIMER", timer.get());
		if ((zeroedBefore && (timer.get() > 0.5 || getEncoder() < -200)) || timer.get() > 3) {
			stopSpeed();
			System.out.println("RobotShoot.java\tSTOP:");
			System.out.println("RobotShoot.java\tTimer " + timer.get());
			System.out.println("RobotShoot.java\tEncoder " + getEncoder());
			timer.stop();
			timer.reset();
			stage = 5;
		}
	}

	// relatches the shooter
	public static void latchShooter() {
		if (timer.get() == 0.0) {
			timer.start();
		}
		latch();
		//// TODO: CHANGE THE TIME ON THIS LATER ON
		//// CHANGED: Latch time from 1.0
		if (timer.get() >= 0.5) {
			timer.stop();
			timer.reset();
			stage = 6;
		}
		stopSpeed();
	}

	// rewinds the shooter
	public static void rewindShooter() {
		setSpeed( 0 );
		if (getEncoder() <= tensionTargetTicks - TENSION_TOLERANCE && RobotSensors.shooterLoadedLim.get()) {
			automatedWind();
			return;
		}

		if (getEncoder() >= tensionTargetTicks + TENSION_TOLERANCE && !getAtBack()) {
			automatedUnwind();
			if (Math.abs(getEncoder() - tensionTargetTicks) < TENSION_TOLERANCE * 3) {
				multiplySpeed(1.0 / 5.0);
			}
			return;
		}
		stopSpeed();
	}

	public static void reset() {
		resetEncoder();
		timer.stop();
		timer.reset();
	}

	// reshoot method
	// needs to be called before reshooting
	public static void shoot() {
		//// CHANGED: ADDED IN TO MAKE SURE WE DONT FIRE IN STAGES 2,3,4,5
		if (RobotPickup.pickupCanShoot() && !(stage >= 2 && stage <= 5)) {
			SmartDashboard.putBoolean("Truss: ", RobotPickup.isPickupInTrussPosition());
			if (stage != 1) {
				returnStage = stage;
				MainRobot.logData += getEncoder() + "\t" + RobotVision.getDistance() + "\n";
			}
			stage = 1;
			timer.stop();
			timer.reset();
		}
	}

	// Automated shoot
	public static void automatedShoot() {
		SmartDashboard.putString("Current Shooter Stage", stage + "");
		SmartDashboard.putNumber("Shooter Timer", timer.get());
		// shoots
		switch (stage) {
			case 1:
				releaseBall();
				break;
			case 2:
				ballInMotion();
				break;
			case 3:
				waitToUnwind();
				break;
			case 4:
				unwind();
				break;
			case 5:
				latchShooter();
				break;
			case 6:
				rewindShooter();
				break;
			case 99:
			case -99:
				break;
			default:
				//System.out.println("You have stage Fright");
				//System.out.println("Stage Issue: " + stage);
				break;
		}

		SmartDashboard.putNumber("latch", latch ? 1 + MathUtils.rand(1) / 1000 : 0 + MathUtils.rand(1) / 1000);
		SmartDashboard.putNumber("stage SHOOTER", stage + MathUtils.rand(1) / 1000);


		if (stage != 1) {
			returnStage = stage;
		}

	}

	// used for calibration
	public static void manualShoot() {
		stage = -99;
		setSpeed( Gamepad.secondary.getRightY() );

		if (Math.abs(Gamepad.secondary.getTriggers()) > .8 && RobotPickup.pickupCanShoot()) {
			releaseLatch();
		} else {
			latch();
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
	public static void latch() {
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
