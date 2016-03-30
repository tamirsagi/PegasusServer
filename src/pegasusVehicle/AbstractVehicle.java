package pegasusVehicle;

import pegasusVehicle.params.VehicleParams;
import control.Interfaces.IVehicleActionsListener;

public abstract class AbstractVehicle {
	private static final String TAG = "Abstract Vehicle";
	protected static AbstractVehicle mInstance;
	
	protected String mName;
	protected int mID;
	protected double mVehicleLength;
	protected double mVehicleWidth;
	protected double mTurningRadious;
	protected double mWheelRadius;
	protected double mCurrentSpeed;
	protected VehicleParams.DrivingDirection mCurrentDrivingDirection;
	protected double mSteeringAngle;
	
	
	public static AbstractVehicle getInstance() throws Exception{
		if(mInstance == null){
			throw new Exception("Vehcile Instance has not initialized yet");
		}
		
		return mInstance;
	}
	
	public double getmVehicleLength() {
		return mVehicleLength;
	}

	public void setmVehicleLength(double mVehicleLength) {
		this.mVehicleLength = mVehicleLength;
	}

	public double getmVehicleWidth() {
		return mVehicleWidth;
	}

	public void setVehicleWidth(double mVehicleWidth) {
		this.mVehicleWidth = mVehicleWidth;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public int getID() {
		return mID;
	}

	public void setID(int mID) {
		this.mID = mID;
	}
	
	public double getRadious(){
		return mTurningRadious;
	}
	
	public void setRadious(double radious){
		mTurningRadious = radious;
	}
	
	/**
	 * Set Ultra Sonic sensors
	 */
	public abstract void setUltraSonicSensors();
	
	
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

	@Override
	public String toString() {
		return "Vehicle [mName=" + mName + ", mID=" + mID + ", mVehicleLength="
				+ mVehicleLength + ", mVehicleWidth=" + mVehicleWidth
				+ ", mRadious=" + mTurningRadious + "]";
	}
	
	
	
}
