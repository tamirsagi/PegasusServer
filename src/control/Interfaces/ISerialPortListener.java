package control.Interfaces;

public interface ISerialPortListener {

	/**
	 * notified when serial port is ready
	 */
	void onSerialPortReady();
	
	/**
	 * notified when Hardware unit is ready
	 */
	void onHardwareReady();
	
	/**
	 * Handle incoming messages from Serial Port
	 * @param msg
	 */
	void onMessageReceivedFromHardwareUnit(String msg);
		
	
	
	/**
	 * Get Serial Port Status when changed
	 * @param status
	 */
	void onSerialStatusChanged(boolean status);
	
	
	/**
	 * Get Serial Port Erros
	 * @param msg
	 */
	void onSerialPortError(String msg);
		
}
