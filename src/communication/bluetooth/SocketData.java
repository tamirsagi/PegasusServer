package communication.bluetooth;
 

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;
import java.io.*;

/**
 * @author Tamir Sagi
 */
public class SocketData {
    private static final  String TAG = "SocketData";

    private StreamConnection clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String remoteAddress;
    private String remoteName;
    private boolean isConnected;
    private RemoteDevice remoteDevice;

    public SocketData(StreamConnection clientSocket, RemoteDevice remoteDevice) {
        this.clientSocket = clientSocket;
        this.remoteDevice = remoteDevice;
        try {
            inputStream = clientSocket.openInputStream();
            outputStream = clientSocket.openOutputStream();
            remoteAddress = remoteDevice.getBluetoothAddress();
            remoteName = remoteDevice.getFriendlyName(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StreamConnection getSocket() {
        return clientSocket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getClientAddress() {
        return remoteAddress;
    }

    public RemoteDevice getRemoteDevice(){
        return remoteDevice;
    }
    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }
    
    public String getDeviceName(){
    	return remoteName;
    }
}
