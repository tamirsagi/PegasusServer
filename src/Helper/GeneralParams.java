package Helper;

import java.util.EnumSet;
import java.util.HashMap;

public class GeneralParams {
	
	
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
	
	public enum Vehicle_Control{
		MANUAL, AUTONOMOUS;
	}
	
	public enum Action_Type{
		VEHICLE_ACTION, SETTINGS;
	}
	
	public enum SteeringDirection{
		FORDWARD, BACKWARD
	}
	
	/**
	 * Actions a vehicle is capable to do
	 */
	public enum Vehicle_Actions{
		CHANGE_DIRECTION(0),CHANGE_SPEED(1),STEERING(2);
		
		private int value;
		 
		 public static HashMap<Integer,Vehicle_Actions> actions = new HashMap<Integer,Vehicle_Actions>();
		 static {
			 for(Vehicle_Actions m : EnumSet.allOf(Vehicle_Actions.class))
				 actions.put(m.getValue(),m);
		 }
		 
		 Vehicle_Actions(int value){
			 this.value = value;
		 }
		 
		 public int getValue(){
			 return value;
		 }
		 
		 public static Vehicle_Actions getMessageType(int type){
			 return actions.get(type);
		 }
	}
	
	public enum Info_Type{
		STATUS
	}
	
	
	
	
	/*
	 *  Messaging Protocol Abbreviations
	 */
	
	public static final String KEY_MESSAGE_TYPE 			= 		"MT";
	public static final String START_MESSAGE 				= 		"$";				//End Of Message
	public static final String END_MESSAGE 					= 		"#";				//End Of Message
	public static final String MESSAGE_SAPERATOR			=		",";				//message sparator
	public static final String MESSAGE_KEY_VALUE_SAPERATOR 	= 		":";
	
	
	
	/*
	 *  Outgoing
	 */
	public static final String KEY_VEHICLE_ACTION_TYPE		= 		"VA";				//VA = Vehicle Action Type
	public static final String KEY_DIGITAL_SPEED 			= 		"DS";				//DS = Digits Speed
	public static final String KEY_ROTATION_ANGLE			= 		"RA";				//RA = Rotation Angle for steering
	public static final String KEY_STEERING_DIRECTION		= 		"SD";				//SD = Steering direction either right or left
	public static final char   VALUE_STEERING_RIGHT			= 		'R';
	public static final char   VALUE_STEERING_LEFT 			= 		'L';
	public static final char   VALUE_STEERING_NONE 			= 		'N';
	public static final String KEY_DRIVING_DIRECTION		= 		"DD";				//DD = Driving Direction
	public static final char   VALUE_DRIVING_FORWARD		= 		'F';				//Forward
	public static final char   VALUE_DRIVING_BACKWARD		= 		'B';				//Backward
	
	
	/*
	 *  Incoming
	 */
	
	public static final String KEY_INFO_TYPE				=		"IT";				//Info Type Key
	public static final String KEY_STATUS			 		= 		"ST";				//SP = Serial Port
	public static final String KEY_SERIAL_PORT		 		= 		"SP";				//SP = Serial Port
	public static final String KEY_STEER_MOTOR 				= 		"SM";				//SM = Steer Motor
	public static final String KEY_BACK_MOTOR				= 		"BM";				//BM = Back Motor
	public static final String VALUE_OK						= 		"OK";				
	public static final String VALUE_ERROR					= 		"ER";				
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/*  INFO CODE */
	 
	public static final int INFO_HARDWARE_STATUS_READY = 100;
	
	public static final int INFO_SERVER_STATUS_READY = 200;
	
	
	
	
	
}
