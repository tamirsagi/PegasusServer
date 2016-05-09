package vehicle.interfaces;

import vehicle.common.VehicleData;


public interface OnManagedVechile {

	public VehicleData getVehicleData();
	
	public void stop();
	
	public void startNormalDriving();
	
	public void changeSpeed(int digitalSpeed);

	public void turnRight(double rotationAngle);

	public void turnLeft(double rotationAngle);

	public void driveForward();

	public void driveBackward();
	
	public int getCurrentState();
	
	public void setCurrentState(int aState);
	
	public int getSpeed(); 
	
	public double getInterruptsCounterOfWheelSensor();
	
	public double getValueFromDistanceSensor(String pos);
	
	public int getMaxServoRightAngle();
	
	public int getMaxServoLeftAngle();
	
}
