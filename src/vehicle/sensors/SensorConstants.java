package vehicle.sensors;

public class SensorConstants {

	public static final int DISABLE_SENSOR = 0;
	public static final int ENABLE_SENSOR = 1;
	
	public static final int ULTRA_SONIC = 0;
	public static final int INFRA_RED = 1;

	public static String getTypeName(int type) {
		switch (type) {
		case ULTRA_SONIC:
			return "ULTRA SONIC";
		case INFRA_RED:
			return "INFRA_RED";
		default:
			return "UNKNOWN";
		}
	}

}
