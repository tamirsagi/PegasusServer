package vehicle.common.constants;

/**
 * Handle various vehicle states
 * @author Tamir
 *
 */
public class VehicleAutonomousMode {
	
	public static final int VEHICLE_NONE = -1;
	public static final int VEHICLE_AUTONOMOUS_FREE_DRIVING = 0;
	public static final int VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING = 1;
	public static final int VEHICLE_AUTONOMOUS_PARKING_FOUND = 2;
	public static final int VEHICLE_AUTONOMOUS_PARKING = 3;
	public static final int VEHICLE_AUTONOMOUS_PARKED = 4;
	
	
	/**
	 * 
	 * @param value state code
	 * @return state name
	 */
	public static String getVehicleStateName(int value){
		switch(value){
		case VEHICLE_AUTONOMOUS_FREE_DRIVING:
			return "VEHICLE_FREE_DRIVING";
		case VEHICLE_AUTONOMOUS_LOOKING_FOR_PARKING:
			return "VEHICLE_LOOKING_FOR_PARKING";
		case VEHICLE_AUTONOMOUS_PARKING_FOUND:
			return "VEHICLE_PARKING_FOUND";
		case VEHICLE_AUTONOMOUS_PARKING:
			return "VEHICLE_PARKING";
		case VEHICLE_AUTONOMOUS_PARKED:
			return "VEHICLE_PARKED";
		default:
			return "UNKNOWN";
		
		}
	}

}
