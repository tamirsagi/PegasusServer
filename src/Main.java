import Bluetooth.BluetoothServer;

public class Main {

    public static void main(String[] args) {
        System.out.println("creating server");
        BluetoothServer btServer = new BluetoothServer();
        btServer.start();
    }
}
