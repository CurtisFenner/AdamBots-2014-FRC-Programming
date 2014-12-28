/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import autons.StandardOneBallAuton;
import autons.AutonZero;
import autons.RobotAuton;
import auxiliary.FileWrite;
import subsystems.RobotShoot;
import subsystems.RobotLights;
import subsystems.RobotDrive;
import subsystems.RobotPickup;
import subsystems.RobotVision;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.Calendar;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the IterativeRobot documentation. If you change the name of this class
 * or the package after creating this project, you must also update the manifest file in the
 * resource directory.
 */
public class MainRobot extends IterativeRobot {

	public static String cumulativeErrorList = "";
	public static String logData = "";

	public static void handleException(Exception e, String from) {
		cumulativeErrorList += "{EXCEPTION FROM " + from + "}\n";
		DriverStation ds = null;
		try {
			ds = DriverStation.getInstance();
		} catch (Exception dsInstanceException) {
		}
		if (ds != null) {
			cumulativeErrorList += "\tmatch time\t" + ds.getMatchTime() + "\n";
		}
		cumulativeErrorList += e.getClass() + "\n\t" + e.getMessage() + "\n" + "\t" + e + "\n\n\n";
		try {
			FileWrite.writeFile("exceptions.txt", cumulativeErrorList);
		} catch (Exception u) {
		}
		System.out.println("EXCEPTIONS!!!!!!");
		System.out.println(cumulativeErrorList);
	}

	/**
	 * This function is run when the robot is first started up and should be used for any
	 * initialization code.
	 */
	public void robotInit() {
		RobotActuators.initialize();
		RobotSensors.initialize();
		RobotDrive.initialize();
		RobotPickup.initialize();
		RobotShoot.initialize();
		RobotVision.initialize();
		RobotAuton.initialize();
		ControlBox.initialize();
		System.out.println("Initialized");
		//// ADDED: UNDERGLOW FROM THE LINE BELOW
		RobotLights.underglowOn();
	}

	public void updateSystems() {
		RobotDrive.update();
		RobotPickup.update();
		RobotShoot.update();
		runCompressor();
		DashboardPut.put();
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		try {

			RobotShoot.useAutomatic();
			RobotAuton.update();
			updateSystems();

		} catch (Exception e) {
			handleException(e, "autonomousPeriodic");
		}
	}

	public void teleopInit() {
		RobotDrive.enableSmoothing();
		RobotLights.underglowOn();

	}

	public void disabledInit() {
		StandardOneBallAuton.timer.stop();
		StandardOneBallAuton.timer.reset();
		StandardOneBallAuton.secondTimer.stop();
		StandardOneBallAuton.secondTimer.reset();
		RobotLights.underglowOn();
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {
		try {

			if (RobotShoot.gameTime.get() == 0) {
				RobotShoot.gameTime.start();
			}
			RobotTeleop.teleop();
			ControlBox.update();

			updateSystems();

		} catch (Exception e) {
			handleException(e, "teleopPeriodic");
		}
	}
	private int counterOnTest; //Used in testPeriodic, testInit for debug.

	public void testInit() {
		counterOnTest = 0;
	}

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		runCompressor();
		DashboardPut.put();
		RobotPickup.closeRollerArm();
		if (counterOnTest <= 15) {
			RobotActuators.shooterWinch.set(-0.3);
			RobotActuators.latchRelease.set(false);
			if (!RobotSensors.shooterAtBack.get()) {
				counterOnTest++;
			}
		} else {
			RobotActuators.latchRelease.set(true);
		}
		if (counterOnTest >= 16 && counterOnTest <= 50) {
			RobotActuators.shooterWinch.set(0.3);
			counterOnTest++;
			RobotActuators.latchRelease.set(true);
		}
		if (counterOnTest >= 51) {
			RobotActuators.shooterWinch.set(0.0);
		}

		// CHANGED: from RobotDrive.stopDrive();
		RobotActuators.leftDrive.set(0.0);
		RobotActuators.rightDrive.set(0.0);

		System.out.println("counterOnTest: " + counterOnTest);
	}

	private void runCompressor() {
		SmartDashboard.putBoolean("Pressure Switch", RobotSensors.pressureSwitch.get());
		if (!RobotSensors.pressureSwitch.get()) {
			RobotActuators.compressor.set(Relay.Value.kOn);
			//System.out.println("Setting the compressor to ON");
		} else {
			RobotActuators.compressor.set(Relay.Value.kOff);
		}
		//System.out.println("runCompressor finished");
	}

	public void disabledPeriodic() {
		try {
			RobotDrive.stopDrive();
			RobotShoot.stopMotors();
			AutonZero.reset();
			DashboardPut.put();
			//maxTrueCount = 0;
			if (logData.length() != 0) {
				FileWrite.writeFile("log" + Calendar.HOUR + "_" + Calendar.MINUTE + ".txt", logData);
			}
			logData = "";
		} catch (Exception e) {
			handleException(e, "disabledPeriodic");
		}
	}

	public void autonomousInit() {
		RobotShoot.reset();
		RobotAuton.initialize();
		RobotLights.underglowOn();
	}
}
