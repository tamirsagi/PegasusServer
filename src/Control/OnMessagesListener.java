package Control;

public interface OnMessagesListener {
	
	/**
	 * Get Server Status when changed
	 * @param status
	 */
	void onServerStatus(boolean status);
	
	/**
	 * Get Serial Port Status when changed
	 * @param status
	 */
	void onSerialStatus(boolean Status);
	/**
	 * Handle message from inputStream
	 * @param msg
	 */
	void onMessageReceivedFromClient(String msg);
	

	/**
	 * Handle message from Arduino via Serial Port
	 * @param msg
	 */
	void onMessageReceivedFromSerialPort(String msg);
	
	
	/**
	 * Send message to client over bluetooth server
	 * @param msg
	 */
	void sendMessageToClient(String msg);
	
	
}
