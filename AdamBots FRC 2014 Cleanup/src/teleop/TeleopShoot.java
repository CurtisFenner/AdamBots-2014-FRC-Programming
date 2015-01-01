/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package teleop;

import frcclasses.Gamepad;
import subsystems.RobotShoot;
import subsystems.RobotVision;

/**
 *
 * @author Nathan
 */
public abstract class TeleopShoot {

	private static boolean shootDebounce = false;
	//
	private static boolean targetAdjustDebounce = false;
	private static boolean targetInManualMode = true;
	private static boolean shooterInManualMode = false;

	public static boolean isTargetManual() {
		return targetInManualMode;
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

	public static void runTeleop() {
		// evaluate the new tension target
		updateShooterTensionTarget();


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
			RobotShoot.manualControlValues(manualSpeed, manualReleaseLatch);
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
	}
}
