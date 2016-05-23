package managers.driving_manager;

import util.CameraManager;
import vehicle.common.ActionTimer;
import vehicle.common.VehicleData;
import vehicle.common.constants.ParkingStates;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.DrivingDirection;
import vehicle.common.constants.VehicleAutonomousMode;
import vehicle.interfaces.OnManagedVechile;
import vehicle.interfaces.OnTimerListener;
import vehicle.pegasus.constants.SensorPositions;
import logs.logger.PegasusLogger;
import managers.common.AbstractManager;
import managers.constant.ParkingType;

/**
 * class is responsible for obstacles avoidance and lane following
 * @author Tamir Sagi
 *
 */
public class DrivingManager extends AbstractManager  implements OnTimerListener{

	private static DrivingManager mInstance;
	private static final int DISTANCE_TO_STOP_FRONT_OBSTACLE = 6; // 6 cm is the max distance to stop when detecting an obstacles
	private static final int DISTANCE_TO_STOP_BACK_OBSTACLE = 4;
	private static final int DISTANCE_FROM_PARKED_CAR = 3;
	private static final int SAFE_DISTANCE_WHEN_PARKING = 5;
	
	private int mCurrentSpeed;
	private VehicleParams.DrivingDirection mCurrentDirection;
	private int mCurrentMode = VehicleAutonomousMode.VEHICLE_NONE;
	private OnManagedVechile mManagedVehicle;
	
	private double[] mLastSensorData = {-1,-1,-1,-1,-1,-1,-1,-1}; //debug
	//	Parking Search Params \\
	
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 1000 * 60; // 1 min for searching
	private static final int DEFAULT_DISTANCE_FROM_PARKED_CAR = 4;
	private int mParkingType;
	private int mParkingState;
	private boolean mFound;
	private double mMaxWheelSensorInterrupts;
	private double mDistanceFromParkedCar = DEFAULT_DISTANCE_FROM_PARKED_CAR;
	private VehicleData mVehicleData;
	
