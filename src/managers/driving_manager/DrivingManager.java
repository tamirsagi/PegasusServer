package managers.driving_manager;

import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleAutonomousMode;
import vehicle.pegasus.PegasusVehicle;
import control.interfaces.OnDrivingManagerEventsListener;
import control.interfaces.OnParkingEventsListener;
import logs.logger.PegasusLogger;
import managers.common.AbstractManager;
import managers.finder.ParkingFinder;

/**
 * class is responsible for obstacles avoidance and lane following
 * @author Tamir Sagi
 *
 */
public class DrivingManager extends AbstractManager  implements OnParkingEventsListener{

	private static DrivingManager mInstance;
	private LaneFollowingService mLaneFollowingService;
	private OnDrivingManagerEventsListener mListener;
	private int mLastSpeed;
	private VehicleParams.DrivingDirection mCurrentDirection;
	private int mCurrentMode;
	
	
	
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
		if(!ParkingFinder.getInstance().isAlive()){
			ParkingFinder.getInstance().registerParkingEventsListner(this);
			ParkingFinder.getInstance().startThread();
			ParkingFinder.getInstance().suspendThread();
		}
	}
	
	/**
	 * register listener
	 * @param aListener
	 */
	public void registerListener(OnDrivingManagerEventsListener aListener){
			mListener = aListener;
	}

	@Override
	public void run() {
		PegasusLogger.getInstance().i(getTag(),"Driving Manager has been started...");
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
	}

	@Override
	public void suspendThread() {
		if(!mIsSuspended){
			if(mLaneFollowingService != null && !mLaneFollowingService.mIsServiceSuspended){
				mLaneFollowingService.suspendService();
			}
			ParkingFinder.getInstance().suspendThread();
			super.suspendThread();
		}
	}
	
	public boolean isThreadSuspended(){
		return mIsSuspended;
	}

	@Override
	public synchronized void resumeThread() {
		if(mIsSuspended){
			super.resumeThread();
		}
	}

	@Override
	public void updateInput(int sensorId, double value){
		
	}
	
	/**
	 * set current autonomous mode;
	 * @param aMode autonomouse mode (Free driving, parking spot searching , manoeuvring)
	 */
	public void setCurrentMode(int aMode){
		mCurrentMode = aMode;
	}
	
	/**
	 * handle free driving
	 */
	public void freeDrive(){
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		if(mLaneFollowingService != null){
			mLaneFollowingService = new LaneFollowingService();
			mLaneFollowingService.startService();
		}else if(!mLaneFollowingService.isAlive()){
			mLaneFollowingService.startService();
		}else if(mLaneFollowingService.mIsServiceSuspended){
			mLaneFollowingService.resumeService();
		}
	}
	
	/**
	 * Method resumes parking finder thread
	 * @param parkingType
	 * @param aMinSpace
	 */
	public void findParkingSpot(int parkingType, double aMinSpace){
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING);
		if(mLaneFollowingService != null && !mLaneFollowingService.mIsServiceSuspended){
			mLaneFollowingService.suspendService();
		}
		ParkingFinder.getInstance().searchParking(parkingType, aMinSpace);
		ParkingFinder.getInstance().resumeThread();
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
			
		}
		
		@Override
		public void run() {
			PegasusLogger.getInstance().i(getName(),"lane following service has been started...");
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
			PegasusLogger.getInstance().i(TAG, "Lane following Service finished..");
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
			PegasusLogger.getInstance().i(TAG, "Lane following Service starting..");
			mIsSerivceRunning = true;
			start();
		}
		
		/**
		 * stop serivce
		 */
		public void stopService(){
			PegasusLogger.getInstance().i(TAG, "Lane following Service stopping..");
			mIsSerivceRunning = false;
		}
		
		/**
		 * suspend service
		 */
		public void suspendService(){
			PegasusLogger.getInstance().i(TAG, "Lane following Service is suspended");
			mIsSuspended = true;
		}
		
		/**
		 * resume service
		 */
		public void resumeService(){
			PegasusLogger.getInstance().i(TAG, "Lane following Service resumed");
			mIsSuspended = false;
			notify();
		}
		
	}
	
	
	

	////////////////////////////////////////Parking Finder events \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	
	@Override
	public void onParkingFound() {
		ParkingFinder.getInstance().suspendThread();
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKING_FOUND);
		PegasusLogger.getInstance().i(getName(), "Parking found stopping car....");
		mListener.onStop();
		mLaneFollowingService.suspendService();
		//TODO - handle parking manoeuvring
		
	}
	@Override
	public void onParkingNoFound() {
		ParkingFinder.getInstance().suspendThread();
		setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
	}

	

}
