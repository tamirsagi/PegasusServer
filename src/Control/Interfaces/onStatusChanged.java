package Control.Interfaces;

public interface onStatusChanged {

	/**
	 * Get Server Status when changed
	 * @param status
	 */
	void onServerStatusChanged(boolean status);
	
	/**
	 * Get Serial Port Status when changed
	 * @param status
	 */
	void onSerialStatusChanged(boolean Status);
	
}
