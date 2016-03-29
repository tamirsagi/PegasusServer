package control.Interfaces;

public interface IServerListener {
	
	/**
	 * update server status
	 * @param code
	 */
	void onUpdateServerStatusChanged(int code);
	
	/**
	 * Handle message from inputStream
	 * @param msg
	 */
	void onMessageReceivedFromClient(String msg);
	
	
}
