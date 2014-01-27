/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.templates.RobotSensors;
import edu.wpi.first.wpilibj.templates.RobotActuators;

/**
 *
 * @author Robin Onsay
 */
public class RobotDrive {
////VARIABLES-------------------------------------------------------------------	
	private Pneumatics shift;
	private Timer time;
	public static double targetDistance;
	public static double distance;
////CONSTANTS-------------------------------------------------------------------
	public static final double WHEEL_DIAMETER = 0.1624;
	public static final double NO_SPEED = 0.0;
	public static final double MAX_SPEED = 1.0;
	public static final double MIN_SPEED = -1.0;		
////INIT------------------------------------------------------------------------
	public void init(){
		RobotSensors.RIGHT_DRIVE_ENCODER.start();
		RobotSensors.LEFT_DRIVE_ENCODER.start();
	}
////CONSTRUCTOR-----------------------------------------------------------------
	public void Drive(){
		shift = new Pneumatics();
	}
	
////SUBCLASS--------------------------------------------------------------------
	public class DistanceCorrection{
////CLASS METHODS---------------------------------------------------------------
		double difference = targetDistance - distance;
		
		public double setTargetDistance(double target){
			targetDistance = target;		
			return targetDistance;
		}
		public double setDistance(double dist){
			distance = dist;
			return distance;
		}		
		public double setDistancePerTick(double wheelDiameterM /*in meters*/){
			final double CIRC = wheelDiameterM * Math.PI;
			double distPerTick = CIRC/360;
			RobotSensors.RIGHT_DRIVE_ENCODER.setDistancePerPulse(distPerTick);
			RobotSensors.LEFT_DRIVE_ENCODER.setDistancePerPulse(distPerTick);			
			double RequiredTicks = difference * (1/distPerTick);
			return RequiredTicks;
		}		
		double[] convertToTime(){
			double[] speed = {RobotSensors.RIGHT_DRIVE_ENCODER.getRate(), RobotSensors.LEFT_DRIVE_ENCODER.getRate()};
			double[] time = new double[2]; 
			for(int i = 0; i < speed.length; i++){
				time[i]= (1/speed[i]) * this.setDistancePerTick(WHEEL_DIAMETER);
			}
			return time;
		}
		public void correct(double speed){
			double[] targetTime = this.convertToTime();
			time.start();
			while(time.get() != targetTime[0] || time.get() != targetTime[1]){
				RobotActuators.rightDrive.set(speed);
				RobotActuators.leftDrive.set(speed);
			}
			RobotActuators.rightDrive.set(NO_SPEED);
			RobotActuators.leftDrive.set(NO_SPEED);
		}
	}
	
	
	public class DriveTele{
		public void shift( boolean bttn1, boolean bttn2){
			if(bttn1){
				shift.shiftIt(true);//Shift to high gear
			}else if(bttn2){
				shift.shiftIt(false);//Shift to low gear
			}	
		}
		public void drive(double axisTrigger, double leftJoy) {           
            //Robin, plz help, I need your wisdom!		
		double turnRightVic = -axisTrigger - leftJoy;
		double turnLeftVic = axisTrigger + leftJoy;	
		
		this.speedLimiter(turnRightVic);
		this.speedLimiter(turnLeftVic);			
		
		RobotActuators.rightDrive.set(turnRightVic);		
		RobotActuators.leftDrive.set(turnLeftVic); 
		}        
        public double speedLimiter(double trigger) {
			if(trigger < MIN_SPEED){
				trigger = MIN_SPEED;
			}
			if(trigger< MAX_SPEED){
				trigger = MAX_SPEED;
			}
			return trigger;
			}
	}
}
