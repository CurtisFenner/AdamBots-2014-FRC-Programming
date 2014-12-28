/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import frcclasses.AnalogSwitch;
import edu.wpi.first.wpilibj.*;

/**
 *
 * @author Tyler
 */
public abstract class RobotSensors {

////VARIABLES-------------------------------------------------------------------
	public static AnalogChannel currentSensor;
	public static AnalogChannel configSwitchA;
	public static AnalogChannel configSwitchB;
	public static AnalogChannel configSwitchC;
	public static Encoder rightDriveEncoder;
	public static Encoder leftDriveEncoder;
	public static AnalogPotentiometer pickupPotentiometer;
	public static Encoder shooterWinchEncoder;
	public static DigitalInput ballReadyToLiftLim;
	public static DigitalInput pickupSystemDownLim;
	public static DigitalInput pickupSystemUpLim;
	public static AnalogSwitch shooterLoadedLim;
	public static DigitalInput shooterAtBack;
	public static DigitalInput pressureSwitch;
	public static ADXL345_I2C accelerometer;

	public static void initialize() {
		//// Analog
		currentSensor = new AnalogChannel(1);
		configSwitchA = new AnalogChannel(3);
		configSwitchB = new AnalogChannel(4);
		configSwitchC = new AnalogChannel(5);
		pickupPotentiometer = new AnalogPotentiometer(2);
		shooterLoadedLim = new AnalogSwitch(new AnalogChannel(6));
		//Now in port TWO on both robots.

		//// Digital In 1
		rightDriveEncoder = new Encoder(1, 11);
		leftDriveEncoder = new Encoder(2, 12);
		ballReadyToLiftLim = new DigitalInput(3);
		shooterWinchEncoder = new Encoder(4, 5);
		pickupSystemDownLim = new DigitalInput(6);
		pickupSystemUpLim = new DigitalInput(7);
		shooterAtBack = new DigitalInput(13);
		pressureSwitch = new DigitalInput(10);

		//// Digital 1 Serial
		accelerometer = new ADXL345_I2C(1, ADXL345_I2C.DataFormat_Range.k2G);

		System.out.println("Sensor init done");
	}
}
