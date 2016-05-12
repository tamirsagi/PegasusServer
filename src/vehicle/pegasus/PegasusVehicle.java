package vehicle.pegasus;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import logs.logger.PegasusLogger;
import managers.constant.ParkingType;
import managers.driving_manager.DrivingManager;
import vehicle.common.AbstractVehicle;
import vehicle.common.constants.VehicleConfigKeys;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleAutonomousMode;
import vehicle.interfaces.OnManagedVechile;
import vehicle.pegasus.constants.SensorPositions;
import vehicle.sensors.Tachometer;
import vehicle.sensors.SensorConstants;
import vehicle.sensors.UltraSonic;

import communication.messages.MessageVaribles;
import communication.serialPorts.SerialPortHandler;

public class PegasusVehicle extends AbstractVehicle implements OnManagedVechile{
	
	private static PegasusVehicle mInstance;
	private static final String TAG = PegasusVehicle.class.getSimpleName();
	private static final String PEGASUS_DEFAULT_ID = "302774773";

	public static final int MIN_DIGITAL_SPEED = 70;
	public static final int MAX_DIGITAL_SPEED = 255;
	private static final int STRAIGHT_STEER_ANGLE = 90;
	private static final int MIN_SERVO_ANGLE = 50;
	private static final int MAX_SERVO_ANGLE = 130;
	
	private int mDigitalSpeed;
	private HashMap<String,UltraSonic> mUltraSonicSensors;
	private Tachometer mTachometer;
	
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
		try {
			setVehicleData();
			setUltraSonicSensors();
			setupTachometerSensor();
		} catch (Exception e) {
			PegasusLogger.getInstance().e(TAG, e.getMessage());
		}
		
	}
	
	
	
	@Override
	public void setVehicleData() {
		JSONObject pegasusData;
		try {
			pegasusData = PegasusVehicleProperties.getInstance().toJsonObject();
			String id = pegasusData.optString(VehicleConfigKeys.KEY_ID,PEGASUS_DEFAULT_ID);
			setID(id);
			PegasusVehicleData.createInstance(pegasusData);
			PegasusLogger.getInstance().d(TAG,"setVehicleData", PegasusVehicleData.getInstance().toString());
		} catch (JSONException e) {
			PegasusLogger.getInstance().e(TAG,e.getMessage());
		}
		
		
	}
	
	@Override
	public PegasusVehicleData getVehicleData(){
		return PegasusVehicleData.getInstance();
	}
	
	
	@Override
	public void setCurrentState(int aState) {
		super.setCurrentState(aState);
		changeUltraSonicSensorState();
		mTachometer.resetTachometer();
	}
	
	/**
	 * set Ultra sonic sensors
	 * save in map by its location on the car
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	private void setUltraSonicSensors() throws NumberFormatException, Exception {
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
				int maxDetectionDistance = Integer.parseInt(PegasusVehicleProperties.getInstance().
						getValue(relevantDistanceKey,PegasusVehicleProperties.DEFAULT_SENSOR_DISTANCE_VALUE));
				UltraSonic us = new UltraSonic(i,maxDetectionDistance);
				us.setPosition(pos);
				mUltraSonicSensors.put(pos, us);
			}
		}
	}
	
	/**
	 * Setup Tachometer Sensor
	 */
	private void setupTachometerSensor(){
		mTachometer = new Tachometer(SensorPositions.INFRA_RED_TACHOMETER_ID);
	}
	
	public Tachometer getTachometer(){
		return mTachometer;
	}
	
	@Override
	public void setControlType(int aVehicleMode){
		super.setControlType(aVehicleMode);
		if(mDigitalSpeed != 0){
			stop();
		}
		switch (aVehicleMode) {
		case VehicleParams.VEHICLE_MODE_AUTONOMOUS:
			changeFrontSensorState(true);
			break;
		case VehicleParams.VEHICLE_MODE_MANUAL:
			disableAllSensors();
			setCurrentState(VehicleAutonomousMode.VEHICLE_NONE);
			break;
		}
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
			int defaultMaxDistance = Integer.parseInt(PegasusVehicleProperties.DEFAULT_SENSOR_DISTANCE_VALUE);
			SerialPortHandler.getInstance().configSensors(sensorData, defaultMaxDistance);
		}
	}
	
	@Override
	public void startNormalDriving(){
		driveForward();
		changeSpeed(MIN_DIGITAL_SPEED);
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
		mCurrentDrivingDirection = VehicleParams.DrivingDirection.BACKWARD;
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeDrivingDirection(
					MessageVaribles.VALUE_DRIVING_REVERSE);

	}
	
	@Override
	public void stop() {
		changeSpeed(0);
	}
	
	@Override
	public String getTag() {
		return TAG;
	}
	
	@Override
	public int getSpeed(){
		return mDigitalSpeed;
	}

	
//	@Override
//	public void onReceived(int sensorId,double value){
//		if(value >= 0){
//			PegasusLogger.getInstance().i(TAG, "onReceived", "Sensor id:" + sensorId +" value:" + value);
//			if(sensorId == SensorPositions.INFRA_RED_TACHOMETER_ID){
//				DrivingManager.getInstance().handleTachometerData(value);
//			}else{
//				DrivingManager.getInstance().updateInput(sensorId,value);
//			}
//		}
//	}

	/**
	 * change ultra sonic state when vehicle state is changed
	 */
	public void changeUltraSonicSensorState(){
			switch(getCurrentState()){
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING:
				changeFrontSensorState(true);
				changeUpperRightSensorsState(false);
				changeUpperLeftSensorsState(false);
				changeRearSensorState(false);
				break;
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING:
				changeFrontSensorState(true);
				changeRearSensorState(false);
				switch(DrivingManager.getInstance().getParkingType()){
				case ParkingType.PARALLEL_RIGHT:
					changeUpperRightSensorsState(true);
					changeUpperLeftSensorsState(false);
					break;
				case ParkingType.PARALLEL_LEFT:
					changeUpperLeftSensorsState(true);
					changeUpperRightSensorsState(false);
						break;
				}
				break;
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING:
				changeFrontSensorState(false);
				changeRearSensorState(true);
				break;
			case VehicleAutonomousMode.VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING:
				
				break;
			}
	}
	
	/**
	 * disable all sensors;
	 */
	private void disableAllSensors(){
		changeFrontSensorState(false);
		changeUpperRightSensorsState(false);
		changeUpperLeftSensorsState(false);
		changeRearSensorState(false);
	}
	
	private void changeSensorState(String pos, boolean aIsEnabled){
		PegasusLogger.getInstance().v(Thread.currentThread().getName(), "changing state");
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

	@Override
	public double getInterruptsCounterOfWheelSensor() {
		return mTachometer.getLastValue();
	}

	@Override
	public double getValueFromDistanceSensor(String pos) {
		return mUltraSonicSensors.get(pos).getLastValue();
	}
	
	@Override
	public int getMaxServoRightAngle(){
		return MIN_SERVO_ANGLE;
	}
	
	@Override
	public int getMaxServoLeftAngle(){
		return MAX_SERVO_ANGLE;
	}

}
