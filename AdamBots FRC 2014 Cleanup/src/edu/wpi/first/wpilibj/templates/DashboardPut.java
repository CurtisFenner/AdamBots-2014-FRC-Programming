/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import subsystems.RobotShoot;
import subsystems.RobotPickup;
import subsystems.RobotVision;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import static subsystems.RobotShoot.getAtBack;

/**
 *
 * @author Blu
 */
public abstract class DashboardPut {

	public static void put() {
		//Shooter diagnostics:
		SmartDashboard.putNumber("shooter EXPECT CURRENT", RobotShoot.getEncoder() * 0.01);
		SmartDashboard.putNumber("shooter ENCODER", RobotShoot.getEncoder());
		SmartDashboard.putBoolean("shooter MANUAL", RobotShoot.isManual());
		SmartDashboard.putBoolean("shooter TARGET MANUAL", RobotTeleop.isTargetManual());
		SmartDashboard.putBoolean("shooter AT BACK", RobotShoot.getAtBack());
		//Pickup diagnostics:
		SmartDashboard.putNumber("pickup ARM ANGLE", RobotPickup.getArmAngleAboveHorizontal());
		SmartDashboard.putNumber("pickup ARM ANGLE TARGET", RobotPickup.getArmTargetAngle());

		SmartDashboard.putBoolean("pickup TRUSS POSITION", RobotPickup.isPickupInTrussPosition());
		SmartDashboard.putBoolean("pickup ARM LIMIT UPPER", RobotPickup.isUpperLimitReached());
		SmartDashboard.putBoolean("pickup ARM LIMIT LOWER", RobotPickup.isLowerLimitReached());
		//Drive
		//SmartDashboard.putBoolean("drive ESTOP",RobotDrive.isStopped());
		//General status
		DriverStation driverStation = DriverStation.getInstance();
		SmartDashboard.putNumber("status BATTERY", driverStation.getBatteryVoltage());
		SmartDashboard.putNumber("vision RED DISTANCE", RobotVision.redDistance());
		SmartDashboard.putNumber("vision BLUE DISTANCE", RobotVision.blueDistance());
		SmartDashboard.putNumber("vision DISTANCE", RobotVision.getDistance());

		SmartDashboard.putNumber("vision HOT NUMBER", RobotVision.getNumber("hot")); //this is on the robot

		// TESTING VARIABLES
		SmartDashboard.putNumber("pickup POTENTIOMETER", RobotSensors.pickupPotentiometer.get());
	}
}
