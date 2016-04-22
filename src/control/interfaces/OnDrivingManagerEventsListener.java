package control.interfaces;

import vehicle.common.constants.VehicleParams;

public interface OnDrivingManagerEventsListener {
	
	/**
	 * driving manager stops the car
	 */
	void onStop();
	
	/*
	 *continue driving with given speed 
	 */
	void onResumeDriving(double aSpeed);
	
	/**
	 * turn right according to lane following
	 * @param angle 
	 */
	void onTurnRight(double aAngle);
	
	/**
	 * turn left according to lane following
	 * @param angle
	 */
	void onTurnLeft(double aAngle);
	
	/*
	 * change to forward or backward respectevly 
	 * @param aDirection
	 */
	void onChangeDirection(VehicleParams.DrivingDirection aDirection);
	
}