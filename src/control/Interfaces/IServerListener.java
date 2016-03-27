package control.Interfaces;

public interface IServerListener {
	
	/**
	 * notifies when server is ready
	 */
	void onServerReady();
	
	/**
	 * Handle message from inputStream
	 * @param msg
	 */
	void onMessageReceivedFromClient(String msg);
	
	
	/**
	 * Get Server Status when changed
	 * @param isReady
	 */
	void onServerStatusChanged(boolean isReady);
	
	
	
	/**
	 * Get Server Erros
	 * @param msg
	 */
	void onServerError(String msg);
	
}
