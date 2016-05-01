package managers.driving_manager;

import org.json.JSONException;
import org.json.JSONObject;

import util.CameraManager;
import vehicle.common.ActionTimer;
import vehicle.common.VehicleData;
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
	
	//Parking Search Params
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 60 * 1000 * 2; // 2 minutes for searching
	
	private static final int DEFAULT_VALUE = -1;
	
	private static final String KEY_SHOULD_ADD_TRAVELLED_DISTANCE = "should_add_travelled_distance";
	private static final String KEY_LAST_SENSOR_DATA = "last_Sensor_data";
	private static final String KEY_DISTANCE_SINCE_STARTED = "distance_since_started";
	
	private int mParkingType;
	private boolean mFound;
	private double mRelevantSpace = DEFAULT_VALUE;
	private OnParkingEventsListener mListener;
	private ActionTimer mTimer;
	private double mMinSpace;
	private JSONObject mCurrentParkingProcessParams;
	
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

	@Override
	public void updateInput(int sensorId, double value){
		resumeThread();
		switch(SensorPositions.getSensorPosition(sensorId)){
		case SensorPositions.FRONT_ULTRA_SONIC_SENSOR:
			handleFrontSensorData(value);
			break;
		}
		suspendThread();
	}
	
	/**
	 * Method handles front sensor data
	 * @param aValue
	 */
	public synchronized void handleFrontSensorData(double aValue){
		if(aValue >= 0){
			PegasusLogger.getInstance().i(getName(),"Front Sensor Data : " + aValue);
			switch(mCurrentMode){
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING:
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING:
				if(0 < aValue && aValue <= MAX_DISTANCE_TO_STOP){
					if(mManagedVehicle.getSpeed() != 0){
						mManagedVehicle.stop();
					}
				}else{
					follow();
					if(mManagedVehicle.getSpeed() == 0){
						mManagedVehicle.startNormalDriving();
					}
				}
				
				break;
				
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING:
				break;
			}
		}
	}
	
	
	/**
	 * handles data from infra red sensor
	 * @param value - round of wheel per second
	 */
	public void handleTachometerData(double aValue){
		if(aValue >= 0){
			
		}
	}
	
	/**
	 * set current autonomous mode;
	 * @param aMode autonomouse mode (Free driving, parking spot searching , manoeuvring)
	 */
	public void setCurrentMode(int aMode){
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
			}
		}
	}
	
	/**
	 * handle free driving
	 */
	public void freeDrive(){
		
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		mManagedVehicle.startNormalDriving();
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
	}
	
	/**
	 * Method resumes parking finder thread
	 * @param parkingType
	 * @param aMinSpace
	 */
	public void findParkingSpot(int parkingType){
		mCurrentParkingProcessParams = new JSONObject();
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING);
		VehicleData vehicleData = mManagedVehicle.getVehicleData();
		double aMinSpace = 0;
		switch(parkingType){
		case ParkingType.PARALLEL_RIGHT:
		case ParkingType.PARALLEL_LEFT:
			aMinSpace = vehicleData.getLength() + 
						VehicleData.MIN_REQUIRED_DISTANCE_SAFE_FACTOR * vehicleData.getMinimumRequiredSpaceToPark();
			break;
		}
		if(aMinSpace > 0){
			PegasusLogger.getInstance().i(getTag(), "findParking", "started looking for parking with min space of: " + aMinSpace);
			mMinSpace = aMinSpace;
			mParkingType = parkingType;
			mFound = false;
			mRelevantSpace = 0;
			if(mTimer != null){
				mTimer.killThread();
			}
			mTimer = new ActionTimer(FINDING_PARKING_SPOT_TIMEOUT, this);
			mTimer.startTimer();
		}else{
			PegasusLogger.getInstance().i(getName(),"findParkingSpot", "Could not start looking min space is 0");
			setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
			mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		}
	}
	
	/**
	 * Methods handles incoming data for right parallel parking searching
	 * @param incomingData
	 */
	private void handleIncomingDataOnParallelParking(int aSensorId, double aValue){
		try {
			if(!mFound){
				if(aSensorId == SensorPositions.INFRA_RED_TACHOMETER_ID){
					boolean relevantDistance = mCurrentParkingProcessParams.optBoolean(KEY_SHOULD_ADD_TRAVELLED_DISTANCE,true);
					if(relevantDistance){
						mRelevantSpace += aValue;
					}
				}else{
					mCurrentParkingProcessParams.put(KEY_LAST_SENSOR_DATA,aValue);
					if(aValue != 0){
						mRelevantSpace = 0;
						mCurrentParkingProcessParams.put(KEY_SHOULD_ADD_TRAVELLED_DISTANCE,false);
						
					}else{
						mCurrentParkingProcessParams.put(KEY_SHOULD_ADD_TRAVELLED_DISTANCE,true);
					}
				}
				mCurrentParkingProcessParams.put(KEY_DISTANCE_SINCE_STARTED, mRelevantSpace);
				if(mRelevantSpace >= mMinSpace){
					mFound = true;
					if(mTimer != null){
						mTimer.killThread();
					}
					onParkingFound();
				}
			}
		}catch (JSONException e) {
					e.printStackTrace();
				}
		
	}
	
	/**
	 * methods use roof camera to follow lane while in motion
	 */
	public void follow(){
		
		
	}
	

	////////////////////////////////////////Parking Finder events \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	
	private void onParkingFound() {
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING_FOUND);
		PegasusLogger.getInstance().i(getName(), "Parking found stopping car....");
		mManagedVehicle.stop();
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING_FOUND);
		//TODO - handle parking manoeuvring
	}
	
	public int getParkingType(){
		return mParkingType;
	}
	
	@Override
	public void onTimerIsOver() {
		mFound = false;
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
	}
	

}
