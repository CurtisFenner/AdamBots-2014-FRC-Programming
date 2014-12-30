/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subsystems;

import auxiliary.MathUtils;
import edu.wpi.first.wpilibj.templates.RobotActuators;
import edu.wpi.first.wpilibj.templates.RobotSensors;

/**
 *
 * @author Nathan Fenner
 */
public abstract class RobotDrive {

	public static final double distancePerTick = 83.0 / 1109.0;
	private static double targetSpeedLeft = 0.0;
	private static double targetSpeedRight = 0.0;
	private static double currentSpeedLeft = 0.0;
	private static double currentSpeedRight = 0.0;
	private static boolean smoothingEnabled = true;

////INIT------------------------------------------------------------------------
	public static void initialize() {
		System.out.println("RobotDrive init");
		RobotSensors.rightDriveEncoder.start();
		RobotSensors.leftDriveEncoder.start();
		RobotSensors.rightDriveEncoder.setDistancePerPulse((Math.PI * 0.5) / 360);
		RobotSensors.leftDriveEncoder.setDistancePerPulse((Math.PI * 0.5) / 360);
	}
////METHODS---------------------------------------------------------------------
	private static double shift_up = 3.0 / 10.0;
	private static double shift_down = 20.0 / 100.0;

	//4.283 smooth
	//6.540 shift
	/**
	 * In inches
	 *
	 * @return
	 */
	public static double getEncoderLeftInches() {
		return getEncoderLeftTicks() * distancePerTick;
	}

	public static int getEncoderLeftTicks() {
		return -RobotSensors.leftDriveEncoder.get();
	}

	/**
	 * In inches
	 *
	 * @return
	 */
	public static double getEncoderRightInches() {
		return getEncoderRightTicks() * distancePerTick;
	}

	public static int getEncoderRightTicks() {
		return RobotSensors.rightDriveEncoder.get();
	}

	public static double getEncoderAverageInches() {
		return getEncoderAverageTicks() * distancePerTick;
	}

	public static int getEncoderAverageTicks() {
		return getEncoderRightTicks(); // the left is broken on the competition robot
	}

	public static void update() {

		double shift_left = (MathUtils.sign(targetSpeedLeft) == MathUtils.sign(targetSpeedLeft - currentSpeedLeft)) ? shift_up : shift_down;
		double shift_right = (MathUtils.sign(targetSpeedRight) == MathUtils.sign(targetSpeedRight - currentSpeedRight)) ? shift_up : shift_down;

		currentSpeedLeft = MathUtils.toward(currentSpeedLeft, targetSpeedLeft, shift_left);

		currentSpeedRight = MathUtils.toward(currentSpeedRight, targetSpeedRight, shift_right);

		// Use currentSpeed and velocity to set raw
		if (smoothingEnabled) {
			RobotDrive.driveSetRaw(currentSpeedLeft, currentSpeedRight);
		} else {
			RobotDrive.driveSetRaw(targetSpeedLeft, targetSpeedRight);
		}
	}

	public static double pwmFromRPM(double rpm) {
		return pwmFromTPS(rpm / 60 * 360);
	}

	/**
	 * Transforms a rotation rate to a PWM value
	 *
	 * @param tps Ticks per second
	 * @return
	 */
	public static double pwmFromTPS(double tps) {
		return (0.1139 * MathUtils.exp(0.0024 * Math.abs(tps)) - .1139) * MathUtils.sign(tps) / (0.987642579 - .1139);
	}

	/**
	 * Sets drive speed to go forward
	 *
	 * @param speed
	 */
	public static void driveStraight(double speed) {
		drive(speed, speed);
	}

	/**
	 * Sets the left and right drive safely, which it fits into the [-1,1] range.
	 *
	 * @param leftSpeed
	 * @param rightSpeed
	 */
	public static void drive(double leftSpeed, double rightSpeed) {
		leftSpeed = Math.max(-1, Math.min(1, leftSpeed));
		rightSpeed = Math.max(-1, Math.min(1, rightSpeed));

		targetSpeedLeft = leftSpeed;
		targetSpeedRight = rightSpeed;
	}

	/**
	 * Raw setting speed, not smooth: avoid use whenever possible
	 *
	 * @param left
	 * @param right
	 */
	private static void driveSetRaw(double leftSpeed, double rightSpeed) {
		leftSpeed = Math.max(-1, Math.min(1, leftSpeed));
		rightSpeed = Math.max(-1, Math.min(1, rightSpeed));
		RobotActuators.leftDrive.set(leftSpeed);
		RobotActuators.rightDrive.set(-rightSpeed);
	}

	/**
	 * Sets the robot to turn in an arc
	 *
	 * @param turnRate Positive values turn right (clockwise)
	 * @param forwardSpeed Positive values go forward
	 */
	public static void turn(double turnRate, double forwardSpeed) {
		drive(forwardSpeed + turnRate, forwardSpeed - turnRate);
	}

	public static void shiftHigh() {
		RobotActuators.shifter.set(true);
	}

	public static void shiftLow() {
		RobotActuators.shifter.set(false);
	}

	public static void stopDrive() {
		targetSpeedLeft = 0.0;
		currentSpeedLeft = 0.0;
		targetSpeedRight = 0.0;
		currentSpeedRight = 0.0;
		driveSetRaw(0,0);
	}

	public static void disableSmoothing() {
		smoothingEnabled = false;
	}

	public static void enableSmoothing() {
		smoothingEnabled = true;
	}
}
