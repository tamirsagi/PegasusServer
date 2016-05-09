package managers.driving_manager;

import org.json.JSONException;
import org.json.JSONObject;

import util.CameraManager;
import vehicle.common.ActionTimer;
import vehicle.common.VehicleData;
import vehicle.common.constants.ParkingStates;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleAutonomousMode;
import vehicle.interfaces.OnManagedVechile;
import vehicle.interfaces.OnTimerListener;
import vehicle.pegasus.PegasusVehicleData;
import vehicle.pegasus.constants.SensorPositions;
import control.interfaces.OnParkingEventsListener;
import logs.logger.PegasusLogger;
import managers.common.AbstractManager;
import managers.finder.constants.ParkingType;

/**
 * class is responsible for obstacles avoidance and lane following
 * @author Tamir Sagi
 *
 */
public class DrivingManager extends AbstractManager  implements OnTimerListener{

	private static DrivingManager mInstance;
	private static final int MAX_DISTANCE_TO_STOP = 6; // 6 cm is the max distance to stop when detecting an obstacles
	
	private int mCurrentSpeed;
	private VehicleParams.DrivingDirection mCurrentDirection;
	private int mCurrentMode = VehicleAutonomousMode.VEHICLE_NONE;
	private OnManagedVechile mManagedVehicle;
	
	//	Parking Search Params \\
	
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 60 * 1000 * 2; // 2 minutes for searching
	
	private int mParkingType;
	private int mParkingState;
	private boolean mFound;
	private double mMaxWheelSensorInterrupts;
	
	// Parking Manouevering \\
	
	
	private ActionTimer mTimer;
	private double mCurrentWheelSensorIntterupts;
	
	public static DrivingManager getInstance(){
		if(mInstance == null){
			mInstance = new DrivingManager(DrivingManager.class.getSimpleName());
		}
		return mInstance;
	}
	
	private DrivingManager(String aTag){
		super(aTag);
		mCurrentDirection = DrivingDirection.FORWARD;
	}
	
	/**
	 * register listener
	 * @param aListener
	 */
	public void registerListener(OnManagedVechile aManagedVehicle){
			mManagedVehicle = aManagedVehicle;
	}

	@Override
	public void run() {
		PegasusLogger.getInstance().i(getName(),"Driving Manager has been started...");
		while(mIsworking){
			synchronized (this) {
				try{
					while(mIsSuspended){
						wait();
					}
					handleCurrentState();
					
				}catch(InterruptedException e){
					PegasusLogger.getInstance().e(getTag(),e.getMessage());
				}
			}
		}
	}
	
	
	@Override
	public void startThread() {
		PegasusLogger.getInstance().i(getName(),"starting driving manager thread...");
		super.startThread();
	}

	@Override
	public void stopThread() {
		super.stopThread();
	}

	@Override
	public void suspendThread() {
		if(!mIsSuspended){
			PegasusLogger.getInstance().i(getName(),"suspended...");
			super.suspendThread();
		}
	}
	
	@Override
	public synchronized void resumeThread() {
		if(mIsSuspended){
			PegasusLogger.getInstance().i(getName(),"resumed...");
			super.resumeThread();
		}
	}
	
	/**
	 * method handle current State
	 */
	private synchronized void handleCurrentState(){
		if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING ||
				mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING){
			
			double frontSensorValue = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.FRONT_ULTRA_SONIC_SENSOR);
			double tachometerValue = mManagedVehicle.getInterruptsCounterOfWheelSensor();
			handleFrontSensorData(frontSensorValue);
			
