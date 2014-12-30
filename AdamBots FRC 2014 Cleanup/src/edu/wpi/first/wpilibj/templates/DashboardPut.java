/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import auxiliary.MathUtils;
import subsystems.RobotShoot;
import subsystems.RobotPickup;
import subsystems.RobotVision;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 * @author Blu
 */
public abstract class DashboardPut {

	public static void put() {
		//Shooter diagnostics:
		SmartDashboard.putNumber("shooter EXPECT CURRENT", RobotShoot.getEncoder() * 0.01);
		SmartDashboard.putNumber("shooter ENCODER", RobotShoot.getEncoder());
		SmartDashboard.putBoolean("shooter MANUAL", RobotShoot.isInManualMode());
		SmartDashboard.putBoolean("shooter TARGET MANUAL", RobotTeleop.isTargetManual());
		SmartDashboard.putBoolean("shooter AT BACK", RobotShoot.getAtBack());
		SmartDashboard.putNumber("shooter CURRENT SPEED", RobotShoot.getCurrentSpeed());
		SmartDashboard.putString("shooter STAGE", RobotShoot.getStage() + "");
		SmartDashboard.putNumber("shooter STAGE NUMBER", RobotShoot.getStage() + MathUtils.rand(1) / 1000);
		SmartDashboard.putNumber("shooter LATCH", RobotShoot.isLatched() ? 1 + MathUtils.rand(1) / 1000 : 0 + MathUtils.rand(1) / 1000);
		SmartDashboard.putNumber("shooter TARGET TICKS", RobotShoot.getTargetTicks());

		SmartDashboard.putNumber("shooter TIMER", RobotShoot.stageTime());
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
