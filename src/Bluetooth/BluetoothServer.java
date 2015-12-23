package Bluetooth;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.util.HashMap;

public class BluetoothServer extends Thread {

    private static final String TAG = "Bluetooth Server";
    private final String uuid = "1101";
    private final String connectionString = "btspp://localhost:" + uuid + ";name=Pegasus";

    private boolean isOnline;
    private HashMap<String,SocketData> clients;
    /** Constructor */

    public BluetoothServer() {
        setName(TAG);
        clients = new HashMap<>();
    }

    @Override
    public void run() {
        waitForConnection();
    }

    /** Waiting for connection from devices */

    private void waitForConnection() {

        // retrieve the local Bluetooth device object
        isOnline = true;
        LocalDevice local = null;
        StreamConnectionNotifier notifier = null;
        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            // generally discoverable, discoveryTimeout should be disabled - but isn't.
            local.setDiscoverable(DiscoveryAgent.GIAC);
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
                System.out.println("Server exception: " + e.getMessage());
                e.printStackTrace();
                 return;
            }
        }
    }


    /**
     * Method handle Bluetooth client
     * @param clientSocket
     * @param remoteDevice
     */
    private void handleClient(StreamConnection clientSocket,RemoteDevice remoteDevice){
        SocketData client = new SocketData(clientSocket,remoteDevice);
        clients.put(client.getClientAddress(),client);
        System.out.println("client:" + client.getClientAddress());
        System.out.println("socket: " + clientSocket + " remoteDevice:" + remoteDevice);
        client.setConnected(true);
     try { 
        String helloMsg = "hello from Pegasus Server";
        client.getOutputStream().write(helloMsg.getBytes());
        client.getOutputStream().flush();
        int available = 0;
        byte[] msg = null;
        while(client.isConnected()){
            while(client.getInputStream().available() > 0){
                msg = new byte[client.getInputStream().available()];
                client.getInputStream().read(msg);
                String receivedMsg = new String(msg);
                System.out.println(TAG + " : " + receivedMsg);
            }
        }//while
      }catch (IOException e){
           System.err.println(TAG + " : " + e.getMessage());
       }
    }

}