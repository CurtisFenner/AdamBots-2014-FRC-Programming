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
	public class Time {
		private final double time;
		public Time(double time) {
			this.time = time;
		}
	}
	private final Timer timer = new Timer();
	public StopWatch() {
		timer.start();
	}
	public Time now() {
		return new Time(timer.get());
	}
	public double deltaSeconds(Time time) {
		return timer.get() - time.time;
	}
}
