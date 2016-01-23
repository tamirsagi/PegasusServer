package Control;

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
	
	/**
	 * Get Server Status when changed
	 * @param status
	 */
	void onHardwareStatusChanged(boolean status);
	
}
