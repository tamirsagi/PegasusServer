package vehicle.pegasus.constants;

public class SensorPositions {

	// Ultra Sonic Sensors positions Keys
	public static final String UNKNOWN_SENSOR = "UNKNOWN_SENSOR";
	public static final String FRONT_ULTRA_SONIC_SENSOR = "FRONT";
	public static final String FRONT_RIGHT_ULTRA_SONIC_SENSOR = "FRONT_RIGHT";
	public static final String BACK_RIGHT_ULTRA_SONIC_SENSOR = "BACK_RIGHT";
	public static final String REAR_RIGHT_ULTRA_SONIC_SENSOR = "REAR_RIGHT";
	public static final String FRONT_LEFT_ULTRA_SONIC_SENSOR = "FRONT_LEFT";
	public static final String BACK_LEFT_ULTRA_SONIC_SENSOR = "BACK_LEFT";
	public static final String REAR_LEFT_ULTRA_SONIC_SENSOR = "REAR_LEFT";

	// Ultra Sonic Sensors IDs
	public static final int ULTRA_SONIC_SENSOR_ONE = 1;
	public static final int ULTRA_SONIC_SENSOR_TWO = 2;
	public static final int ULTRA_SONIC_SENSOR_THREE = 3;
	public static final int ULTRA_SONIC_SENSOR_FOUR = 4;
	public static final int ULTRA_SONIC_SENSOR_FIVE = 5;
	public static final int ULTRA_SONIC_SENSOR_SIX = 6;
	public static final int ULTRA_SONIC_SENSOR_SEVEN = 7;
	// Tachometer ID
	public static final int INFRA_RED_TACHOMETER_ID = 18;

	/**
	 * 
	 * @param sensorID
	 * @return sensor position on the car
	 */
	public static String getSensorPosition(int sensorID) {
		switch (sensorID) {
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
	
}
