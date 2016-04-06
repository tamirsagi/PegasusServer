package vehicle.common.constants;

import java.util.EnumSet;
import java.util.HashMap;

public class VehicleParams {
	
	public static final int DISABLE_SENSOR = 0;
	public static final int ENABLE_SENSOR = 1;

	public enum VehicleControlType{
		MANUAL, AUTONOMOUS;
	}
	
	
	public enum DrivingDirection{
		FORWARD, REVERSE
	}
	
	/**
	 * Actions a vehicle is capable to do
	 */
	public enum VehicleActions{
		CHANGE_DIRECTION(0),CHANGE_SPEED(1),STEERING(2), CHANGE_SENSOR_STATE(3);
		
		private int value;
		 
		 public static HashMap<Integer,VehicleActions> actions = new HashMap<Integer,VehicleActions>();
		 static {
			 for(VehicleActions m : EnumSet.allOf(VehicleActions.class))
				 actions.put(m.getValue(),m);
		 }
		 
		 VehicleActions(int value){
			 this.value = value;
		 }
		 
		 public int getValue(){
			 return value;
		 }
		 
		 public static VehicleActions getMessageType(int type){
			 return actions.get(type);
		 }
	}
	
	
	
	
	
	
}
