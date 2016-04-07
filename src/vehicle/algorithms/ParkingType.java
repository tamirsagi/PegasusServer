package vehicle.algorithms;

public class ParkingType {

	public static final int PARALLEL_RIGHT = 0;
	public static final int PARALLEL_LEFT = 1;
	public static final int ANGULAR_RIGHT = 2;
	public static final int ANGULAR_LEFT = 3;
	
	
	public static String getParkingTypeName(int parkingCode){
		switch(parkingCode){
		case PARALLEL_RIGHT:
			return "PARALLEL_RIGHT";
		case PARALLEL_LEFT:
			return "PARALLEL_LEFT";
		case ANGULAR_RIGHT:
			return "ANGULAR_RIGHT";
		case ANGULAR_LEFT:
			return "ANGULAR_LEFT";
		default:
			return "UNKNOWN";
		
		}
	}

}
