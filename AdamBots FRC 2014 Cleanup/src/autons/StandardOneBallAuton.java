/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autons;

import subsystems.RobotShoot;
import subsystems.RobotDrive;
import subsystems.RobotPickup;
import subsystems.RobotVision;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.templates.RobotSensors;

/**
 *
 * @author Tyler
 */
public class StandardOneBallAuton {

	public static final double STRAIGHT_DISTANCE = 450; // needs to be found in testing
	public static final double BACKWARDS_DISTANCE = 0; // needs to be found in testing
	public static double averageDriveEncoder;
	public static Timer timer;
	public static double fallTimer = 2.0;
	public static double closeTime = 2.0;
	public static int step;

	// reset all Encoders
	public static void reset() {
		RobotSensors.leftDriveEncoder.reset();
		RobotSensors.rightDriveEncoder.reset();
		//RobotSensors.shooterWinchEncoder.reset();
		timer.stop();
		timer.reset();
	}

	public static void stepOne() {
		if (timer.get() == 0) {
			timer.start();
			RobotShoot.startShoot();
			System.out.println("start at 0");
		}

		RobotDrive.disableSmoothing();

		double forward = -1.0;

		if (averageDriveEncoder <= STRAIGHT_DISTANCE) {
			RobotDrive.drive(forward, forward);
		} else {
			RobotDrive.stopDrive();
		}

		RobotPickup.moveToShootPosition();

		if (RobotPickup.isPickupInShootPosition() && averageDriveEncoder >= STRAIGHT_DISTANCE) {
			step = 3;
		}
	}
	//// VARIABLES -------------------------------------------------------------
	public static final double speed = 0.5;
	public static double startMovingBack;
	public static final int TENSION_VALUE = 1090; //downloaded to robot for Q26
	public static double openingTime = 0.5;
	public static double currentTime = 0.0;
	public static Timer secondTimer;

	// init
	public static void initialize() {
		timer = new Timer();
		step = 1;
		startMovingBack = 0.0;
		secondTimer = new Timer();
		RobotShoot.setTargetTicks(TENSION_VALUE);	// AUTON TARGET TICKS
	}

	// Moves forward while putting the arm down
	public static void stepTwo() {
	}

	// shoots if the goal is hot or timer says so
	public static void stepThree() {
		RobotPickup.openRollerArm();
		if (secondTimer.get() == 0 && RobotVision.isHot() && RobotShoot.isReadyToShoot()) {
			secondTimer.start();
		}
		if ((secondTimer.get() >= 0.5 || timer.get() >= 7.0) && RobotShoot.isReadyToShoot()) {
			secondTimer.stop();
			secondTimer.reset();
			RobotShoot.shoot();
			//FileWrite.writeFile("autonshot.txt", "\nhot: " + RobotVision.isHot() + "\nTime: " + timer.get());
			startMovingBack = timer.get() + 0.5;
			step = 99;
			// If you ever want to move back after shooting in autonous, change to step 4;
		}
	}

	// waits 0.5 seconds
	public static void stepFour() {
		if (startMovingBack <= timer.get()) {
			step = 5;
		}
	}

	// moves back to the white line
	public static void stepFive() {
		if (averageDriveEncoder >= BACKWARDS_DISTANCE) {
			double forward = 1.0;
			RobotDrive.drive(forward, forward);
		} else {
			RobotDrive.stopDrive();
			step = 99;
		}
	}

	// update method
	public static void update() {
		averageDriveEncoder = RobotDrive.getEncoderRightTicks();
		SmartDashboard.putBoolean("Pickup in shoot", RobotPickup.isPickupInShootPosition());
		SmartDashboard.putBoolean("vision IS HOT", RobotVision.isHot());
		switch (step) {
			case 1:
				stepOne();
				break;
			case 2:
				stepTwo();
				break;
			case 3:
				stepThree();
				break;
			// Case 4, 5 make robot move backward
			/*case 4:
			 stepFour();
			 break;
			 case 5:
			 stepFive();
			 break;*/
			default:
				break;
		}
	}
}
