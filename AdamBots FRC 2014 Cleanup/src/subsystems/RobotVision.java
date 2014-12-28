/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package subsystems;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.templates.ControlBox;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author Curtis Fenner
 */
public abstract class RobotVision {

	private static Timer timer = new Timer();
	private static String database = "";
	private static final String BEAGEL_ADDRESS = "10.2.45.3:3000";
	private static double previousEncoder = 1000;

	public static double getEncoder() {
		double distance;
		double ticks;
		if (ControlBox.isRed()) {
			distance = redDistance();
		} else {
			distance = blueDistance();
		}
		if (distance <= 5) {
			return previousEncoder;
		}
		// A quadratic regression from tests
		ticks = 1.4674 * distance * distance - 27.253 * distance + 1226.5;
		previousEncoder = ticks;
		return Math.max(500, Math.min(1500, previousEncoder));
	}

	public static void initialize() {
		timer.start();
		System.out.println("Robot Vision Intialize");
		Thread visionThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(30);
						retrieve();
					} catch (Exception e) {
						//System.out.println("Exception in vision thread: " + e);
					}
				}
			}
		});
		visionThread.start();
	}

	public static String getProperty(String s) {
		String databaseCopy = RobotVision.database;
		if (databaseCopy == null) {
			return "";
		}
		String key = "";
		String val = "";
		boolean mode = true;
		for (int i = 0; i < databaseCopy.length(); i++) {
			char c = databaseCopy.charAt(i);
			if (c == ':') {
				mode = false;
				continue;
			}
			if (c == '\n') {
				mode = true;
				if (key.equals(s)) {
					return val;
				}
				key = "";
				val = "";
				continue;
			}
			if (mode) {
				key += c;
			} else {
				val += c;
			}
		}
		return "";
	}

	public static double parseNumber(String s) {
		try {
			s = s.trim();
			return Double.parseDouble(s);
		} catch (Exception e) {
			return 0.0;
		}
	}

	public static double getNumber(String key) {
		return parseNumber(getProperty(key));
	}

	public static boolean isHot() {
		return getNumber("hot") >= 80;
	}

	public static double getDistance() {
		if (ControlBox.isRed()) {
			return redDistance();
		}
		return blueDistance();
	}

	public static double redDistance() {
		return getNumber("red");
	}

	public static double blueDistance() {
		return getNumber("blue");
	}

	public static double redBallAngle() {
		return (getNumber("red ball") / 320 - 0.5) * 40;
	}

	public static double blueBallAngle() {
		return (getNumber("blue ball") / 320 - 0.5) * 40;
	}

	public static double highBlueBall() {
		return getNumber("blue high");
	}

	public static double highRedBall() {
		return getNumber("red high");
	}

	public static double blueBallDist() {
		return getNumber("blue ball dist");
	}

	public static double redBallDist() {
		return getNumber("red ball dist");
	}
	static SocketConnection http = null;
	static InputStream data = null;

	public static void retrieve() {
		boolean connectionFailure = true;
		try {
			http = (SocketConnection) Connector.open("socket://" + BEAGEL_ADDRESS);
			connectionFailure = false;
			data = http.openInputStream();
			String buildingDatabase = "";
			int p = 1;
			int length = 0;
			int failedAttempts = 0;
			while (p >= 0 && length < 100 && failedAttempts < 15) {
				if (data.available() > 0) {
					p = data.read();
					buildingDatabase += (char) p;
					length++;
					failedAttempts = 0;
				} else {
					try {
						Thread.sleep(20);
					} catch (Exception e) {
					}
					failedAttempts++;
				}
			}
			System.out.println("RobotVision message received:\n\t"
					+ length + "/100 , " + failedAttempts + "/15 failed");
			data.close();
			http.close();

			database = buildingDatabase;

		} catch (Exception e) {
			//System.out.println("Exception in RobotVision.retrieve() (networking):");
			//System.out.println("\t" + e);
			//System.out.println("\t" + e.getMessage());
		}
		try {
			data.close();
		} catch (Exception e) {
			//System.out.println("Error Closing Data: " + e);
		}
		try {

			http.close();
		} catch (Exception e) {
			//System.out.println("Error Closing HTTP: " + e);
		}
		if (connectionFailure) {
			double t = timer.get();
			System.gc();
			try {
				Thread.sleep(30000);
			} catch (Exception e) {
			}
		}
	}
}
