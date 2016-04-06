package vehicle.common.constants;

/**
 * Handle various vehicle states
 * @author Tamir
 *
 */
public class VehicleState {
	
	public static final int VEHICLE_DEFAULT = -1;
	public static final int VEHICLE_FREE_DRIVING = 0;
	public static final int VEHICLE_LOOKING_FOR_PARKING = 1;
	public static final int VEHICLE_PARKING_FOUND = 2;
	public static final int VEHICLE_PARKING = 3;
	public static final int VEHICLE_PARKED = 4;
	
	
	/**
	 * 
	 * @param value state code
	 * @return state name
	 */
	public static String getVehicleStateName(int value){
		switch(value){
		case VEHICLE_DEFAULT:
			return "VEHICLE_STOPPED";
		case VEHICLE_FREE_DRIVING:
			return "VEHICLE_FREE_DRIVING";
		case VEHICLE_LOOKING_FOR_PARKING:
			return "VEHICLE_LOOKING_FOR_PARKING";
		case VEHICLE_PARKING_FOUND:
			return "VEHICLE_PARKING_FOUND";
		case VEHICLE_PARKING:
			return "VEHICLE_PARKING";
		case VEHICLE_PARKED:
			return "VEHICLE_PARKED";
		default:
			return "UNKNOWN";
		
		}
	}

}
