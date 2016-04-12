package vehicle.common;

import logs.logger.PegasusLogger;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleParams.VehicleControlType;
import vehicle.common.constants.VehicleState;
import control.interfaces.OnVehicleEventsListener;

public abstract class AbstractVehicle {
	
	private static final String TAG = AbstractVehicle.class.getSimpleName();
	
	
	protected String mName;
	protected String mID;
	protected double mSteeringAngle;
	protected DrivingDirection mCurrentDrivingDirection;
	protected VehicleControlType mVehicleControlType;
	protected double mCurrentSpeed;
	protected double mDistance;
	protected int mCurrentState = VehicleState.VEHICLE_DEFAULT;
	protected OnVehicleEventsListener mListener; 
	private boolean mIsReady;
	private VehicleData mVehicleData;
	
	
	/**
	 * is used after insance first created.
	 * @param aListener
	 */
	public void notifyWhenReady(OnVehicleEventsListener aListener){
		if(aListener != null && !mIsReady){
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
	
	public void setControlType(VehicleControlType aControlType){
		mVehicleControlType = aControlType;
	}
	
	public VehicleControlType getVehicleControlType(){
		return mVehicleControlType;
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
		return mCurrentState;
	}
	
	public void setCurrentState(int aState){
		if(aState != mCurrentState){
			PegasusLogger.getInstance().d(TAG,"setCurrentState", "State was "
					+ VehicleState.getVehicleStateName(mCurrentState)
					+ " and changed to:" + VehicleState.getVehicleStateName(aState));
			mCurrentState = aState;
			changeUltraSonicSensorState();
		}
	}
	
	public abstract void changeUltraSonicSensorState();
	
	public double getTravelledDistance(){return mDistance;}
	
	/**
	 * keep the total travelled distance the vehicle is doing
	 * @param aDist
	 */
	public void setTravelledDistance(double aDist){
			mDistance = aDist;
	}
	
	public String getTag(){return TAG;}

	@Override
	public String toString() {
		return "Vehicle [mName=" + mName + ", mID=" + mID + "]";
	}
	
	
	
}
