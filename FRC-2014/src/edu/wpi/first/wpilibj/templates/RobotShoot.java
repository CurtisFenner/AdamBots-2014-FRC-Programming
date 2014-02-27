/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * TODO: RUN AUTOMATED SHOOT BY ENUMERATION
 *
 * @author Tyler
 */
public class RobotShoot {
	////VARIABLES---------------------------------------------------------------

	//// ADDED: SWITCHED THE SIGNS ON THE WIND AND UNWIND SPEED
	public static final double UNWIND_SPEED = -0.75; // TODO: may change
	public static final double WAIT_TIME = 0.75;
	public static final double WIND_SPEED = 1.0;
	public static final double MAX_REVS = 1700;
	public static final double QUICK_SHOOT_REVS = .8 * MAX_REVS;
	public static final double BACKWARDS_REV = -(MAX_REVS + 500.0);
	public static final double TENSION_TOLERANCE = 75;
	private static double tensionTargetTicks = 1200;
	private static Timer timer;
	public static Timer gameTime;
	private static double updatedSpeed;
	private static boolean inManualMode = true;
	private static boolean latch;
	private static String currentStage;
	private static int stage;
	private static int returnStage = 0;
	public static double voltage;
	public static double current;

	public static void setTargetTicks(double ticks) {
		ticks = Math.max(500, Math.min(1400, ticks));
		tensionTargetTicks = ticks;
	}

	//// INIT ------------------------------------------------------------------
	public static void initialize() {
		latch();
		timer = new Timer();
		updatedSpeed = 0.0;
		currentStage = "";
		stage = 0;
		gameTime = new Timer();
		RobotSensors.shooterWinchEncoder.start();
	}

	public static void startShoot() {
		stage = 2;
		//stage = 1;
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
		return stage == 6 && Math.abs(getEncoder() - tensionTargetTicks) < TENSION_TOLERANCE * 1.5;
	}

	//// STAGES ----------------------------------------------------------------
	// releases the latch
	public static void releaseBall() {
		currentStage = "1";
		if (RobotPickup.isPickupInShootPosition()) {
			releaseLatch();
			stage = 2;
		} else {
			stage = returnStage;
		}
	}

	// Is Shown in our diagram as the shooter head moving forward
	// Nothing that is controlled is happening now
	public static void ballInMotion() {
		currentStage = "2";

		timer.stop();
		timer.reset();
		timer.start();
		releaseLatch();

		stage = 3;
	}

	// waiting the 0.5 seconds before unwinding the shooter motor
	public static void waitToUnwind() {
		currentStage = "3";
		double time = timer.get();
		if (time >= WAIT_TIME) {
			timer.stop();
			timer.reset();
			stage = 4;
		}
	}

	// unwindes the shooter until it hits the back limit switch or reaches max revolutions
	//and returns the limit value
	public static void unwind() {
		currentStage = "4";
		releaseLatch();
		if (getAtBack() && timer.get() == 0) {
			timer.start();
			RobotSensors.shooterWinchEncoder.reset();
		}

		automatedUnwind();

		if ((timer.get() > 0.1 && getEncoder() < -200) || timer.get() > 0.5) {
			updatedSpeed = 0.0;
			System.out.println("Timer " + timer.get());
			timer.stop();
			timer.reset();
			stage = 5;
		}
	}

	// relatches the shooter
	public static void latchShooter() {
		currentStage = "5";
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
	}

	// rewinds the shooter
	public static void rewindShooter() {
		currentStage = "6";
		//// TODO: TAKE OUT THE OR TRUE WHEN IT IS WORKING
		if (getEncoder() <= tensionTargetTicks - TENSION_TOLERANCE && (true || !RobotSensors.shooterLoadedLim.get())) {
			automatedWind();
			return;
		}

		if (getEncoder() >= tensionTargetTicks + TENSION_TOLERANCE && !getAtBack()) {
			automatedUnwind();
			return;
		}
		updatedSpeed = 0.0;
	}
	
	public static void reset() {
		RobotSensors.shooterWinchEncoder.reset();
		gameTime.stop();
		gameTime.reset();
		timer.stop();
		timer.reset();
	}

