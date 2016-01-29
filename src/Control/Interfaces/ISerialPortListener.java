package Control.Interfaces;

public interface ISerialPortListener {

	
	/**
	 * Handle incoming messages from Serial Port
	 * @param msg
	 */
	void onMessageReceivedFromSerialPort(String msg);
		
		
}
