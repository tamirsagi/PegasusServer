package vehicle.common.constants;


public class VehicleParams {
	
	public static final int VEHICLE_MODE_NONE = -1;
	public static final int VEHICLE_MODE_AUTONOMOUS = 0;
	public static final int VEHICLE_MODE_MANUAL = 1;
	
	public static final int VEHICLE_ACTION_CHANGE_DIRECTION = 2001;
	public static final int VEHICLE_ACTION_CHANGE_SPEED = 2002;
	public static final int VEHICLE_ACTION_CHANGE_STEERING = 2003;
	public static final int VEHICLE_ACTION_CHANGE_SENSOR_STATE = 2004;
	
	
	public enum DrivingDirection{
		FORWARD, BACKWARD
	}
	
	
	
}
