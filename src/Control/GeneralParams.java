package Control;

public class GeneralParams {
	
	public static final String KEY_MESSAGE_TYPE = "type";
	
	/*
	 *  Message Entries
	 */
	
	public enum MessageType{
		ACTION
	}
	
	public enum ActionType{
		CAHNGE_SPEED,STEERING, SETTINGS, CHANGE_DIRECTION
	}
	
	public static final char END_MESSAGE 					= 		'#';				//End Of Message
	public static final char MESSAGE_SAPERATOR				=		',';				//message sparator
	
	/*
	 *  Messaging Protocol Abbreviations
	 */
	public static final String KEY_ACTION_TYPE		 		= 		"AT";				//AT = Action Type
	public static final String KEY_DIGITAL_SPEED 			= 		"DS";				//DS = Digits Speed
	public static final String KEY_ROTATION_ANGLE			= 		"RA";				//RA = Rotation Angle for steering
	public static final String KEY_STEERING_DIRECTION		= 		"SD";				//SD = Steering direction either right or left
	public static final char   VALUE_STEERING_RIGHT			= 		'R';
	public static final char   VALUE_STEERING_LEFT 			= 		'L';
	public static final char   VALUE_STEERING_NONE 			= 		'N';
	public static final String KEY_DRIVING_DIRECTION		= 		"DD";				//DD = Driving Direction
	public static final char   VALUE_DRIVING_FORWARD		= 		'F';				//Forward
	public static final char   VALUE_DRIVING_BACKWARD		= 		'B';				//Backward
	
	
	/*    ACTION TO ARDUINO  */
	public static final int ACTION_DRIVING_DIRECTION = 0;
	public static final int ACTION_BACK_MOTOR = 1;
	public static final int ACTION_STEER_MOTOR = 2;
	
	public static final int ACTION_SERVER_READY = 200;
	
	
	
	
}
