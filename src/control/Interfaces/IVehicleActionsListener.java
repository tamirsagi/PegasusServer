package control.Interfaces;

public interface IVehicleActionsListener {

	/**
	 * change vehicle's speed
	 * @param digitalSpeed (0-255) on Arduino
	 */
	void changeSpeed(int digitalSpeed);
	
	/**
	 * Turn Vehicle Right, Angle is 0-40
	 * @param rotationAngle
	 */
	void turnRight(double rotationAngle);
	
	/**
	 * Turn Vehicle Left, Angle is 0-40
	 * @param rotationAngle
	 */
	void turnLeft(double rotationAngle);
	
	/**
	 * Change Driving Direction Forward
	 */
	void driveForward();
	
	/**
	 * Change Driving Direction Backward
	 */
	void driveBackward();
	
	/**
	 * stop the vehicle
	 */
	void stop();
	
	/**
	 * find parking spot
	 * @param parkingType type of parking spot
	 */
	void findParkingSpot(int parkingType);
	
	
	
}
