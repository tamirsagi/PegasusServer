package Util;

import logs.logger.PegasusLogger;

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
			deattachedArduinoToSerialPort();
			Process arduinoPortHandlerProcess = Runtime.getRuntime().exec(linkArduinoToPort);
			arduinoPortHandlerProcess.waitFor();
			return true;
		} catch (Exception e) {
			PegasusLogger.getInstance().e(TAG,"attachedArduinoToSerialPort",e.getMessage());
			return false;
		}
	}
	
	/* Link Arduino to Serial Port (Virtual Com)
	 * 
	 * @return
	 */
	public static boolean deattachedArduinoToSerialPort() {
		String[] unlinkArduinoToPort = new String[] { "sh", "-c",
		"sudo rm /dev/ttyS0" };
		try {
			Process arduinoPortHandlerProcess = Runtime.getRuntime().exec(unlinkArduinoToPort);
			arduinoPortHandlerProcess.waitFor();
			return true;
		} catch (Exception e) {
			PegasusLogger.getInstance().e(TAG,"deattachedArduinoToSerialPort",e.getMessage());
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
