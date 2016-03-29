package communication.bluetooth.Server;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import communication.bluetooth.Constants.BluetoothServerStatus;
import communication.messages.MessageVaribles;

import control.Interfaces.IServerListener;

import java.io.IOException;
import java.util.HashMap;


public class BluetoothServer extends Thread {

    private static final String TAG = "Bluetooth Server";
    public static  BluetoothServer mBluetoothServer;
    
    private int mServerStatus;
    private final String uuid = "1101";
    private final String connectionString = "btspp://localhost:" + uuid + ";name=Pegasus";
    private LocalDevice mLocalDevice;
    private StreamConnectionNotifier mNotifier;
    private boolean isOnline;
    private HashMap<String,IServerListener> listeners;
    private HashMap<String,SocketData> clients;
   
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
        clients = new HashMap<>();
        listeners = new HashMap<String, IServerListener>();
        prepareBluetooth();
    }
    
	/**
	 * Register Listener
	 * @param name
	 * @param listener
	 */
	public void registerMessagesListener(String name,IServerListener listener){
		listeners.put(name,listener);
	}
	
	/**
	 * Unregister Listener
	 * @param name
	 */
	public void unRegisterMessagesListener(String name){
		if(listeners.containsKey(name))
			listeners.remove(name);
	}
	
	/**
	 * set server state
	 * @param status
	 */
	private void setServerState(int status){
		System.out.println(TAG + " Current Sserver Status: " + getServerStatusName() +
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
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
            setServerState(BluetoothServerStatus.DISCONNECTED);
            return;
        }
    	
    }
    
    public boolean isServerOnline(){
    	return isOnline;
    }
    
    public void startThread(){
    	start();
    }

    @Override
    public void run() {
        waitForConnection();
    }

    /** Waiting for connection from devices */

    private void waitForConnection() {
        while (isOnline) {
            try {
                System.out.println("waiting for connection...");
                //wait for connection
                final StreamConnection connection = mNotifier.acceptAndOpen();
                final RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(connection);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleClient(connection,remoteDevice);
                        } catch (Exception e) {
                            System.err.println(TAG +" :" + e.getMessage());
                            //TODO - should remove client?
                        }
                    }
                }).start();
            } catch (Exception e) {
                System.err.println(TAG + " " + e.getMessage());
                isOnline = false;
                setServerState(BluetoothServerStatus.DISCONNECTED);
                 return;
            }
        }//while
        
    }


    /**
     * Method handle Bluetooth client
     * @param clientSocket
     * @param remoteDevice
     * @throws InterruptedException 
     */
    private void handleClient(StreamConnection clientSocket,RemoteDevice remoteDevice) throws InterruptedException{
        SocketData client = new SocketData(clientSocket,remoteDevice);
        clients.put(client.getClientAddress(),client);
        System.out.println("client:" + client.getDeviceName() + "Address:" + client.getClientAddress());
        client.setConnected(true);
     try { 
        int available = 0;
        byte[] msg = null;
        StringBuilder receivedMsg = new StringBuilder();
        while(client.isConnected()){
	            while((available = client.getInputStream().available() ) > 0){
	            	msg = new byte[available + 1];
	            	client.getInputStream().read(msg);
	            	String last = "" + (char)msg[available - 1];
	            	if(last.equals(MessageVaribles.END_MESSAGE)){
	            		System.out.println(TAG + " : " + receivedMsg.toString());
	            		FireMessagesFromClient(receivedMsg.toString());
	            		receivedMsg = new StringBuilder();
	            	}
	            	else
	            		receivedMsg.append(new String(msg));
	            }
	            sleep(100);
        }//while
      }catch (IOException e){
           System.err.println(TAG + " : " + e.getMessage());
       }
    }
    
    
    /**
     * Shut Server Down
     */
    public void shutDownServer(){
    	try {
    	for(String clientAddress : clients.keySet()){
    		clients.get(clientAddress).getSocket().close();
    	}
    	isOnline = false;
		mNotifier.close();
		mBluetoothServer = null;
		setServerState(BluetoothServerStatus.DISCONNECTED);
		} catch (IOException e) {
			System.out.println(TAG +" " + e.getMessage() + " State:" + getServerStatusName());
		}
    }
    
    /**
     * Fire the incoming message to Controller
     * @param msg
     */
    private void FireMessagesFromClient(String msg){
    	for(String key : listeners.keySet())
    		listeners.get(key).onMessageReceivedFromClient(msg);
    }
    
    /**
     * update listeners when server is running
     */
    public void updateServerStatusChanged(int aStatusCode){
    	for(String key : listeners.keySet()){
    		listeners.get(key).onUpdateServerStatusChanged(aStatusCode);
    	}
    }
    
    

}