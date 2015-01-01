/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package teleop;

import auxiliary.MathUtils;
import frcclasses.Gamepad;
import subsystems.RobotDrive;

/**
 *
 * @author Nathan
 */
public abstract class TeleopDrive {

	public static void runTeleop() {

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
}
