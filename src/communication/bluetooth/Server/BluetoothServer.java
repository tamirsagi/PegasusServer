package communication.bluetooth.Server;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;



import communication.bluetooth.Constants.BluetoothServerStatus;
import communication.messages.MessageVaribles;

import control.interfaces.OnServerEventsListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import logs.logger.PegasusLogger;


public class BluetoothServer extends Thread {

    private static final String TAG = "Bluetooth Server";
    public static  BluetoothServer mBluetoothServer;
    
    private int mServerStatus;
    private final String uuid = "1101";
    private final String connectionString = "btspp://localhost:" + uuid + ";name=Pegasus";
    private LocalDevice mLocalDevice;
    private StreamConnectionNotifier mNotifier;
    private boolean isOnline;
    private HashMap<String,OnServerEventsListener> mListeners;
    private HashMap<String,SocketData> mClients;
    private Queue<String> mMessagesToClients;
    private boolean mIsSuspended;
   
    /**
     * Get bluetooth server instance. (Singleton pattern)
     * @return server instance
     */
    public static BluetoothServer getInstance(){
    	if(mBluetoothServer == null)
    		mBluetoothServer = new BluetoothServer();
    	
    	return mBluetoothServer;
    }
    
    /** Constructor */
    private BluetoothServer() {
        setName(TAG);
        mServerStatus = BluetoothServerStatus.DISCONNECTED;
        mClients = new HashMap<>();
        mListeners = new HashMap<String, OnServerEventsListener>();
        mMessagesToClients = new LinkedList<String>();
    }
    
	/**
	 * Register Listener
	 * @param name
	 * @param listener
	 */
	public void registerMessagesListener(String name,OnServerEventsListener listener){
		PegasusLogger.getInstance().d(TAG,"registerMessagesListener", "Listener : " + name + " has registered");
		mListeners.put(name,listener);
	}
	
	/**
	 * Unregister Listener
	 * @param name
	 */
	public void unRegisterMessagesListener(String name){
		if(mListeners.containsKey(name))
			mListeners.remove(name);
	}
	
	/**
	 * set server state
	 * @param status
	 */
	private void setServerState(int status){
		PegasusLogger.getInstance().d(TAG ,"setServerState", "Current Sserver Status: " + getServerStatusName() +
				" new status is : " + BluetoothServerStatus.getServerStatusName(status));
		if(status != mServerStatus){
			mServerStatus = status;
			updateServerStatusChanged(mServerStatus);
		}
	}
	
	/**
	 * 
	 * @return server status name
	 */
	public String getServerStatusName(){
		return BluetoothServerStatus.getServerStatusName(getServerStatusCode());
	}
	
	/**
	 * 
	 * @return server status code
	 */
	private int getServerStatusCode(){
		return mServerStatus;
	}
    
    /**
     * 
     * @return local Bluetooth Device
     */
    public LocalDevice getLocalDevice(){
    	return mLocalDevice;
    }
    
    /**
     * Prepare bluetooth prior running the server
     */
    private void prepareBluetooth(){
        try {
        	mLocalDevice = LocalDevice.getLocalDevice();
            // generally discoverable, discoveryTimeout should be disabled - but isn't.
        	mLocalDevice.setDiscoverable(DiscoveryAgent.GIAC);
        	mServerStatus = BluetoothServerStatus.CONNECTING;
        	mNotifier = (StreamConnectionNotifier) Connector.open(connectionString);
        	isOnline = true;
        	setServerState(BluetoothServerStatus.CONNECTED);
        	PegasusLogger.getInstance().d(TAG ,"prepareBluetooth", "Server Is Connected");
        } catch (Exception e) {
        	PegasusLogger.getInstance().e(TAG ,"prepareBluetooth", e.getMessage());
            setServerState(BluetoothServerStatus.DISCONNECTED);
            return;
        }
    	
    }
    
    public boolean isServerOnline(){
    	return isOnline;
    }
    
    public void startThread(){
    	PegasusLogger.getInstance().d(TAG, "startThread", "Bluetooth Server is inisitaing...");
    	prepareBluetooth();
    	start();
    }

    @Override
    public void run() {
    	PegasusLogger.getInstance().d(TAG, "run", "Bluetooth Server Thread Started");
        waitForConnection();
    }

    /** Waiting for connection from devices */

