/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autons;

import edu.wpi.first.wpilibj.templates.RobotSensors;

/**
 *
 * @author Curtis
 */
public class RobotAuton {

	private static boolean switchA = false;
	private static boolean switchB = false;
	private static boolean switchC = false;

	public boolean queryAutonomousConfiguration(boolean a, boolean b, boolean c) {
		return switchA == a && switchB == b && switchC == c;
	}

	// Sets up autonomous
	public static void initialize() {
		switchA = RobotSensors.configSwitchA.get();
		switchB = RobotSensors.configSwitchB.get();
		switchC = RobotSensors.configSwitchC.get();
		StandardOneBallAuton.initialize();
	}

	// Periodic updates
	public static void update() {
		StandardOneBallAuton.update();
	}
}