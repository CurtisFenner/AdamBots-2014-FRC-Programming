/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package teleop;

import auxiliary.MathUtils;
import frcclasses.Gamepad;
import subsystems.RobotPickup;

/**
 *
 * @author Nathan
 */
public abstract class TeleopPickup {

	private static int pickupPosition = 1;
	private static boolean pickupPositionDebounce = false;

	public static void runTeleop() {

		// a default position for the pickup mechanism
		RobotPickup.moveToShootPosition();

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
}
