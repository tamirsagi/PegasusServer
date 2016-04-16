package vehicle.pegasus;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import logs.logger.PegasusLogger;
import vehicle.common.AbstractVehicle;
import vehicle.common.constants.VehicleConfigKeys;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleParams.VehicleControlType;
import vehicle.common.constants.VehicleState;
import vehicle.interfaces.onInputReceived;
import vehicle.managers.driving_manager.DrivingManager;
import vehicle.managers.finder.ParkingFinder;
import vehicle.managers.finder.constants.ParkingType;
import vehicle.pegasus.constants.SensorPositions;
import vehicle.sensors.InfraRed;
import vehicle.sensors.SensorConstants;
import vehicle.sensors.UltraSonic;

import communication.serialPorts.SerialPortHandler;
import communication.serialPorts.messages.MessageVaribles;

public class PegasusVehicle extends AbstractVehicle implements onInputReceived{
	
	private static PegasusVehicle mInstance;
	private static final String TAG = PegasusVehicle.class.getSimpleName();
	private static final String PEGASUS_DEFAULT_ID = "302774773";

	private static final int MIN_DIGITAL_SPEED = 0;
	private static final int MAX_DIGITAL_SPEED = 255;
	private static final int STRAIGHT_STEER_ANGLE = 90;
	private static final int MIN_STEER_ANGLE = 50;
	private static final int MAX_STEER_ANGLE = 140;
	
	private int mDigitalSpeed;
	private HashMap<String,UltraSonic> mUltraSonicSensors;
	private InfraRed mTachometer;
	
	/**
	 * Get class instance
	 * 
	 * @return
	 */
	public static PegasusVehicle getInstance() {
		if (mInstance == null){
			mInstance = new PegasusVehicle();
		}
		return mInstance;
	}
	
	private PegasusVehicle() {
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.FORWARD; // by default
		setControlType(VehicleControlType.AUTONOMOUS);
		setVehicleData();
		setUltraSonicSensors();
		setupTachometerSensor();
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
		
		double wheelDistance = Double.parseDouble(PegausVehicleProperties.getInstance().
				getValue(VehicleConfigKeys.KEY_FRONT_WHEEL_DISTANCE,PegausVehicleProperties.DEFAULT_VALUE_ZERO));
		
		setID(id);
		PegasusVehicleData.getInstance().setLength(length);
		PegasusVehicleData.getInstance().setWidth(width);
		PegasusVehicleData.getInstance().setWheelDiameter(wheelDiameter);
		PegasusVehicleData.getInstance().setSteeringAngle(steeringAngle);
		PegasusVehicleData.getInstance().setNumberOfUltraSonicSensors(numberOfUltraSonicSensors);
		PegasusVehicleData.getInstance().setWheelBase(wheelBase);
		PegasusVehicleData.getInstance().setDistanceCenterFrontWheelToFrontCar(centreFrontWheelToFrontCar);
		PegasusVehicleData.getInstance().setMinimumRequiredSpaceToPark();
		PegasusVehicleData.getInstance().setFrontWheelDistance(wheelDistance);
		PegasusLogger.getInstance().d(TAG,"setVehicleData", PegasusVehicleData.getInstance().toString());
		
	}
	
	@Override
	public PegasusVehicleData getVehicleData(){
		return PegasusVehicleData.getInstance();
	}
	
	
	/**
	 * set Ultra sonic sensors
	 * save in map by its location on the car
	 */
	private void setUltraSonicSensors() {
		mUltraSonicSensors = new HashMap<String, UltraSonic>();
		for (int i = 1; i <= PegasusVehicleData.getInstance().getNumberOfUltraSonicSensors(); i++) {
			String pos = SensorPositions.getSensorPosition(i);
			String relevantDistanceKey = "";
			if(!pos.equals(SensorPositions.UNKNOWN_SENSOR)){
				switch(pos){
				case SensorPositions.FRONT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_FRONT_SENSOR_MAX_DISTANCE;
					break;
				case SensorPositions.FRONT_LEFT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_FRONT_LEFT_SENSOR_MAX_DISTANCE;
						break;
				case SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_BACK_LEFT_SENSOR_MAX_DISTANCE;
						break;
				case SensorPositions.REAR_LEFT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_REAR_LEFT_SENSOR_MAX_DISTANCE;
					break;
				case SensorPositions.REAR_RIGHT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_REAR_RIGHT_SENSOR_MAX_DISTANCE;
					break;
				case SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_BACK_RIGHT_SENSOR_MAX_DISTANCE;
					break;
				case SensorPositions.FRONT_RIGHT_ULTRA_SONIC_SENSOR:
					relevantDistanceKey = VehicleConfigKeys.KEY_FRONT_RIGHT_SENSOR_MAX_DISTANCE;
					break;
				}
				int maxDetectionDistance = Integer.parseInt(PegausVehicleProperties.getInstance().
						getValue(relevantDistanceKey,PegausVehicleProperties.DEFAULT_SENSOR_DISTANCE_VALUE));
				UltraSonic us = new UltraSonic(i,maxDetectionDistance);
				us.registerListener(this);
				
				us.setPosition(pos);
				mUltraSonicSensors.put(pos, us);
			}
		}
	}
	
	/**
	 * Setup Tachometer Sensor
	 */
	private void setupTachometerSensor(){
		mTachometer = new InfraRed(SensorPositions.INFRA_RED_TACHOMETER_ID);
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
		String position = SensorPositions.getSensorPosition(aSensorId);
		if(!position.equals(SensorPositions.UNKNOWN_SENSOR)){
			return mUltraSonicSensors.get(position);
		}
		return null;
	}
	
	
	
