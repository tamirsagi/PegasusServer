package vehicle.Pegasus;

import java.util.HashMap;
import communication.serialPorts.SerialPortHandler;
import communication.serialPorts.messages.MessageVaribles;
import logs.logger.PegasusLogger;
import vehicle.Interfaces.onInputReceived;
import vehicle.Sensor.AbstractSensor;
import vehicle.Sensor.InfraRed;
import vehicle.Sensor.UltraSonic;
import vehicle.algorithms.ParkingFinder;
import vehicle.common.AbstractVehicle;
import vehicle.common.constants.VehicleConfigKeys;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.VehicleControlType;
import vehicle.common.constants.VehicleState;

public class PegasusVehicle extends AbstractVehicle implements onInputReceived{
	
	private static final String TAG = PegasusVehicle.class.getSimpleName();
	private static final String PEGASUS_DEFAULT_ID = "302774773";
	private static PegasusVehicle mInstance;

	private static final int MIN_DIGITAL_SPEED = 0;
	private static final int MAX_DIGITAL_SPEED = 255;
	private static final int STRAIGHT_STEER_ANGLE = 90;
	private static final int MIN_STEER_ANGLE = 50;
	private static final int MAX_STEER_ANGLE = 140;
	
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
	
	private int mDigitalSpeed;
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
		setControlType(VehicleControlType.AUTONOMOUS);
		setCurrentState(VehicleState.VEHICLE_DEFAULT);
		setVehicleData();
		setUltraSonicSensors();
		setupTachometerSensor();
		mListener.onVehicleStateChanged(true);
		
	}
	
	@Override
	public void setVehicleData() {
		String id = PegausVehicleProperties.getInstance().getValue(VehicleConfigKeys.KEY_ID,PEGASUS_DEFAULT_ID);
		double length = Double.parseDouble(PegausVehicleProperties.getInstance().
					getValue(VehicleConfigKeys.KEY_LENGTH,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		double width = Double.parseDouble(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_WIDTH,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		double steeringAngle = Double.parseDouble(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_MAX_STEERING_ANGLE_FACTOR,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		double wheelDiameter = Double.parseDouble(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_WHEEL_DIAMETER,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		int numberOfUltraSonicSensors = Integer.parseInt(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_NUMBER_OF_ULTRA_SONIC_SENSORS,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		double wheelBase = Double.parseDouble(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_WHEEL_BASE,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		double centreFrontWheelToFrontCar = Double.parseDouble(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_FRONT_WHEEL_FRONT_CAR,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		setID(id);
		PegasusVehicleData.getInstance().setLength(length);
		PegasusVehicleData.getInstance().setWidth(width);
		PegasusVehicleData.getInstance().setWheelDiameter(wheelDiameter);
		PegasusVehicleData.getInstance().setSteeringAngle(steeringAngle);
		PegasusVehicleData.getInstance().setNumberOfUltraSonicSensors(numberOfUltraSonicSensors);
		PegasusVehicleData.getInstance().setWheelBase(wheelBase);
		PegasusVehicleData.getInstance().setDistanceCenterFrontWheelToFrontCar(centreFrontWheelToFrontCar);
		PegasusVehicleData.getInstance().setMinimumRequiredSpaceToPark();
		PegasusLogger.getInstance().d(TAG,"setVehicleData", PegasusVehicleData.getInstance().toString());
		
	}
	
	public PegasusVehicleData getVehicleData(){
		return PegasusVehicleData.getInstance();
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
	private void setUltraSonicSensors() {
		mUltraSonicSensors = new HashMap<String, UltraSonic>();
		for (int i = 1; i <= PegasusVehicleData.getInstance().getNumberOfUltraSonicSensors(); i++) {
			UltraSonic us = new UltraSonic(i);
			us.registerListener(this);
			String pos = getSensorPosition(us.getId());
			us.setPosition(pos);
			mUltraSonicSensors.put(pos, us);
		}
	}
	
	/**
	 * Setup Tachometer Sensor
	 */
	private void setupTachometerSensor(){
		mTachometer = new InfraRed(INFRA_RED_TACHOMETER_ID);
		mTachometer.registerListener(this);
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
	
	public void registerAllSensorToDataProvider(){
		for( String key : mUltraSonicSensors.keySet()){
			mUltraSonicSensors.get(key).registerToDataSupplier();
		}
		mTachometer.registerToDataSupplier();
	}
	

	@Override
	public void changeSpeed(int digitalSpeed) {
		mDigitalSpeed = digitalSpeed;
		if (SerialPortHandler.getInstance().isBoundToSerialPort()){
			SerialPortHandler.getInstance().changeSpeed(digitalSpeed);
		}
	}

	@Override
	public void turnRight(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE - rotationAngle; // from 0-40 to 50 - 90
		if (SerialPortHandler.getInstance().isBoundToSerialPort()){
			SerialPortHandler.getInstance().changeSteerMotor(
					MessageVaribles.VALUE_STEERING_RIGHT, rotationAngle);
		}

	}

	@Override
	public void turnLeft(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE + rotationAngle; // from 0-40 to 90 - 130
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeSteerMotor(
					MessageVaribles.VALUE_STEERING_LEFT, rotationAngle);
	}

	@Override
	public void driveForward() {
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.FORWARD;
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeDrivingDirection(
					MessageVaribles.VALUE_DRIVING_FORWARD);

	}

	@Override
	public void driveBackward() {
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.REVERSE;
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeDrivingDirection(
					MessageVaribles.VALUE_DRIVING_REVERSE);

	}
	
	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public void stop() {
		changeSpeed(0);
	}
	
	
	@Override
	public void onReceived(int sensorId,double value){
		PegasusLogger.getInstance().i(TAG, "onReceived", "Sensor id:" + sensorId +" value:" + value);
		if(sensorId == INFRA_RED_TACHOMETER_ID){
			handleTachometerData(value);
		}else{
			handleDistanceSensorData(sensorId,value);
		}
	}
	
	/**
	 * handles data from infra red sensor
	 * @param value - round of wheel per second
	 */
	private void handleTachometerData(double aValue){
		if(aValue >= 0){
			double travelledDsitanceInSec = aValue * PegasusVehicleData.getInstance().getWheelPerimeter();
			setCurrentspeed(travelledDsitanceInSec);
			setTravelledDistance(getTravelledDistance() + travelledDsitanceInSec);
		}
	}

	/**
	 * handle incoming data from distance sensor(ultra sonic)
	 * @param sensorId source
	 * @param value its reading
	 */
	private void handleDistanceSensorData(int sensorId, double value){
		switch(getCurrentState()){
		case VehicleState.VEHICLE_FREE_DRIVING:
			break;
		case VehicleState.VEHICLE_LOOKING_FOR_PARKING:
			ParkingFinder.getInstance().updateInput(sensorId,value);
			break;
		case VehicleState.VEHICLE_PARKING:
			
			break;
		}
		
	}


}
