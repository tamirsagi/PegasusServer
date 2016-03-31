package vehicle;

import control.Interfaces.IVehicleActionsListener;

public abstract class AbstractVehicle {
	private static final String TAG = "Abstract Vehicle";
	protected static AbstractVehicle mInstance;
	
	protected String mName;
	protected String mID;
	protected double mSteeringAngle;
	protected VehicleParams.DrivingDirection mCurrentDrivingDirection;
	protected double mCurrentSpeed;
	protected double mDistance;
	
	
	public static AbstractVehicle getInstance() throws Exception{
		if(mInstance == null){
			throw new Exception("Vehcile Instance has not initialized yet");
		}
		return mInstance;
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getID() {
		return mID;
	}

	public void setID(String mID) {
		this.mID = mID;
	}
	
	/**
	 * config vehicle parameters;(length, width etc..)
	 */
	public abstract void setVehicleData();
	
	/**
	 * Register Listener
	 */
	public abstract void registerVehicleActionsListener(IVehicleActionsListener listener);
	
	/**
	 * Handle the vehicle speed
	 * @param digitalSpeed
	 */
	public abstract void changeSpeed(int digitalSpeed);
	
	
	/**
	 * turn the vehicle right with a steering angle
	 * @param rotatioAngle
	 */
	public abstract void turnRight(double rotationAngle);
	
	/**
	 * turn the vehicle left with a steering angle
	 * @param rotatioAngle
	 */
	public abstract void turnLeft(double rotationAngle);
	
	/**
	 * set driving direction Forward
	 */
	public abstract void driveForward();
	
	/**
	 * set driving direction backward
	 */
	public abstract void driveBackward();
	
	/**
	 * stop the vehicle
	 */
	public abstract void stop();
	
	
	public abstract Object getVehicleData();
	

	@Override
	public String toString() {
		return "Vehicle [mName=" + mName + ", mID=" + mID + " " + getVehicleData().toString() + "]";
	}
	
	
	
}
