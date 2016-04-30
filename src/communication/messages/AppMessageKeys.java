package communication.messages;

/**
 * Hold keys for messages bluetooth <-> Pegasus Application
 * @author pi
 *
 */
public class AppMessageKeys {
	
	public static final String KEY_VEHICLE_MODE				=		"VM"; 				//Vehicle Mode
	public static final String KEY_AUTONOMOUS_MODE			=		"AM"; 				//Autonomous Mode
	public static final int AUTONOMOUS_MODE_AUTO_DRIVE = 0;
	public static final int AUTONOMOUS_MODE_FIND_PARKING = 1;
	
	public static final String JSON_KEY_REAL_TIME_DATA_TYPE = "real_time_data_type";
	public static final int REAL_TIME_DATA_TYPE_DISTANCE = 0;
	public static final int REAL_TIME_DATA_TYPE_SPEED = 1;
	public static final int REAL_TIME_DATA_TYPE_SENSOR = 2;
	
	public static final String JSON_KEY_DISTANCE = "distance";
	public static final String JSON_KEY_SPEED = "speed";
	
	public static final String KEY_MESSAGE_TYPE_LOG = "log";
	public static final String KEY_MESSAGE_TYPE_REAL_TIME_DATA = "real_time_data";
	
	

}