	// Parking Manoeuvering \\
	private double mArchLength;
	
	
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
			mVehicleData = mManagedVehicle.getVehicleData();
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
			if(CameraManager.getInstance().isCameraEnabled()){
				CameraManager.getInstance().turnCameraOff();
			}
			super.suspendThread();
		}
	}
	
	@Override
	public synchronized void resumeThread() {
		if(mIsSuspended){
			PegasusLogger.getInstance().i(getName(),"resumed...");
			if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING ||
					mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING){
				if(!CameraManager.getInstance().isCameraEnabled()){
					//CameraManager.getInstance().turnCameraOn();
				}
			}
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
			handleFrontSensorData(frontSensorValue);
			if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING){
				if(isParallelParking()){
					searchParallelParking();
					if(mFound){
						mManagedVehicle.stop();
						double newValue;
						// keep counting wheel turns until car is stopped
						while((newValue = mManagedVehicle.
								getInterruptsCounterOfWheelSensor()) != mCurrentWheelSensorIntterupts){
							mCurrentWheelSensorIntterupts = newValue;
						}
						onParkingFound();
					}
				}
			follow();
			}
		}else if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING){
			enterParallelParking();
		}else if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING){
			exitParallelParking();
		}
	}

	/**
	 * Method handles front sensor data
	 * @param aValue
	 */
	public synchronized void handleFrontSensorData(double aValue){
		if(aValue >= 0){
			if(mLastSensorData[1] != aValue){
				mLastSensorData[1] = aValue;
				PegasusLogger.getInstance().i(getName(),"Front Sensor Data : " + mLastSensorData[1]);
			}
			if(0 < aValue && aValue <= DISTANCE_TO_STOP_FRONT_OBSTACLE){
				if(mManagedVehicle.getSpeed() != 0){
					mManagedVehicle.stop();
				}
			}else if(mManagedVehicle.getSpeed() == 0){
						mManagedVehicle.startNormalDriving();
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
				if(!CameraManager.getInstance().isCameraEnabled()){
					//CameraManager.getInstance().turnCameraOn();
				}
				if(mManagedVehicle.getSpeed() == 0 ){
					mManagedVehicle.startNormalDriving();
				}
				break;
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING:
				CameraManager.getInstance().turnCameraOff();
				break;
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKED:
				break;
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING:
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
		if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKED){
			mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING);
			DrivingManager.getInstance().setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING);
		}else{
			mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
			DrivingManager.getInstance().setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		}
		resumeThread();
	}
	
	/**
	 * Method resumes parking finder thread
	 * @param parkingType
	 * @param aMinSpace
	 */
	public void findParkingSpot(int parkingType){
		if(mCurrentMode == VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING || 
				mCurrentMode == VehicleAutonomousMode.VEHICLE_NONE){
			mParkingState = ParkingStates.PARKING_DEFAULT;
			double minSpaceToPark = 0;
			switch(parkingType){
			case ParkingType.PARALLEL_RIGHT:
			case ParkingType.PARALLEL_LEFT:
				PegasusLogger.getInstance().d(getName(), "findParkingSpot","parking type : " +  ParkingType.getParkingTypeName(parkingType));
				minSpaceToPark = calculateMinSpaceToPark(mDistanceFromParkedCar);
				break;
			}
			if(minSpaceToPark > 0){
				mCurrentWheelSensorIntterupts = 0;
				mMaxWheelSensorInterrupts = calculateMaxWheelInterrupts(minSpaceToPark);
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
				mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
				setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
			}
		}
		resumeThread();
	}
	
	/**
	 * Methods handles incoming data for parallel parking searching
	 * @param incomingData
	 */
	private void searchParallelParking() {
		if (!mFound) {
			double relevantSensorValue;
			if (mParkingType == ParkingType.PARALLEL_RIGHT) {
				relevantSensorValue = mManagedVehicle
						.getValueFromDistanceSensor(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR);
			} else {
				relevantSensorValue = mManagedVehicle
						.getValueFromDistanceSensor(SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR);
			}
			if (mCurrentWheelSensorIntterupts >= mMaxWheelSensorInterrupts && !mFound) {
				PegasusLogger.getInstance().d(getName(),"found parking:" + mCurrentWheelSensorIntterupts);
				mFound = true;
				if (mTimer != null) {
					mTimer.killThread();
				}
			}
			if (!mFound) {
				if (relevantSensorValue != 0) {
					PegasusLogger.getInstance().d(getName(),
							"searchParallelParking",
							"back sensor data: " + relevantSensorValue);
					mManagedVehicle.resetInterruptsCounter();
					mCurrentWheelSensorIntterupts = 0;
					if (relevantSensorValue < mDistanceFromParkedCar) {
						mDistanceFromParkedCar = relevantSensorValue;
						double spaceToPark = calculateMinSpaceToPark(mDistanceFromParkedCar);
						mMaxWheelSensorInterrupts = calculateMaxWheelInterrupts(spaceToPark);
						PegasusLogger.getInstance().i(
								getName(),
								"searchParallelParking",
								"update parking with min space of: "
										+ spaceToPark
										+ "Max interrupts : "
										+ mMaxWheelSensorInterrupts);
					}
				} else {
					mCurrentWheelSensorIntterupts = mManagedVehicle
							.getInterruptsCounterOfWheelSensor();
				}
			}
			if(mCurrentWheelSensorIntterupts != mLastSensorData[0]){
				mLastSensorData[0] = mCurrentWheelSensorIntterupts;
				PegasusLogger.getInstance().d(getName(),"Current intterupts:" + mLastSensorData[0]);
			}
		}
	}
	
	/**
	 * determine the minimum space required to park the car
	 * @param aTurningRadius - back wheel turning radius (the small one)
	 * @param aDistanceFromParkedCar distance from parked car
	 * @param aPArkedCarWidth parked car width
	 * @return
	 */
	public double calculateMinSpaceToPark(double aDistanceFromParkedCar){
		double CenterToCenter = 2 * mVehicleData.getBackWheelTurningRadius();
		double side = CenterToCenter - aDistanceFromParkedCar - mVehicleData.getWidth();
		return Math.sqrt(Math.pow(CenterToCenter, 2) - Math.pow(side, 2));
	}
	
	/**
	 * determine how many hits we need the Tachometer to detect for the given minimum space
	 * @param aMinSpaceToPark
	 * @param aWheelSlots number of neats which are detected by the IR sensor
	 * @param aWheelPerimeter
	 * @return
	 */
	public double calculateMaxWheelInterrupts(double aMinSpaceToPark){
		return (mVehicleData.getNumberOfWheelSlots() * aMinSpaceToPark) / mVehicleData.getWheelPerimeter();
	}

	/**
	 * enter parallel parking spot, first we check if the car needs to re-positioned
	 */
	private void enterParallelParking(){
		mManagedVehicle.changeDrivingDirection(VehicleParams.BACKWARD);
		prepareParallelParkingData();
		//check if car had moved forward after parking found
		double diff = mCurrentWheelSensorIntterupts - mMaxWheelSensorInterrupts;
		PegasusLogger.getInstance().d(getName(),
				"enterParallelParking","check if car had moved forward after parking found. diff:" + diff);
		if(diff > 0){
			mManagedVehicle.startNormalDriving();
			while(diff > mManagedVehicle.getInterruptsCounterOfWheelSensor());
		}
		double totalWay = mArchLength + diff;
		
		if(mParkingType == ParkingType.PARALLEL_RIGHT){
			mManagedVehicle.turnRight(mManagedVehicle.getMaxServoRightAngle());
			mManagedVehicle.resetInterruptsCounter();
			while(totalWay > mManagedVehicle.getInterruptsCounterOfWheelSensor());
			mManagedVehicle.turnLeft(mManagedVehicle.getMaxServoLeftAngle());
			mManagedVehicle.resetInterruptsCounter();
			while(mArchLength - SAFE_DISTANCE_WHEN_PARKING > mManagedVehicle.getInterruptsCounterOfWheelSensor());
		}else{
			mManagedVehicle.turnLeft(mManagedVehicle.getMaxServoLeftAngle());
			mManagedVehicle.resetInterruptsCounter();
			while(totalWay > mManagedVehicle.getInterruptsCounterOfWheelSensor());
			mManagedVehicle.turnRight(mManagedVehicle.getMaxServoRightAngle());	
			mManagedVehicle.resetInterruptsCounter();
			while(mArchLength - SAFE_DISTANCE_WHEN_PARKING > mManagedVehicle.getInterruptsCounterOfWheelSensor());
		}
		double back_right_sensor = Integer.MAX_VALUE;
		double back_left_sensor = Integer.MAX_VALUE;
		while(back_right_sensor > DISTANCE_TO_STOP_BACK_OBSTACLE && back_left_sensor > DISTANCE_TO_STOP_BACK_OBSTACLE){
			back_right_sensor = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR);
			back_left_sensor = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR);
			PegasusLogger.getInstance().d(getName(),"Back sensors right: " + back_right_sensor +" left: " + back_left_sensor);
		}
		mManagedVehicle.stop();
		if(mParkingType == ParkingType.PARALLEL_RIGHT){
			mManagedVehicle.turnRight(mManagedVehicle.getStraightServoAngle());
		}else{
			mManagedVehicle.turnLeft(mManagedVehicle.getStraightServoAngle());
		}
		setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKED);
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_PARKED);
	}
	
	/**
	 * Manoeuvring out of parallel parking
	 */
	private void exitParallelParking(){
		mManagedVehicle.changeDrivingDirection(VehicleParams.FORWARD);
		if(mParkingType == ParkingType.PARALLEL_RIGHT){
			mManagedVehicle.turnLeft(mManagedVehicle.getMaxServoLeftAngle());
			mManagedVehicle.startNormalDriving();
			while(mArchLength > mManagedVehicle.getInterruptsCounterOfWheelSensor());
			mManagedVehicle.turnRight(mManagedVehicle.getMaxServoRightAngle());
			while(mArchLength > mManagedVehicle.getInterruptsCounterOfWheelSensor());
		}else{
			mManagedVehicle.turnRight(mManagedVehicle.getMaxServoRightAngle());
			mManagedVehicle.startNormalDriving();
			while(mArchLength > mManagedVehicle.getInterruptsCounterOfWheelSensor());
			mManagedVehicle.turnLeft(mManagedVehicle.getMaxServoLeftAngle());
			while(mArchLength > mManagedVehicle.getInterruptsCounterOfWheelSensor());
		}
		
		setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		
	}
	
	/**
	 * prepare relevant data prior executing parallel park
	 */
	private void prepareParallelParkingData(){
		VehicleData vehicleData = mManagedVehicle.getVehicleData();
		double distanceFromParkedCar;
		if(mParkingType == ParkingType.PARALLEL_RIGHT){
			distanceFromParkedCar = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR);
		}else{
			distanceFromParkedCar = mManagedVehicle.getValueFromDistanceSensor(SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR);
		}
		if(distanceFromParkedCar == 0){
			distanceFromParkedCar = DISTANCE_FROM_PARKED_CAR;
		}
		double relevantDistance = distanceFromParkedCar + vehicleData.getWidth();
		double diameter = 2 * vehicleData.getBackWheelTurningRadius();
		double arcAngle = Math.acos( (diameter - relevantDistance ) / diameter );
		mArchLength = arcAngle * vehicleData.getBackWheelTurningRadius();
		PegasusLogger.getInstance().d(getName(),"prepareParallelParkingData",
					String.format("distanceFromParkedCar:%s,relevantDistance:%s,arcAngle:%s,mArchLength:%s",
							distanceFromParkedCar,relevantDistance,arcAngle,mArchLength));
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
		mManagedVehicle.stop();
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING);
		setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING);
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
		mManagedVehicle.stop();
		setMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
		mManagedVehicle.setCurrentState(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
	}

	

}
