package vehicle.Sensor;

public class Constants {

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
