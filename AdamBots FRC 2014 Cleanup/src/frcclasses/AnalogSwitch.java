/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package frcclasses;

import edu.wpi.first.wpilibj.AnalogChannel;

/**
 *
 * @author Tyler
 */
public final class AnalogSwitch {

	//// VARIABLES -------------------------------------------------------------

	private final AnalogChannel analogSensor; // truely named switch but that's reserved
	private final double valueThreshold;

	//// CONSTRUCTORS ----------------------------------------------------------

	// constructor that takes a channel and a sidecard number
	public AnalogSwitch(AnalogChannel analogSensor) {
		this.analogSensor = analogSensor;
		this.valueThreshold = 2.5;
	}

	// constructor that takes a channel and a sidecard number
	public AnalogSwitch(AnalogChannel analogSensor, double valueThreshold) {
		this.analogSensor = analogSensor;
		this.valueThreshold = valueThreshold;
	}

	//// METHODS ---------------------------------------------------------------
	// gets wether the switch is considered on or off
	public boolean get() {
		return analogSensor.getVoltage() >= valueThreshold;
	}
}
