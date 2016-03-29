package communication.bluetooth.Constants;

public class BluetoothServerStatus {
	
	public static final int DISCONNECTED = 0;
	public static final int CONNECTING = 1;
	public static final int CONNECTED = 2;
	
	
	/**
	 * 
	 * @param code - status code
	 * @return Bluetooth server status name
	 */
	public static String getServerStatusName(int code){
		switch(code){
		case DISCONNECTED:
			return "DISCONNECTED";
		case CONNECTING:
			return "CONNECTING";
		case CONNECTED:
			return "CONNECTED";
		default:
			return "UNKNOWN";
		}
	}
	

}
