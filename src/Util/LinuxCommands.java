package Util;

public class LinuxCommands {

	public static final String TAG = "LinuxCommands";

	/**
	 * Link Arduino to Serial Port (Virtual Com)
	 * 
	 * @return
	 */
	public static boolean attachedArduinoToSerialPort() {
		String[] linkArduinoToPort = new String[] { "sh", "-c",
				"sudo ln -s /dev/ttyACM0 /dev/ttyS0" };
		try {
			Process linkArduinoToPortProccess = Runtime.getRuntime().exec(
					linkArduinoToPort);
			linkArduinoToPortProccess.waitFor();
			return true;
		} catch (Exception e) {
			System.out.println(TAG + " " + e.getMessage());
			return false;
		}

	}

	/**
	 * enable bluetooth in Linux
	 */
	public static boolean enableBluetooth(){
		String[] enableBluetooth = new String[] {"sh", "-c","sudo service bluetooth start"};
		try{
			
			Process enableBluetoothProccess = Runtime.getRuntime().exec(enableBluetooth);
			enableBluetoothProccess.waitFor();
			return true;
		}
		catch(Exception e){
			System.out.println(TAG +" " + e.getMessage());
			return false;
		}
		
	}
}
