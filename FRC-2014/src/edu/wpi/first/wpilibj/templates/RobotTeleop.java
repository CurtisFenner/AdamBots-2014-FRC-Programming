/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 * @author Nathan
 */
public class RobotTeleop {

	static double fine_speed = 0.0;
	private static int pickupPosition = 0;
	private static boolean pickupPositionDebounce = false;
	private static boolean catchClosing = false;
	private static boolean catchClosingDebounce = false;
	private static boolean shootDebounce = false;
	public static double DEBUG_OSCILLATE = 0.0;

	public static void update() {

		if (Gamepad.primary.getB()) {
			RobotDrive.shiftHigh();
		} else if (Gamepad.primary.getA()) {
			RobotDrive.shiftLow();
		}

		// Begin drive control

		double forwardRate = Gamepad.primary.getTriggers();
		double turnRate = Gamepad.primary.getLeftX() * 1;
		double leftDrive = forwardRate - turnRate;
		double rightDrive = forwardRate + turnRate;

		double leftPWM = RobotDrive.pwmFromTPS(leftDrive * 900);
		double rightPWM = RobotDrive.pwmFromTPS(rightDrive * 900);

		leftPWM = Math.max(-1.0, Math.min(1.0, leftPWM));
		rightPWM = Math.max(-1.0, Math.min(1.0, rightPWM));
		RobotDrive.drive(leftPWM, rightPWM);

		DEBUG_OSCILLATE = (DEBUG_OSCILLATE + 0.001) % 1.0; // used for SmartDashboard control

		// End Drive Control

		// Robot Pickup Control:

		// both can control it, potentially fighting with each other
		// care must be taken here
		RobotPickup.setRollerSpeed(Gamepad.primary.getRightY() + Gamepad.secondary.getLeftY());

		//RobotPickup.adjustArmAngle(Gamepad.secondary.getTriggers());

		RobotPickup.setOverrideEncoderMode(Gamepad.secondary.getBack());
		RobotPickup.setOverrideSpeed(Gamepad.secondary.getTriggers() / 3.0);


		if (Gamepad.secondary.getY()) {
			RobotPickup.openRollerArm();
		} else if (Gamepad.secondary.getX()) {
			RobotPickup.closeRollerArm();
		} else {
			RobotPickup.neutralRollerArm();
		}

		SmartDashboard.putBoolean("Left Switch 1", ControlBox.getLeftSwitch(1));
		// added the false &&
		if (false && ControlBox.getLeftSwitch(1)) {
			RobotPickup.enterOverrideEncoderMode();
			double overridePickupAngleSpeed = Gamepad.secondary.getTriggers() * 0.4;
			//double overridePickupAngleSpeed = (Gamepad.secondary.getLB() ? 0.35 : 0) + (Gamepad.secondary.getRB() ? -0.35 : 0);
			RobotPickup.setOverrideSpeed(overridePickupAngleSpeed);
			// automatic shooting uses opposite controls now
			if (Gamepad.secondary.getRB()) {
				if (!shootDebounce) {
					RobotShoot.shoot();
				}
				shootDebounce = true;
			} else {
				shootDebounce = false;
			}
		} else {
			RobotPickup.exitOverrideEncoderMode();
			if (Gamepad.secondary.getLB() || Gamepad.secondary.getRB()) {
				if (!pickupPositionDebounce) {
					if (Gamepad.secondary.getLB()) {
						pickupPosition--;
					}
					if (Gamepad.secondary.getRB()) {
						pickupPosition++;
					}
					pickupPosition = Math.max(0, Math.min(2, pickupPosition));
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
		}

		if (Gamepad.secondary.getA()) {
			if (!catchClosingDebounce) {
				catchClosing = false;
			}
			catchClosingDebounce = true;
			double col = RobotVision.highBlueBall();
			if (col > 200) {
				catchClosing = true;
			}
			if (catchClosing) {
				RobotPickup.closeRollerArm();
			}
		} else {
			catchClosingDebounce = false;
		}

	}
}
