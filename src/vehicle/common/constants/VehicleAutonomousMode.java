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
	public static final int VEHICLE_AUTONOMOUS_POSITIONING_VEHICLE_PARALLEL = 2;
	public static final int VEHICLE_AUTONOMOUS_READY_TO_PARK = 3;
	public static final int VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING = 4;
	public static final int VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING = 5;
	
	
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
		case VEHICLE_AUTONOMOUS_POSITIONING_VEHICLE_PARALLEL:
			return "VEHICLE_AUTONOMOUS_POSITIONING_VEHICLE_PARALLEL";
		case VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING:
			return "VEHICLE_AUTONOMOUS_MANUEVERING_INTO_PARKING";
		case VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING:
			return "VEHICLE_AUTONOMOUS_MANUEVERING_OUT_OF_PARKING";
		default:
			return "UNKNOWN";
		
		}
	}

}
