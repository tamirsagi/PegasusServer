package Control;

public interface OnMessagesListener {
	
	/**
	 * Handle message from inputStream
	 * @param msg
	 */
	void onMessageReceivedFromClient(String msg);
	

	
	/**
	 * Send message to client over bluetooth server
	 * @param msg
	 */
	void sendMessageToClient(String msg);
}
