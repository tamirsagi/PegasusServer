package managers.driving_manager;

import util.CameraManager;
import vehicle.common.VehicleData;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleAutonomousMode;
import vehicle.interfaces.OnManagedVechile;
import vehicle.pegasus.PegasusVehicle;
import vehicle.pegasus.constants.SensorPositions;
import control.interfaces.OnDrivingManagerEventsListener;
import control.interfaces.OnParkingEventsListener;
import logs.logger.PegasusLogger;
import managers.common.AbstractManager;
import managers.finder.ParkingFinder;
import managers.finder.constants.ParkingType;

/**
 * class is responsible for obstacles avoidance and lane following
 * @author Tamir Sagi
 *
 */
public class DrivingManager extends AbstractManager  implements OnParkingEventsListener{

	private static DrivingManager mInstance;
	private LaneFollowingService mLaneFollowingService;
	private static final int MAX_DISTANCE_TO_STOP = 6; // 6 cm is the max distance to stop when detecting an obstacles
	
	private int mCurrentSpeed;
	private VehicleParams.DrivingDirection mCurrentDirection;
	private int mCurrentMode = VehicleAutonomousMode.VEHICLE_NONE;
	private OnManagedVechile mManagedVehicle;
	
	public static DrivingManager getInstance(){
		if(mInstance == null){
			mInstance = new DrivingManager(DrivingManager.class.getSimpleName());
		}
		return mInstance;
	}
	
	private DrivingManager(String aTag){
		super(aTag);
		mCurrentDirection = DrivingDirection.FORWARD;
		mLaneFollowingService = new LaneFollowingService();
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
		if(mLaneFollowingService != null){
			mLaneFollowingService.startService();
		}
	}

	@Override
	public void stopThread() {
		super.stopThread();
		if(mLaneFollowingService != null){
			mLaneFollowingService.stopService();
		}
		ParkingFinder.getInstance().stopThread();
	}

	@Override
	public void suspendThread() {
		if(!mIsSuspended){
			if(mLaneFollowingService != null && !mLaneFollowingService.mIsServiceSuspended){
				mLaneFollowingService.suspendService();
			}
			if(!ParkingFinder.getInstance().isThreadSuspended()){
				ParkingFinder.getInstance().suspendThread();
			}
			super.suspendThread();
		}
	}
	
	@Override
	public synchronized void resumeThread() {
		if(mIsSuspended){
			super.resumeThread();
		}
	}

	@Override
	public void updateInput(int sensorId, double value){
		switch(SensorPositions.getSensorPosition(sensorId)){
		case SensorPositions.FRONT_ULTRA_SONIC_SENSOR:
			handleFrontSensorData(value);
			break;
			
		}
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
				if(aValue <= MAX_DISTANCE_TO_STOP){
					if(mManagedVehicle.getSpeed() != 0){
						mManagedVehicle.stop();
					}
					if(!mLaneFollowingService.isServiceSuspended()){
						mLaneFollowingService.suspendService();
					}
					if(!ParkingFinder.getInstance().isThreadSuspended()){
						ParkingFinder.getInstance().suspendThread();
					}
				}else{
					if(mManagedVehicle.getSpeed() == 0){
						mManagedVehicle.startNormalDriving();
					}
					if(mLaneFollowingService.isServiceSuspended()){
						mLaneFollowingService.resumeService();
					}
					if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING &&
							ParkingFinder.getInstance().isThreadSuspended()	){
						ParkingFinder.getInstance().resumeThread();
					}
				}
				
				break;
				
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING:
				break;
			}
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
				initialization();
				break;
			}
		}
	}
	
	/**
	 * start relevant functionalities 
	 */
	private void initialization(){
		if(mLaneFollowingService == null){
			mLaneFollowingService = new LaneFollowingService();
			mLaneFollowingService.startService();
		}else if(!mLaneFollowingService.isAlive()){
			mLaneFollowingService.startService();
		}else if(mLaneFollowingService.mIsServiceSuspended){
			mLaneFollowingService.resumeService();
		}
		if(mManagedVehicle.getSpeed() == 0 ){
			mManagedVehicle.startNormalDriving();
		}
		if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING){
			if(!ParkingFinder.getInstance().isAlive()){
				ParkingFinder.getInstance().registerParkingEventsListner(this);
				ParkingFinder.getInstance().startThread();
				while(!ParkingFinder.getInstance().isAlive());
				ParkingFinder.getInstance().suspendThread();
			}
		}else if(ParkingFinder.getInstance().isAlive() &&
				!ParkingFinder.getInstance().isThreadSuspended()){
			ParkingFinder.getInstance().suspendThread();
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
		ParkingFinder.getInstance().searchParking(parkingType, aMinSpace);
		ParkingFinder.getInstance().resumeThread();
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING);
		
	}
	
	
	/**
	 * class handles lane following
	 * @author Tamir
	 *
	 */
	private class LaneFollowingService extends Thread{
		private final String TAG = LaneFollowingService.class.getSimpleName();
		private boolean mIsServiceSuspended;
		private boolean mIsSerivceRunning;
		
		public LaneFollowingService(){
			setName(TAG);
			CameraManager.getInstance().turnCameraOff();
		}
		
		@Override
		public void run() {
			PegasusLogger.getInstance().i(getName(),"run()", "lane following service has been started...");
			//CameraManager.getInstance().turnCameraOn();
			while(mIsSerivceRunning){
				
				synchronized (this) {
					try{
						while(mIsServiceSuspended){
							wait();
						}
						follow();	
					}catch(InterruptedException e){
						PegasusLogger.getInstance().e(getTag(),e.getMessage());
					}
				}
			}
			PegasusLogger.getInstance().i(getName(), "Lane following Service finished..");
		}
		

		/**
		 * methods use roof camera to follow lane while in motion
		 */
		public void follow(){
			
			
		}
		
		
		/**
		 * start lane following service
		 */
		public void startService(){
			PegasusLogger.getInstance().i(getName(), "Lane following Service starting..");
			mIsSerivceRunning = true;
			start();
		}
		
		/**
		 * stop serivce
		 */
		public void stopService(){
			PegasusLogger.getInstance().i(getName(), "Lane following Service stopping..");
			mIsSerivceRunning = false;
		}
		
		/**
		 * suspend service
		 */
		public void suspendService(){
			PegasusLogger.getInstance().i(getName(), "Lane following Service is suspended");
			mIsSuspended = true;
			CameraManager.getInstance().turnCameraOff();
		}
		
		/**
		 * resume service
		 */
		public void resumeService(){
			PegasusLogger.getInstance().i(getName(), "Lane following Service resumed");
			mIsSuspended = false;
			notify();
			CameraManager.getInstance().turnCameraOn();
		}
		
		public boolean isServiceSuspended(){
			return mIsSuspended;
		}
	}
	
	
	

	////////////////////////////////////////Parking Finder events \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	
	@Override
	public void onParkingFound() {
		ParkingFinder.getInstance().suspendThread();
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING_FOUND);
		PegasusLogger.getInstance().i(getName(), "Parking found stopping car....");
		mManagedVehicle.stop();
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING_FOUND);
		mLaneFollowingService.suspendService();
		//TODO - handle parking manoeuvring
		
	}
	@Override
	public void onParkingNoFound() {
		ParkingFinder.getInstance().suspendThread();
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
	}

	

}
