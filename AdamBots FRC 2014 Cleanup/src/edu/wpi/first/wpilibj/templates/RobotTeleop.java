/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import subsystems.RobotPickup;
import teleop.TeleopDrive;
import teleop.TeleopPickup;
import teleop.TeleopShoot;

/**
 *
 * @author Nathan
 */
public abstract class RobotTeleop {




	public static void runTeleop() {

		// Begin drive control

		TeleopDrive.runTeleop();

		TeleopPickup.runTeleop();

		TeleopShoot.runTeleop();
	}
}
