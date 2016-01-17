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
		DRIVE, SETTINGS, CHANGE_DIRECTION
	}
	
	public static final char END_MESSAGE 					= 		'#';				//End Of Message
	public static final char MESSAGE_SAPERATOR				=		',';				//message sparator
	
	/*
	 *  Manual Control KEYS for incoming socket
	 */
	
	public static final String KEY_DIGITAL_SPEED 			= 		"DS";				//DS = Digits Speed
	public static final String KEY_ROTATION_ANGLE			= 		"RA";				//RA = Rotation Angle for steering
	public static final String KEY_STEERING_DIRECTION		= 		"direction";		//Steering direction either right or left
	public static final char   KEY_STERRING_RIGHT 			= 		'R';
	public static final char   KEY_STERRING_LEFT 			= 		'L';
	public static final char   KEY_STERRING_NONE 			= 		'N';
	
	
	
	/*    ACTION TO ARDUINO  */
	public static final int ACTION_DRIVING_DIRECTION = 0;
	public static final int ACTION_BACK_MOTOR = 1;
	public static final int ACTION_STEER_MOTOR = 2;
	
	
	/**
	 * Method build the mssage based on the protocol between Raspbery and Arduino
	 * @param action - Action To Arduino
	 * @param params - Relevant Parameters
	 * @return message to send over Arduino
	 */
	public static String getFixedMessageToArduino(int action,Object... params){
		StringBuilder messageToArduino = new StringBuilder();
		messageToArduino.append(action);
		for(Object s : params){
			messageToArduino.append(MESSAGE_SAPERATOR);
			messageToArduino.append(s);
		}
		messageToArduino.append(END_MESSAGE);
		
		return messageToArduino.toString();
	}
	
	
	
	
	
}
