package communication.messages;

import java.util.EnumSet;
import java.util.HashMap;


public class MessageVaribles {

	public static final int INFO = 1000;
	public static final int ACTION = 2000;
	public static final int WARNING = 3000;
	public static final int ERROR = 4000;
	/*
	 *  Message Entries
	 */
	
	public enum MessageType{
		 INFO(1000), ACTION(2000), WARNING(3000), ERROR(4000);
		 
		 private int value;
		 
		 public static HashMap<Integer,MessageType> types = new HashMap<Integer,MessageType>();
		 static {
			 for(MessageType m : EnumSet.allOf(MessageType.class))
				 types.put(m.getValue(),m);
		 }
		 
		 MessageType(int value){
			 this.value = value;
		 }
		 
		 public int getValue(){
			 return value;
		 }
		 
		 public static MessageType getMessageType(int type){
			 return types.get(type);
		 }
		 
	}
	
	
	public enum Action_Type{
		VEHICLE_ACTION, SETTINGS;
	}
	
	
	/*
	 *  Messaging Protocol Abbreviations
	 */
	
	public static final String KEY_MESSAGE_TYPE 			= 		"MT";
	public static final String START_MESSAGE 				= 		"$";				//End Of Message
	public static final String END_MESSAGE 					= 		"#";				//End Of Message
	public static final String MESSAGE_SAPERATOR			=		",";				//message sparator
	public static final String MESSAGE_KEY_VALUE_SAPERATOR 	= 		":";
	public static final String KEY_INFO_TYPE				=		"IT";				//Info Type Key
	public static final String KEY_STATUS			 		= 		"ST";				//Status
	
	
	public static final String KEY_SETTINGS_ACTION_TYPE		= 		"SA";				//SA = Settings Action Type
	public static final String KEY_VEHICLE_ACTION_TYPE		= 		"VA";				//VA = Vehicle Action Type
	public static final String KEY_DIGITAL_SPEED 			= 		"DS";				//DS = Digits Speed
	public static final String KEY_ROTATION_ANGLE			= 		"RA";				//RA = Rotation Angle for steering
	public static final String KEY_STEERING_DIRECTION		= 		"SD";				//SD = Steering direction either right or left
	public static final String KEY_DRIVING_DIRECTION		= 		"DD";				//DD = Driving Direction
	public static final String KEY_SENSOR_ID				=		"SID";				//Sensor ID
	public static final String KEY_SENSOR_STATE				=		"SS";				//Sensor State
	
	
	public static final String 	 VALUE_STEERING_RIGHT		= 		"R";
	public static final String   VALUE_STEERING_LEFT 		= 		"L";
	public static final String   VALUE_STEERING_NONE 		= 		"N";
	public static final String   VALUE_DRIVING_FORWARD		= 		"F";				//Forward
	public static final String   VALUE_DRIVING_REVERSE		= 		"B";				//Backward
	
	
	public static final String KEY_SERIAL_PORT		 		= 		"SP";				//SP = Serial Port
	public static final String KEY_STEER_MOTOR 				= 		"SM";				//SM = Steer Motor
	public static final String KEY_BACK_MOTOR				= 		"BM";				//BM = Back Motor
	public static final String VALUE_OK						= 		"OK";				
	public static final String VALUE_ERROR					= 		"ER";	
	public static final String KEY_SENSOR_DATA		 		= 		"SD";				//Sensor Data
	
	
	public enum InfoType{
		STATUS, SENSOR_DATA
	}

	/*  Status CODE */
	 
	public enum StatusCode{
		INFO_HARDWARE_STATUS_READY(100), INFO_SERVER_STATUS_READY(200);
		
		public static HashMap<Integer,StatusCode> status_codes = new HashMap<Integer,StatusCode>();
		 static {
			 for(StatusCode m : EnumSet.allOf(StatusCode.class))
				 status_codes.put(m.getStatusCode(),m);
		 }
		private int code;
		
		StatusCode(int code){
			this.code = code;
		}
		
		public int getStatusCode(){
			return code;
		}
		
		public static StatusCode get(int code){
			return status_codes.get(code);
		}
	}
	
}
