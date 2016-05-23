package vehicle.common;

import logs.logger.PegasusLogger;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleAutonomousMode;
import control.interfaces.OnVehicleEventsListener;

public abstract class AbstractVehicle {
	
	private static final String TAG = AbstractVehicle.class.getSimpleName();
	
	
	protected String mName;
	protected String mID;
	protected double mSteeringAngle;
	protected int mCurrentDrivingDirection = VehicleParams.FORWARD;
	protected int mVehicleMode = VehicleParams.VEHICLE_MODE_NONE;
	protected double mCurrentSpeed;
	protected double mDistance;
	protected int mAutonomousMode = VehicleAutonomousMode.VEHICLE_NONE;
	protected OnVehicleEventsListener mListener; 
	private boolean mIsReady;
	private VehicleData mVehicleData;
	
	
	/**
	 * is used after insance first created.
	 * @param aListener
	 */
	public void notifyWhenReady(OnVehicleEventsListener aListener){
		if(aListener != null && !mIsReady){
			PegasusLogger.getInstance().i("notifyWhenReady", "Vehicle is ready");
			mIsReady = true;
			registerVehicleActionsListener(aListener);
			aListener.onVehicleStateChanged(true);
		}
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
	
	public void setControlType(int aVehicleMode){
		mVehicleMode = aVehicleMode;
	}
	
	public int getVehicleControlType(){
		return mVehicleMode;
	}
	
	/**
	 * config vehicle parameters;(length, width etc..)
	 */
	public abstract void setVehicleData();
	
	
	public VehicleData getVehicleData(){
		return mVehicleData;
	}
	
	/**
	 * Register Listener
	 */
	public void registerVehicleActionsListener(OnVehicleEventsListener aListener){
		if(aListener != null){
			mListener = aListener;
		}
	}
	
	/**
	 * register sensors to data provider
	 */
	public abstract void registerAllSensorToDataProvider();
	
	/**
	 * 
	 */
	public abstract void startNormalDriving();
	
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
	 * change driving direction
	 * @param aDrivingDirection 1:Forward, 2: Backward
	 */
	public abstract void changeDrivingDirection(int aDrivingDirection);
	
	
	/**
	 * stop the vehicle
	 */
	public abstract void stop();
	

	public double getCurrentSpeed(){
		return mCurrentSpeed;
	}
	
	public void setCurrentspeed(double aValue){
		if(aValue > 0){
			mCurrentSpeed = aValue;
		}else{
			mCurrentSpeed = 0;
		}
	}
	
	public int getCurrentState(){
		return mAutonomousMode;
	}
	
	public void setCurrentState(int aState){
		if(aState != mAutonomousMode){
			PegasusLogger.getInstance().d(TAG,"setCurrentState", "State was "
					+ VehicleAutonomousMode.getVehicleStateName(mAutonomousMode)
					+ " and changed to:" + VehicleAutonomousMode.getVehicleStateName(aState));
			mAutonomousMode = aState;
		}
	}
	
	/**
	 * send sensor configuration file to Hardware unit
	 * Configuration are loaded from configuration file
	 */
	public abstract void sendSensorConfiguration();
	
	
	public double getTravelledDistance(){return mDistance;}
	
	/**
	 * keep the total travelled distance the vehicle is doing
	 * @param aDist
	 */
	public void setTravelledDistance(double aDist){
			mDistance = aDist;
	}
	
	public void setDrivingDirection(int aDrivingDirection){
		if(aDrivingDirection == VehicleParams.FORWARD || aDrivingDirection == VehicleParams.BACKWARD){
			mCurrentDrivingDirection = aDrivingDirection;
		}
	}
	
	public String getTag(){return TAG;}

	@Override
	public String toString() {
		return "Vehicle [mName=" + mName + ", mID=" + mID + "]";
	}
	
	
	
}
