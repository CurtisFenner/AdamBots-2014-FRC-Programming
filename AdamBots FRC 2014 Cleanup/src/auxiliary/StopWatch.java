/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliary;

import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author Nathan
 */
public final class StopWatch {
	private final Timer timer = new Timer();
	private double markTime = 0;
	public StopWatch() {
		timer.start();
	}
	public void markEvent() {
		markTime = timer.get();
	}
	public double deltaSeconds() {
		return timer.get() - markTime;
	}
	public boolean isAfter(double time) {
		return deltaSeconds() >= time;
	}
}