	// reshoot method
	// needs to be called before reshooting
	public static void shoot() {
		//// CHANGED: ADDED IN TO MAKE SURE WE DONT FIRE IN STAGES 2,3,4,5
		if (RobotPickup.isPickupInShootPosition() && !(stage >= 2 && stage <= 5)) {
			if (stage != 1) {
				returnStage = stage;
			}
			stage = 1;
			timer.stop();
			timer.reset();
		}
	}

	// TODO: RUN AUTOMATED SHOOT BY ENUMERATION
	// Automated shoot
	public static void automatedShoot() {
		SmartDashboard.putString("Current Shooter Stage", currentStage);
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

		SmartDashboard.putNumber("latch", latch ? 1 + MathUtils.rand(1)/1000 : 0 + MathUtils.rand(1)/1000);
		SmartDashboard.putNumber("stage SHOOTER", stage + MathUtils.rand(1)/1000);
		//System.out.println("-->stage: " + stage);
		
		if (stage != 1) {
			returnStage = stage;
		}

	}

	// used for calibration
	public static void manualShoot() {
		stage = -99;
		updatedSpeed = Gamepad.secondary.getRightY();
		//SmartDashboard.putBoolean("is the shooterLoadedLim", RobotSensors.shooterLoadedLim.get());
		// TODO: TAKE OUT THE OR TRUE FOR THE REAL ROBOT WHEN THE SWITCH IS FIXED
		/*if ((RobotSensors.shooterLoadedLim.get() || true) && getEncoder() <= BACKWARDS_REV && Gamepad.secondary.getRightY() <= 0.0) {
		 updatedSpeed = 0.0;
		 System.out.println("Can't move back");
		 }*/

		//// TODO: UNCOMMENT IT OUT WHEN IT IS DONE
		/*if (RobotSensors.shooterLoadedLim.get() && updatedSpeed >= 0.0) {
		 updatedSpeed = 0.0;
		 }*/
		if (ControlBox.getTopSwitch(1) && RobotPickup.isPickupInShootPosition()) {
			releaseLatch();
		} else {
			latch();
		}

		if (Gamepad.secondary.getA()) {
			//System.out.println("A PRESSED AND ENCODER RESET"); // TODO remove
			RobotSensors.shooterWinchEncoder.reset();
		}

		//RobotPickup.moveToShootPosition();
	}

	// sets speed to the unwind speed
	private static void automatedUnwind() {
		updatedSpeed = UNWIND_SPEED;
	}

	// sets the speed to the wind speed
	private static void automatedWind() {
		updatedSpeed = WIND_SPEED;
	}

	// sets the speed to 0.0
	public static void stopMotors() {
		updatedSpeed = 0.0;
		stage = 99;
	}

	// Releases the pnuematic
	public static void releaseLatch() {
		latch = true;
		//zeroEncoder();
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
		if (getEncoder() >= 100 && !getAtBack()) {
			//System.out.println("Bad limit switch: " + getEncoder());
		}

		// checks to see if the encoder should be zeroed
		if ((getEncoder() <= BACKWARDS_REV && updatedSpeed <= 0.0) || (getEncoder() >= MAX_REVS && updatedSpeed >= 0.0)) {
			updatedSpeed = 0.0;
		}

		/*if ((!getAtBack() && updatedSpeed <= 0) || (RobotSensors.shooterWinchEncoder.get() >= MAX_REVS && updatedSpeed >= 0.0)) {
		 updatedSpeed = 0.0;
		 }*/

		SmartDashboard.putBoolean("Shooter At back", getAtBack());
		
		if (!RobotSensors.shooterLoadedLim.get() && updatedSpeed >= 0) {
			updatedSpeed = 0.0;
		}
		// sets pnuematics
		RobotActuators.latchRelease.set(latch);
		if (RobotActuators.latchRelease.get()) {
			System.out.println(gameTime.get() + "   Latches");
		}

		// sets motor
		RobotActuators.shooterWinch.set(updatedSpeed);

		// prints to smart dashboard
		if (inManualMode) {
			manualShoot();
		} else {
			automatedShoot();
		}
	}

	public static double getEncoder() {
		return RobotSensors.shooterWinchEncoder.get();
	}

	////CURRENT CHECK CODE (ask Debjit)
	public static double getCurrent() {
		voltage = RobotSensors.currentSensor.getVoltage();
		current = (voltage - 500) * 0.05 - 100;
		return current;
		//System.out.println("Current = " + current + " Voltage = " + voltage); //Not too sure about the units, though. (most likely milli-)
	}
}
