package Bluetooth;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import Control.GeneralParams;
import Control.OnMessagesListener;

public class BluetoothServer extends Thread {

    private static final String TAG = "Bluetooth Server";
    public static  BluetoothServer mBluetoothServer;
    private final String uuid = "1101";
    private final String connectionString = "btspp://localhost:" + uuid + ";name=Pegasus";
    private LocalDevice mLocalDevice;
    
    private boolean isOnline;
    private HashMap<String,OnMessagesListener> listeners;
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
        clients = new HashMap<>();
        listeners = new HashMap<String, OnMessagesListener>();
    }
    
	/**
	 * Register Listener
	 * @param name
	 * @param listener
	 */
	public void registerMessagesListener(String name,OnMessagesListener listener){
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
     * 
     * @return local Bluetooth Device
     */
    public LocalDevice getLocalDevice(){
    	return mLocalDevice;
    }
    

    @Override
    public void run() {
        waitForConnection();
    }

    /** Waiting for connection from devices */

    private void waitForConnection() {

        // retrieve the local Bluetooth device object
        isOnline = true;
        StreamConnectionNotifier notifier = null;
        // setup the server to listen for connection
        try {
        	mLocalDevice = LocalDevice.getLocalDevice();
            // generally discoverable, discoveryTimeout should be disabled - but isn't.
        	mLocalDevice.setDiscoverable(DiscoveryAgent.GIAC);
            notifier = (StreamConnectionNotifier) Connector.open(connectionString);
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        // waiting for connection
        while (isOnline) {
            try {
                System.out.println("waiting for connection...");
                //wait for connection
                final StreamConnection connection = notifier.acceptAndOpen();
                final RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(connection);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handleClient(connection,remoteDevice);
                        } catch (Exception e) {
                            System.err.println(TAG +" :" + e.getMessage());
                        }
                    }
                }).start();
            } catch (Exception e) {
                System.err.println(TAG + " " + e.getMessage());
                 return;
            }
        }
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
        System.out.println("client:" + client.getRemoteDevice() + "Address:" + client.getClientAddress());
        client.setConnected(true);
     try { 
        int available = 0;
        byte[] msg = null;
        StringBuilder receivedMsg = new StringBuilder();
        while(client.isConnected()){
	            while((available = client.getInputStream().available() ) > 0){
	            	msg = new byte[available];
	            	client.getInputStream().read(msg);
	            	if(msg[available - 1] == GeneralParams.END_MESSAGE){
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
    	isOnline = false;
    }
    
    /**
     * Fire the incoming message to Controller
     * @param msg
     */
    private void FireMessagesFromClient(String msg){
    	for(String key : listeners.keySet())
    		listeners.get(key).onMessageReceivedFromClient(msg);
    }

}