			if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING){
				if(tachometerValue >= 0){
					mCurrentWheelSensorIntterupts += tachometerValue;
					PegasusLogger.getInstance().d(getName(),"Current intterupts:" + mCurrentWheelSensorIntterupts);
				}
				if(isParallelParking()){
					handleParallelParkingSearching();
				}
			}else if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_POSITIONING_VEHICLE_PARALLEL){
				checkVehiclePositionBeforeParkingParallel();
				if(mManagedVehicle.getSpeed() == 0){
					mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_READY_TO_PARK);
					setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_READY_TO_PARK);
				}
			}
			
		}
	}

	/**
	 * Method handles front sensor data
	 * @param aValue
	 */
	public synchronized void handleFrontSensorData(double aValue){
		if(aValue >= 0){
			PegasusLogger.getInstance().i(getName(),"Front Sensor Data : " + aValue);
				if(0 < aValue && aValue <= MAX_DISTANCE_TO_STOP){
					if(mManagedVehicle.getSpeed() != 0){
						mManagedVehicle.stop();
					}
				}else{
					if(mManagedVehicle.getSpeed() == 0){
						mManagedVehicle.startNormalDriving();
					}
					follow();
				}
			}
	}
	
	
	/**
	 * set current autonomous mode;
	 * @param aMode autonomous mode (Free driving, parking spot searching , manoeuvring)
	 */
	public void setMode(int aMode){
		if(aMode != mCurrentMode){
			PegasusLogger.getInstance().i(getName(), "Changing autonomous Mode From " + mCurrentMode + " to:" + aMode);
			mCurrentMode = aMode;
			switch(mCurrentMode){
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING:
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING:
				if(mManagedVehicle.getSpeed() == 0 ){
					mManagedVehicle.startNormalDriving();
				}
				break;
			
			default:
				suspendThread();
			}
		}
	}
	
	/**
	 * handle free driving
	 */
	public void freeDrive(){
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		DrivingManager.getInstance().setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		resumeThread();
	}
	
	/**
	 * Method resumes parking finder thread
	 * @param parkingType
	 * @param aMinSpace
	 */
	public void findParkingSpot(int parkingType){
		mParkingState = ParkingStates.PARKING_DEFAULT;
		VehicleData vehicleData = mManagedVehicle.getVehicleData();
		double minSpaceToPark = 0;
		switch(parkingType){
		case ParkingType.PARALLEL_RIGHT:
		case ParkingType.PARALLEL_LEFT:
			PegasusLogger.getInstance().d(getName(), "findParkingSpot","parking type : " +  ParkingType.getParkingTypeName(parkingType));
			minSpaceToPark = vehicleData.getLength() + 
						VehicleData.MIN_REQUIRED_DISTANCE_SAFE_FACTOR * vehicleData.getMinExtraSpaceOnParallelParking();
			break;
		}
		if(minSpaceToPark > 0){
			mMaxWheelSensorInterrupts = (vehicleData.getNumberOfWheelSlots() * minSpaceToPark) / vehicleData.getWheelPerimeter();
			mParkingType = parkingType;
			mFound = false;
			if(mTimer != null){
				mTimer.killThread();
			}
			mTimer = new ActionTimer(FINDING_PARKING_SPOT_TIMEOUT, this);
			mTimer.startTimer();
			mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING);
			setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING);
			PegasusLogger.getInstance().i(getName(), "findParking", "started looking for parking with min space of: " + minSpaceToPark 
					+ "Max Number of slots : " + mMaxWheelSensorInterrupts);
		}else{
			PegasusLogger.getInstance().i(getName(),"findParkingSpot", "Could not start looking min space is 0");
			setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
			mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		}
		resumeThread();
	}
	
	/**
	 * Methods handles incoming data for parallel parking searching
	 * @param incomingData
	 */
	private void handleParallelParkingSearching() {
		if (!mFound) {
			double relevantSensorValue = -1;
			if (mParkingType == ParkingType.PARALLEL_RIGHT) {
				relevantSensorValue = mManagedVehicle
						.getValueFromDistanceSensor(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR);
			} else {
				relevantSensorValue = mManagedVehicle
						.getValueFromDistanceSensor(SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR);

				if (relevantSensorValue != 0) {
					mCurrentWheelSensorIntterupts = 0;
				}
				if (mCurrentWheelSensorIntterupts >= mMaxWheelSensorInterrupts) {
					mFound = true;
					if (mTimer != null) {
						mTimer.killThread();
					}
					onParkingFound();
				}
			}
		}
	}
	
	/**
	 * in case the vehicle is next to another car we would like to place the back wheels same line with the parked car
	 */
	private void checkVehiclePositionBeforeParkingParallel(){
		double BackSideSensorValue = -1;
		switch(mParkingType){
		case ParkingType.PARALLEL_RIGHT:
			BackSideSensorValue = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR);
			break;
		case ParkingType.PARALLEL_LEFT:
			BackSideSensorValue = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR);
			break;
		}
		PegasusLogger.getInstance().i(getName(),"checkVehiclePositionBeforeParking", "placing the veicle: " + BackSideSensorValue);
		if(BackSideSensorValue > 0){
			mManagedVehicle.stop();
		}
	}
	
	/**
	 * calculate relvant arch prior executing parking 
	 */
	private void prepareManuevering(){
		//TODO - handle parallel
		VehicleData vehicleData = mManagedVehicle.getVehicleData();
		double backRightSensor = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR);
		double relevantDistance = backRightSensor + vehicleData.getWidth();
	}
	
	
	/**
	 * methods use roof camera to follow lane while in motion
	 */
	private void follow(){
		
		
	}
	
	/**
	 * stop driving manager
	 */
	public void stopDrivingManager(){
		mManagedVehicle.stop();
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_NONE);
		setMode(VehicleAutonomousMode.VEHICLE_NONE);
	}
	

	////////////////////////////////////////Parking Finder events \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	
	private void onParkingFound() {
		mParkingState = ParkingStates.PARKING_FOUND;
		PegasusLogger.getInstance().i(getName(), "Parking found stopping car with num of interrupts: " + mCurrentWheelSensorIntterupts);
		if(isParallelParking()){
			double frontSideSensor;
			if(mParkingType == ParkingType.PARALLEL_RIGHT){
			 frontSideSensor = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.FRONT_RIGHT_ULTRA_SONIC_SENSOR);
			}else{
				frontSideSensor = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.FRONT_LEFT_ULTRA_SONIC_SENSOR);
			}
			if(frontSideSensor != 0){
				mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_POSITIONING_VEHICLE_PARALLEL);
				setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_POSITIONING_VEHICLE_PARALLEL);
			}else {
				mManagedVehicle.stop();
				mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_READY_TO_PARK);
				setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_READY_TO_PARK);
			}
			
		}
		
	}
	
	public int getParkingType(){
		return mParkingType;
	}
	
	public boolean isParallelParking(){
		return mParkingType == ParkingType.PARALLEL_RIGHT ||
				mParkingType == ParkingType.PARALLEL_LEFT;
	}
	
	@Override
	public void onTimerIsOver() {
		mFound = false;
		mParkingState = ParkingStates.PARKING_NOT_FOUND;
		setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
	}

	

}