    private void waitForConnection() {
        while (isOnline) {
        	try {
        	synchronized (this) {
				while(mIsSuspended){
					wait();
				}
			}
            PegasusLogger.getInstance().d(TAG, "waitForConnection", "Waiting for connections...");
            //wait for connection
            final StreamConnection connection = mNotifier.acceptAndOpen();
            final RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(connection);
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        	suspendThread();
                            handleClient(connection,remoteDevice);
                        } catch (Exception e) {
                        	PegasusLogger.getInstance().e(TAG ,"waitForConnection", e.getMessage());
                            //TODO - should remove client?
                        }
                    }
                }).start();
            } catch (Exception e) {
            	PegasusLogger.getInstance().e(TAG, "waitForConnection", e.getMessage());
                isOnline = false;
                setServerState(BluetoothServerStatus.DISCONNECTED);
                 return;
            }
        }//while
        
    }
    
    public void suspendThread(){
    	PegasusLogger.getInstance().i(getName(), "suspended...");
    	mIsSuspended = true;
    }
    
    public synchronized void resumeThread(){
    	PegasusLogger.getInstance().i(getName(), "resuming...");
    	mIsSuspended = false;
    	notifyAll();
    }


    /**
     * Method handle Bluetooth client
     * @param clientSocket
     * @param remoteDevice
     * @throws InterruptedException 
     */
    private void handleClient(StreamConnection clientSocket,RemoteDevice remoteDevice) throws InterruptedException{
        SocketData client = new SocketData(clientSocket,remoteDevice);
        mClients.put(client.getClientAddress(),client);
        PegasusLogger.getInstance().d(TAG, "handleClient", "client:" + client.getDeviceName() + " Address: " + client.getClientAddress());
        client.setConnected(true);
     try { 
        while(client.isConnected()){
	           readFromSerial(client);
	           sendMessageToClients();
	           sleep(1500);
        }//while
      }catch (IOException e){
    	  client.setConnected(false);
    	  mMessagesToClients.clear(); //now we have only one client
    	  PegasusLogger.getInstance().e(getName(), "handleClient", e.getMessage());
    	  mClients.remove(client).getClientAddress();
    	  resumeThread();
       }
    }
    
    private void readFromSerial(SocketData aClient) throws IOException{
    	 int available = 0;
         byte[] msg = null;
         StringBuilder receivedMsg = new StringBuilder();
         while((available = aClient.getInputStream().available() ) > 0){
         	msg = new byte[available + 1];
         	aClient.getInputStream().read(msg);
         	receivedMsg.append(new String(msg).trim());
         	pullMessages(receivedMsg);
         }
         if(receivedMsg.length() > 0 ){
				pullMessages(receivedMsg);
			}
    }
    
    
    /**
	 * pull all messages from current buffer
	 * if could not find either start or end symbol method will end.
	 * @param buffer -Message that received from stream
	 */
	private synchronized void pullMessages(StringBuilder buffer){
		int first = buffer.indexOf(MessageVaribles.START_MESSAGE);
		int last = buffer.indexOf(MessageVaribles.END_MESSAGE);
		String msgToSend  = "";
		while (first >= 0 && last >= 0) {
			if(first > last){ //in case half message received 
				buffer.delete(0, last + 1);
			}else{
				msgToSend = buffer.substring(first + 1, last);
				PegasusLogger.getInstance().d(getName(), "pullMessages", msgToSend);
				FireMessagesFromClient(msgToSend);
				buffer.delete(first, last + 1);
			}
			first = buffer.indexOf(MessageVaribles.START_MESSAGE);
			last = buffer.indexOf(MessageVaribles.END_MESSAGE);
		}
	}
    
    
    /**
     * Shut Server Down
     */
    public void shutDownServer(){
    	try {
    	for(String clientAddress : mClients.keySet()){
    		mClients.get(clientAddress).getSocket().close();
    	}
    	isOnline = false;
		mNotifier.close();
		mBluetoothServer = null;
		setServerState(BluetoothServerStatus.DISCONNECTED);
		PegasusLogger.getInstance().d(TAG, "shutDownServer", "Server shutted down");
		} catch (IOException e) {
			PegasusLogger.getInstance().e(TAG, "shutDownServer", e.getMessage() + " state:" + getServerStatusName());
		}
    }
    
    /**
     * Fire the incoming message to Controller
     * @param msg
     */
    private void FireMessagesFromClient(String msg){
    	for(String key : mListeners.keySet())
    		mListeners.get(key).onMessageReceivedFromClient(msg);
    }
    
    /**
     * update listeners when server is running
     */
    public void updateServerStatusChanged(int aStatusCode){
    	for(String key : mListeners.keySet()){
    		mListeners.get(key).onUpdateServerStatusChanged(aStatusCode);
    	}
    }
    
    /**
     * add message to send to clients
     * @param msg
     */
    public void addMessageToQueue(String msg){
    	if(msg!= null && !msg.isEmpty()){
    		mMessagesToClients.add(msg);
    	}
    }
    
    /**
     * send message to clients
     * @param msg
     */
    private void sendMessageToClients(){
    	while(!mMessagesToClients.isEmpty()){
	    	for(String client : mClients.keySet()){
	    		try {
	    			String msg = mMessagesToClients.poll();
	    			PegasusLogger.getInstance().i(getName(),"sendMessageToClients",msg);
					mClients.get(client).getOutputStream().write(msg.getBytes());
				} catch (IOException e) {
					PegasusLogger.getInstance().e(getName(),"sendMessageToClients",e.getMessage());
				}
	    	}
    	}
    }
    
    

}