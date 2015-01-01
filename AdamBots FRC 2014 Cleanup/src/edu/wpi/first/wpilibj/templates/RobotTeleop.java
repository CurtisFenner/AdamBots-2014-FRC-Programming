/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import auxiliary.MathUtils;
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

		if (Gamepad.primary.getB()) {
			RobotDrive.shiftHigh();
		} else if (Gamepad.primary.getA()) {
			RobotDrive.shiftLow();
		}

		double forwardRate = Gamepad.primary.getTriggers();
		double turnRate = Gamepad.primary.getLeftX() * 1;
		double leftDrive = forwardRate - turnRate;
		double rightDrive = forwardRate + turnRate;

		leftDrive = MathUtils.capValueMinMax(leftDrive, -1, 1);
		rightDrive = MathUtils.capValueMinMax(rightDrive, -1, 1);

		double leftPWM = RobotDrive.pwmFromTPS(leftDrive * 900);
		double rightPWM = RobotDrive.pwmFromTPS(rightDrive * 900);

		leftPWM = MathUtils.capValueMinMax(leftPWM, -1, 1);
		rightPWM = MathUtils.capValueMinMax(rightPWM, -1, 1);
		RobotDrive.drive(leftPWM, rightPWM);
	}

	/**
	 * Commands RobotShoot to target a new tension. If targetInManualMode, it uses secondary A, B,
	 * and D-Pad to select tension. Otherwise, it requests the target from RobotVision.
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

	public static void teleopPickup() {

		// Sets roller speed (D1 right-Y) + (D2 left-Y)
		RobotPickup.setRollerSpeed(Gamepad.primary.getRightY() + Gamepad.secondary.getLeftY());

		// Opens or closes the roller arm (secondary Y for open, X for close)
		if (Gamepad.secondary.getY()) {
			RobotPickup.openRollerArm();
		} else if (Gamepad.secondary.getX()) {
			RobotPickup.closeRollerArm();
		} else {
			RobotPickup.neutralRollerArm();
		}

		// Raises or lowers the arm
		// 'pickupPositionDebounce' makes it only happen once per press,
		// rather than once per frame.
		if (Gamepad.secondary.getLB() || Gamepad.secondary.getRB()) {
			if (!pickupPositionDebounce) {
				if (Gamepad.secondary.getLB()) {
					pickupPosition--;
				}
				if (Gamepad.secondary.getRB()) {
					pickupPosition++;
				}
				pickupPosition = MathUtils.capValueMinMax(pickupPosition, 0, 3);
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
	}

	public static void teleopShoot() {
		// `shootDebounce` prevents repeated shot attempts.
		// it will only fire the moment the triggers are pressed,
		// and fire again only after letting go of the trigger
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
			double manualSpeed = Gamepad.secondary.getRightY();
			boolean manualReleaseLatch = Math.abs(Gamepad.secondary.getTriggers()) > .8;
			RobotShoot.manualControlValues(manualSpeed,manualReleaseLatch);
		} else {
			RobotShoot.useAutomatic();
		}

		if (Gamepad.secondary.getBack()) {
			shooterInManualMode = true;
		}
		if (Gamepad.secondary.getStart()) {
			shooterInManualMode = false;
		}
		// This gives `primary` priority over secondary.
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

	public static void teleop() {
		updateShooterTensionTarget();
		RobotPickup.moveToShootPosition();
		///////////////

		// Begin drive control
		teleopDrive();
		teleopPickup();
		teleopShoot();
	}
}
