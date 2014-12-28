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
	private static boolean targetAdjustDebounce = false;
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

	/**
	 * Commands RobotShoot to target a new tension. If targetInManualMode, it
	 * uses secondary A, B, and D-Pad to select tension. Otherwise, it requests
	 * the target from RobotVision.
	 */
	private static void updateShooterTensionTarget() {
		if (!targetInManualMode) {
			//Automatic targetting Mode (Using camera to figure out encoder)
			RobotShoot.setTargetTicks(RobotVision.getEncoder());
		} else {
			//Manual targetting mode (using driver to tap left and right)
			if (Gamepad.secondary.getA()) {
				RobotShoot.setTargetTicks(1000);
			}
			if (Gamepad.secondary.getB()) {
				RobotShoot.setTargetTicks(1300);
			}
			boolean adjustDown = Gamepad.secondary.getDPadLeft();
			boolean adjustUp = Gamepad.secondary.getDPadRight();
			if (!adjustDown && !adjustUp) {
				targetAdjustDebounce = false;
			}
			if (targetAdjustDebounce) {
				return;
			}
			if (adjustDown) {
				RobotShoot.adjustTargetDown();
			}
			if (adjustUp) {
				RobotShoot.adjustTargetUp();
			}
		}
	}

	public static void teleop() {
		updateShooterTensionTarget();
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

		if (shooterInManualMode) {
			RobotShoot.useManual();
		} else {
			RobotShoot.useAutomatic();
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

		// Override to force the shooter to rezero on next contact with the
		// limit switch[?]
		if (Gamepad.primary.getX() && Gamepad.primary.getY()) {
			RobotShoot.zeroedBefore = false;
		}

	}
}
