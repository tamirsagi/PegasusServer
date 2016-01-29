package Control.Interfaces;

public interface IServerListener {
	
	
	/**
	 * Handle message from inputStream
	 * @param msg
	 */
	void onMessageReceivedFromClient(String msg);
	
	
	
}
