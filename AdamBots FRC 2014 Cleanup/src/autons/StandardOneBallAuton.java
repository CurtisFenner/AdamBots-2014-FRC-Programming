/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autons;

import auxiliary.StopWatch;
import subsystems.RobotShoot;
import subsystems.RobotDrive;
import subsystems.RobotPickup;
import subsystems.RobotVision;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 * @author Tyler
 */
public class StandardOneBallAuton {

	private static final double STRAIGHT_DISTANCE_TICKS = 450; // needs to be found in testing
	//// VARIABLES -------------------------------------------------------------
	private static final int TENSION_VALUE = 1090; //downloaded to robot for Q26
	///
	private static int stage = 0;
	private static final StopWatch stageTimer = new StopWatch();
	private static final StopWatch autonTimer = new StopWatch();

	// reset all Encoders
	public static void reset() {
		RobotDrive.resetEncoders();
		//RobotSensors.shooterWinchEncoder.reset();
	}

	private static void changeStage(int newStage) {
		stage = newStage;
		stageTimer.markEvent();
	}

	// init
	public static void initialize() {
		autonTimer.markEvent();

		changeStage( 1 ); // begin the auton procedure

	}

	private static void stageOne() {
		RobotShoot.startShoot();
		RobotShoot.setTargetTicks(TENSION_VALUE);	// AUTON TARGET TICKS
		RobotDrive.disableSmoothing();
		RobotPickup.moveToShootPosition();
		System.out.println("start at 0");

		changeStage(2);

	}

	private static void stageTwo() {
		double forward = -1;
		if (RobotDrive.getEncoderAverageTicks() <= STRAIGHT_DISTANCE_TICKS) {
			RobotDrive.driveStraight(forward);
		} else {
			RobotDrive.stopDrive();
			if (RobotPickup.isPickupInShootPosition()) {
				changeStage(3);
			}
		}
	}

	private static void stageThree() {
		RobotPickup.openRollerArm();
		if (RobotShoot.isReadyToShoot()) {
			changeStage(4);
		}
	}

	private static void stageFour() {
		if (stageTimer.hasElapsed(0.5)) {
			changeStage(5);
		}
	}

	private static void stageFive() {
		if (RobotVision.isHot() || autonTimer.hasElapsed(7)) {
			changeStage(6);
		}
	}

	private static void stageSix() {
		RobotShoot.shoot();
		changeStage(99);
	}

	// update method
	public static void update() {
		SmartDashboard.putBoolean("Pickup in shoot", RobotPickup.isPickupInShootPosition());
		SmartDashboard.putBoolean("vision IS HOT", RobotVision.isHot());
		switch (stage) {
			case 1:
				stageOne();
				break;
			case 2:
				stageTwo();
				break;
			case 3:
				stageThree();
				break;
			case 4:
				stageFour();
				break;
			case 5:
				stageFive();
				break;
			case 6:
				stageSix();
				break;
			default:
				break;
		}
	}
}
