package vehicle.interfaces;

import vehicle.common.VehicleData;


public interface OnManagedVechile {

	public VehicleData getVehicleData();
	
	public void stop();
	
	public void startNormalDriving();
	
	public void changeSpeed(int digitalSpeed);

	public void turnRight(double rotationAngle);

	public void turnLeft(double rotationAngle);
	
	/**
	 * 
	 * @param aDrivingDirection  1 - FORWARD, 2 - BACKWARD
	 */
	public void changeDrivingDirection(int aDrivingDirection);

	public int getCurrentState();
	
	public void setCurrentState(int aState);
	
	public int getSpeed(); 
	
	public double getInterruptsCounterOfWheelSensor();
	
	public double getValueFromDistanceSensor(String pos);
	
	public int getMaxServoRightAngle();
	
	public int getMaxServoLeftAngle();
	
	public int getStraightServoAngle();
	
	public void resetInterruptsCounter();
	
}
