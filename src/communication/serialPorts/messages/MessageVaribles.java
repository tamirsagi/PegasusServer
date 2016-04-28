package communication.serialPorts.messages;

import java.util.EnumSet;
import java.util.HashMap;


public class MessageVaribles {
	
	public static final int MESSAGE_TYPE_INFO = 1000;
	public static final int MESSAGE_TYPE_ACTION = 2000;
	public static final int MESSAGE_TYPE_SETTINGS = 3000;
	public static final int MESSAGE_TYPE_ERROR = 4000;
	
	public static final int MESSAGE_TYPE_INFO_HARDWARE_READY = 100;
	public static final int MESSAGE_TYPE_INFO_SERVER_READY = 200;
	
	
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
	
	
	public static final String KEY_SETTINGS_TYPE			= 		"ST";				//SA = Settings Action Type
	public static final String KEY_SETTINGS_SENSOR_PREFIX	= 		"S";				//SA = Settings Action Type
	public static final String KEY_VEHICLE_ACTION_TYPE		= 		"VA";				//VA = Vehicle Action Type
	public static final String KEY_DIGITAL_SPEED 			= 		"DS";				//DS = Digits Speed
	public static final String KEY_ROTATION_ANGLE			= 		"RA";				//RA = Rotation Angle for steering
	public static final String KEY_STEERING_DIRECTION		= 		"SD";				//SD = Steering direction either right or left
	public static final String KEY_DRIVING_DIRECTION		= 		"DD";				//DD = Driving Direction
	public static final String KEY_SENSOR_ID				=		"SID";				//Sensor ID
	public static final String KEY_SENSOR_STATE				=		"SS";				//Sensor State
	
	
	public static final String VALUE_STEERING_RIGHT			= 		"R";
	public static final String VALUE_STEERING_LEFT 			= 		"L";
	public static final String VALUE_STEERING_NONE 			= 		"N";
	public static final String VALUE_DRIVING_FORWARD		= 		"F";				//Forward
	public static final String VALUE_DRIVING_REVERSE		= 		"B";				//Backward
	
	
	public static final String KEY_SERIAL_PORT		 		= 		"SP";				//SP = Serial Port
	public static final String KEY_STEER_MOTOR 				= 		"SM";				//SM = Steer Motor
	public static final String KEY_BACK_MOTOR				= 		"BM";				//BM = Back Motor
	public static final String VALUE_OK						= 		"OK";				
	public static final String VALUE_ERROR					= 		"ER";	
	public static final String KEY_SENSOR_DATA		 		= 		"SD";				//Sensor Data
	
	
	public enum InfoType{
		STATUS, SENSOR_DATA
	}
	
	public static final int SETTINGS_SET_SENSORS = 3001;
	
	
}
