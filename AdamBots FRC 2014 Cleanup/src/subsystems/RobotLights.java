/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subsystems;

import edu.wpi.first.wpilibj.templates.RobotActuators;

/**
 *
 * @author Tyler
 */
public abstract class RobotLights {

////VARIABLES-------------------------------------------------------------------
////CONSTANTS-------------------------------------------------------------------
	public static void underglowOn() {
		RobotActuators.groundLEDStrip1.set(true);
		RobotActuators.groundLEDStrip2.set(true);
		RobotActuators.groundLEDStrip3.set(true);
		RobotActuators.groundLEDStrip4.set(true);

	}

	public static void underglowOff() {
		RobotActuators.groundLEDStrip1.set(false);
		RobotActuators.groundLEDStrip2.set(false);
		RobotActuators.groundLEDStrip3.set(false);
		RobotActuators.groundLEDStrip4.set(false);
	}
}