	@Override
	public void registerAllSensorToDataProvider(){
		for( String key : mUltraSonicSensors.keySet()){
			mUltraSonicSensors.get(key).registerToDataSupplier();
		}
		mTachometer.registerToDataSupplier();
	}
	
	@Override
	public void sendSensorConfiguration(){
		JSONObject sensorData = new JSONObject();
		for( String key : mUltraSonicSensors.keySet()){
			UltraSonic us = mUltraSonicSensors.get(key);
			String id = "" + us.getId();
			int maxDistance =  us.getMaxDistance();
			try {
				sensorData.put(id, maxDistance);
			} catch (JSONException e) {
				PegasusLogger.getInstance().e(TAG,"sendSensorConfiguration", e.getMessage());
			}
		}
		if (SerialPortHandler.getInstance().isBoundToSerialPort()){
			int defaultMaxDistance = Integer.parseInt(PegausVehicleProperties.DEFAULT_SENSOR_DISTANCE_VALUE);
			SerialPortHandler.getInstance().configSensors(sensorData, defaultMaxDistance);
		}
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
		if(sensorId == SensorPositions.INFRA_RED_TACHOMETER_ID){
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
			if(getCurrentState() == VehicleState.VEHICLE_LOOKING_FOR_PARKING){
				ParkingFinder.getInstance().updateInput(SensorPositions.INFRA_RED_TACHOMETER_ID,travelledDsitanceInSec);
			}
		}
	}

	/**
	 * handle incoming data from distance sensor(ultra sonic)
	 * @param sensorId source
	 * @param value its reading
	 */
	private void handleDistanceSensorData(int sensorId, double value){
		String sensorPosition = SensorPositions.getSensorPosition(sensorId);
		switch(getCurrentState()){
		case VehicleState.VEHICLE_FREE_DRIVING:
			break;
		case VehicleState.VEHICLE_LOOKING_FOR_PARKING:
			if(!sensorPosition.equals(SensorPositions.FRONT_ULTRA_SONIC_SENSOR)){
				ParkingFinder.getInstance().updateInput(sensorId,value);
			}else{
				DrivingManager.getInstance().updateInput(sensorId,value);
			}
			break;
		case VehicleState.VEHICLE_PARKING:
			
			break;
		}
		
	}
	
	/**
	 * handle findParkingState
	 * @param parkyingType
	 */
	@Override
	public void changeUltraSonicSensorState(){
			changeUpperRightSensorsState(false);
			changeUpperLeftSensorsState(false);
			changeRearSensorState(false);
			switch(getCurrentState()){
			case VehicleState.VEHICLE_FREE_DRIVING:
				changeFrontSensorState(true);
				break;
			case VehicleState.VEHICLE_LOOKING_FOR_PARKING:
				switch(ParkingFinder.getInstance().getParkingType()){
				case ParkingType.PARALLEL_RIGHT:
					changeUpperRightSensorsState(true);
					break;
				case ParkingType.PARALLEL_LEFT:
					changeUpperLeftSensorsState(true);
						break;
				}
				break;
			case VehicleState.VEHICLE_PARKING:
				changeRearSensorState(true);
				
				break;
			}
	}
	
	private void changeSensorState(String pos, boolean aIsEnabled){
		int state = aIsEnabled ? SensorConstants.ENABLE_SENSOR : SensorConstants.DISABLE_SENSOR;
		UltraSonic us = mUltraSonicSensors.get(pos);
		if(us != null){
			SerialPortHandler.getInstance().changeSensorState(us.getId(), state);
		}
	}
	
	/**
	 * @param aSensorId
	 * @return true if sensor exist in system, false otherwise
	 */
	private boolean isValidSensorID(int aSensorId){
		return SensorPositions.getSensorPosition(aSensorId).equals(SensorPositions.UNKNOWN_SENSOR);
	}
	
	/**
	 * change front sensor state
	 * @param aIsEnabled
	 */
	private void changeFrontSensorState(boolean aIsEnabled){
		changeSensorState(SensorPositions.FRONT_ULTRA_SONIC_SENSOR,aIsEnabled);
	}
	
	/**
	 * change rear sensors state
	 * @param aIsEnabled
	 */
	private void changeRearSensorState(boolean aIsEnabled){
		changeSensorState(SensorPositions.REAR_RIGHT_ULTRA_SONIC_SENSOR,aIsEnabled);
		changeSensorState(SensorPositions.REAR_LEFT_ULTRA_SONIC_SENSOR,aIsEnabled);
	}
	
	/**
	 * change state for 2 right sensors
	 */
	private void changeUpperRightSensorsState(boolean aIsEnabled){
		changeSensorState(SensorPositions.FRONT_RIGHT_ULTRA_SONIC_SENSOR,aIsEnabled);
		changeSensorState(SensorPositions.BACK_RIGHT_ULTRA_SONIC_SENSOR,aIsEnabled);
	}
	
	/**
	 * change state for 2 left sensors
	 */
	private void changeUpperLeftSensorsState(boolean aIsEnabled){
		changeSensorState(SensorPositions.FRONT_LEFT_ULTRA_SONIC_SENSOR,aIsEnabled);
		changeSensorState(SensorPositions.BACK_LEFT_ULTRA_SONIC_SENSOR,aIsEnabled);
	}

}
