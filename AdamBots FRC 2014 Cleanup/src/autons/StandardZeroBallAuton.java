/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autons;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import subsystems.RobotDrive;
import subsystems.RobotPickup;
import edu.wpi.first.wpilibj.templates.*;
import subsystems.RobotShoot;

/**
 *
 * @author Tyler
 */
public class StandardZeroBallAuton {

	public static final double STRAIGHT_DISTANCE = 50; // needs to be found in testing
	public static final double BACKWARDS_DISTANCE = -50; // needs to be found in testing
	public static double averageDriveEncoder;
	public static Timer timer;
	public static double fallTimer = 2.0;
	public static double closeTime = 2.0;
	public static int step;
	public static final double speed = 0.5;

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
	public static void intialize() {
		timer = new Timer();
		step = 1;
		reset();
		averageDriveEncoder = 0.0;
	}

	// Auton step one
	public static void stepTwo() {
		RobotPickup.moveToPickupPosition();
		if (averageDriveEncoder <= STRAIGHT_DISTANCE) {
			double forward = speed * Math.max(-1, Math.min(1, (STRAIGHT_DISTANCE - averageDriveEncoder) / 1000.0)) + .2;
			RobotDrive.drive(forward, forward);
		} else {
			RobotDrive.drive(0, 0);
			step = 3;
		}
	}

	// auton step two
	public static void stepThree() {
		step = 4;
	}

	// auton step three
	public static void stepFour() {
		if (averageDriveEncoder >= BACKWARDS_DISTANCE) {
			//Forward is negative, so actually, backwards, but counting in the direction of forwards.
			double forward = speed * Math.max(-1, Math.min(1, (BACKWARDS_DISTANCE - averageDriveEncoder) / 1000.0)) - .2;
			RobotDrive.drive(forward, forward);
		} else {
			RobotDrive.drive(0, 0);
			step = 5;
		}
	}

	// update method
	public static void update() {
		averageDriveEncoder = RobotDrive.getEncoderRightTicks();
		SmartDashboard.putBoolean("Pickup in shoot", RobotPickup.isPickupInShootPosition());
		StandardOneBallAuton.update();
		averageDriveEncoder = (RobotDrive.getEncoderLeftTicks() + RobotDrive.getEncoderRightTicks()) / 2.0;
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
			case 4:
				stepFour();
				break;
			default:
				break;
		}
	}
}
