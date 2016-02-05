package Control.Interfaces;

public interface ISerialPortListener {

	
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
