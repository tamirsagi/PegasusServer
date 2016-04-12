package control.interfaces;

public interface OnSerialPortEventsListener {

	/**
	 * notifies changes in serial port
	 */
	void onSerialPortStateChanged(boolean aIsReady);
	
	
	/**
	 * notifies changes in hardware
	 */
	void updateHardwareStatus(int aStatusCode);
	
	
	/**
	 * Get Serial Port Erros
	 * @param msg
	 */
	void onSerialPortError(String msg);
		
}
