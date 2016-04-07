package vehicle.common;

import logs.logger.PegasusLogger;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleParams.VehicleControlType;
import vehicle.common.constants.VehicleState;
import control.Constants.ApplicationStates;
import control.Interfaces.IVehicleActionsListener;

public abstract class AbstractVehicle {
	private static final String TAG = "Abstract Vehicle";
	protected static AbstractVehicle mInstance;
	
	
	protected String mName;
	protected String mID;
	protected double mSteeringAngle;
	protected DrivingDirection mCurrentDrivingDirection;
	protected VehicleControlType mVehicleControlType;
	protected double mCurrentSpeed;
	protected double mDistance;
	protected int mCurrentState;
	
	
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
	
	/**
	 * handle looking for parking action
	 * @param parkingType parking type(parallel, angular etc..)
	 */
	public abstract void findParkingSpot(int parkingType);
	
	
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
			PegasusLogger.getInstance().d(TAG,"setState", "State was "
					+ VehicleState.getVehicleStateName(mCurrentState)
					+ " and changed to:" + ApplicationStates.getStateName(aState));
			mCurrentState = aState;
		}
	}
	
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
