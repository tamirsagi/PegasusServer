package pegasusVehicle;

import java.util.HashMap;

import pegasusVehicle.Sensor.InfraRed;
import pegasusVehicle.Sensor.UltraSonic;
import pegasusVehicle.params.VehicleParams;
import control.Interfaces.IVehicleActionsListener;

public class PegasusVehicle extends AbstractVehicle {
	private static final String TAG = "Pegasus Vehicle";
	private static final int PEGASUS_ID = 302774773;
	private static PegasusVehicle mInstance;

	private static final int MIN_DIGITAL_SPEED = 0;
	private static final int MAX_DIGITAL_SPEED = 255;
	private static final int STRAIGHT_STEER_ANGLE = 90;
	private static final int MIN_STEER_ANGLE = 50;
	private static final int MAX_STEER_ANGLE = 130;
	
	//Ultra Sonic Sensors positions Keys
	protected static final String UNKNOWN_SENSOR = "UNKNOWN_SENSOR";
	protected static final String FRONT_ULTRA_SONIC_SENSOR = "FRONT";
	protected static final String FRONT_RIGHT_ULTRA_SONIC_SENSOR = "FRONT_RIGHT";
	protected static final String BACK_RIGHT_ULTRA_SONIC_SENSOR = "BACK_RIGHT";
	protected static final String REAR_RIGHT_ULTRA_SONIC_SENSOR = "REAR_RIGHT";
	protected static final String FRONT_LEFT_ULTRA_SONIC_SENSOR = "FRONT_LEFT";
	protected static final String BACK_LEFT_ULTRA_SONIC_SENSOR = "BACK_LEFT";
	protected static final String REAR_LEFT_ULTRA_SONIC_SENSOR = "REAR_LEFT";
	//Ultra Sonic Sensors IDs
	private static final int ULTRA_SONIC_SENSOR_ONE = 1;
	private static final int ULTRA_SONIC_SENSOR_TWO = 2;
	private static final int ULTRA_SONIC_SENSOR_THREE = 3;
	private static final int ULTRA_SONIC_SENSOR_FOUR = 4;
	private static final int ULTRA_SONIC_SENSOR_FIVE = 5;
	private static final int ULTRA_SONIC_SENSOR_SIX = 6;
	private static final int ULTRA_SONIC_SENSOR_SEVEN = 7;
	//Tachometer ID
	private static final int INFRA_RED_TACHOMETER_ID = 18;
	
	
	private IVehicleActionsListener mVehicleActionsListener;
	private int mDigitalSpeed;
	private int mNumberOfUltraSonicSensors;
	private HashMap<String,UltraSonic> mUltraSonicSensors;
	private InfraRed mTachometer;

	/**
	 * Get class instance
	 * 
	 * @return
	 */
	public static PegasusVehicle getInstance() {
		if (mInstance == null)
			mInstance = new PegasusVehicle();

		return mInstance;
	}

	private PegasusVehicle() {
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.FORWARD; // by default
		setID(PEGASUS_ID);
		setUltraSonicSensors();
		mTachometer = new InfraRed(INFRA_RED_TACHOMETER_ID);

	}

	@Override
	public void registerVehicleActionsListener(IVehicleActionsListener listener) {
		mVehicleActionsListener = listener;
	}
	
	
	/**
	 * 
	 * @param sensorID
	 * @return sensor position on the car 
	 */
	private String getSensorPosition(int sensorID){
		switch(sensorID){
		case ULTRA_SONIC_SENSOR_ONE:
			return FRONT_ULTRA_SONIC_SENSOR;
		case ULTRA_SONIC_SENSOR_TWO:
			return FRONT_LEFT_ULTRA_SONIC_SENSOR;
		case ULTRA_SONIC_SENSOR_THREE:
			return BACK_LEFT_ULTRA_SONIC_SENSOR;
		case ULTRA_SONIC_SENSOR_FOUR:
			return REAR_LEFT_ULTRA_SONIC_SENSOR;
		case ULTRA_SONIC_SENSOR_FIVE:
			return REAR_RIGHT_ULTRA_SONIC_SENSOR;
		case ULTRA_SONIC_SENSOR_SIX:
			return BACK_RIGHT_ULTRA_SONIC_SENSOR;
		case ULTRA_SONIC_SENSOR_SEVEN:
			return FRONT_RIGHT_ULTRA_SONIC_SENSOR;
		default:
			return UNKNOWN_SENSOR;
		}
	}
	
	/**
	 * set Ultra sonic sensors
	 * save in map by its location on the car
	 */
	public void setUltraSonicSensors() {
		mUltraSonicSensors = new HashMap<String, UltraSonic>();
		for (int i = 1; i <= mNumberOfUltraSonicSensors; i++) {
			UltraSonic us = new UltraSonic(i);
			String pos = getSensorPosition(us.getId());
			mUltraSonicSensors.put(pos, us);
		}
	}
	
	public InfraRed getTachometer(){
		return mTachometer;
	}
	
	/**
	 * @param aSensorId - sensor number
	 * @return Ultra Sonic sensor, null if sensor id does not exist
	 */
	public UltraSonic getUltraSonicSensor(int aSensorId){
		String position = getSensorPosition(aSensorId);
		if(!position.equals(UNKNOWN_SENSOR)){
			return mUltraSonicSensors.get(position);
		}
		return null;
	}
	

	@Override
	public void changeSpeed(int digitalSpeed) {
		mDigitalSpeed = digitalSpeed;
		mVehicleActionsListener.changeSpeed(digitalSpeed);
	}

	@Override
	public void turnRight(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE - rotationAngle; // from 0-40 to 50 - 90
		mVehicleActionsListener.turnRight(rotationAngle);

	}

	@Override
	public void turnLeft(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE + rotationAngle; // from 0-40 to 90 - 130
		mVehicleActionsListener.turnLeft(rotationAngle);
	}

	@Override
	public void driveForward() {
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.FORWARD;
		mVehicleActionsListener.driveForward();

	}

	@Override
	public void driveBackward() {
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.REVERSE;
		mVehicleActionsListener.driveBackward();

	}

	@Override
	public void stop() {
		mDigitalSpeed = 0;
		mVehicleActionsListener.stop();

	}

	

}
