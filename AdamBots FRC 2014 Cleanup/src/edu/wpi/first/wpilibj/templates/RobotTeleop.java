/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import frcclasses.Gamepad;
import subsystems.RobotShoot;
import subsystems.RobotDrive;
import subsystems.RobotPickup;
import subsystems.RobotVision;

/**
 *
 * @author Nathan
 */
public abstract class RobotTeleop {
	private static int pickupPosition = 1;
	private static boolean pickupPositionDebounce = false;
	private static boolean shootDebounce = false;
	//
	private static boolean previousShooterLeft = false;
	private static boolean previousShooterRight = false;
	private static boolean targetInManualMode = true;
	private static boolean shooterInManualMode = false;
	
	public static boolean isTargetManual() {
		return targetInManualMode;
	}

	private static void teleopDrive() {
		double forwardRate = Gamepad.primary.getTriggers();
		double turnRate = Gamepad.primary.getLeftX() * 1;
		double leftDrive = forwardRate - turnRate;
		double rightDrive = forwardRate + turnRate;

		leftDrive = Math.max(-1.0, Math.min(1.0, leftDrive));
		rightDrive = Math.max(-1.0, Math.min(1.0, rightDrive));

		double leftPWM = RobotDrive.pwmFromTPS(leftDrive * 900);
		double rightPWM = RobotDrive.pwmFromTPS(rightDrive * 900);

		leftPWM = Math.max(-1.0, Math.min(1.0, leftPWM));
		rightPWM = Math.max(-1.0, Math.min(1.0, rightPWM));
		RobotDrive.drive(leftPWM, rightPWM);

	}

	public static void teleop() {

		//SmartDashboard.putBoolean("shooter AUTO ENCODER", ControlBox.getTopSwitch(3));
		if (!targetInManualMode) {
			//Automatic targetting Mode (Using camera to figure out encoder)
			RobotShoot.setTargetTicks(RobotVision.getEncoder());
			// reinstated the vision's encoder
			//RobotShoot.setTargetTicks(1300);
		} else {
			//Manual targetting mode (using driver to tap left and right)
			if (Gamepad.secondary.getA()) {
				RobotShoot.setTargetTicks(1000);
			}
			if (Gamepad.secondary.getB()) {
				RobotShoot.setTargetTicks(1300);
			}

			if (Gamepad.secondary.getDPadLeft()) {
				if (!previousShooterLeft) {
					RobotShoot.adjustTargetDown();
				}
				previousShooterLeft = true;
			} else {
				previousShooterLeft = false;
			}
			if (Gamepad.secondary.getDPadRight()) {
				if (!previousShooterRight) {
					RobotShoot.adjustTargetUp();
				}
				previousShooterRight = true;
			} else {
				previousShooterRight = false;
			}
		}

		ControlBox.update();
		RobotDrive.update();
		RobotPickup.update();
		RobotShoot.update();

		RobotPickup.moveToShootPosition();

		///////////////

		if (Gamepad.primary.getB()) {
			RobotDrive.shiftHigh();
		} else if (Gamepad.primary.getA()) {
			RobotDrive.shiftLow();
		}

		// Begin drive control
		teleopDrive();
		// End Drive Control
		// Robot Pickup Control:
		// Both primary & secondary control rollers, potentially conflicting
		// resolved by summing (warn drivers)
		RobotPickup.setRollerSpeed(Gamepad.primary.getRightY() + Gamepad.secondary.getLeftY());


		if (Gamepad.secondary.getY()) {
			RobotPickup.openRollerArm();
		} else if (Gamepad.secondary.getX()) {
			RobotPickup.closeRollerArm();
		} else {
			RobotPickup.neutralRollerArm();
		}

		if (Gamepad.secondary.getLB() || Gamepad.secondary.getRB()) {
			if (!pickupPositionDebounce) {
				if (Gamepad.secondary.getLB()) {
					pickupPosition--;
				}
				if (Gamepad.secondary.getRB()) {
					pickupPosition++;
				}
				pickupPosition = Math.max(0, Math.min(3, pickupPosition));
			}
			pickupPositionDebounce = true;
		} else {
			pickupPositionDebounce = false;
		}

		switch (pickupPosition) {
			case 0:
				RobotPickup.moveToPickupPosition();
				break;
			case 1:
				RobotPickup.moveToShootPosition();
				break;
			case 2:
				RobotPickup.moveToTrussPosition();
				break;
			case 3:
				RobotPickup.moveToCatchPosition();
				break;
		}

		if (Math.abs(Gamepad.secondary.getTriggers()) > 0.9) {
			if (!shootDebounce) {
				System.out.println("Shoot!!!");
				RobotShoot.shoot();
			}
			shootDebounce = true;
		} else {
			shootDebounce = false;
		}


		////////////////////////

		if (!shooterInManualMode) {
			RobotShoot.useAutomatic();
		} else {
			RobotShoot.useManual();
		}

		if (Gamepad.secondary.getBack()) {
			shooterInManualMode = true;
		}
		if (Gamepad.secondary.getStart()) {
			shooterInManualMode = false;
		}
		if (Gamepad.primary.getBack()) {
			targetInManualMode = true;
		}
		if (Gamepad.primary.getStart()) {
			targetInManualMode = false;
		}

		if (Gamepad.primary.getX() && Gamepad.primary.getY()) {
			RobotShoot.zeroedBefore = false;
		}

	}
}
