package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import logs.logger.PegasusLogger;

public class LinuxCommands {

	public static final String TAG = "LinuxCommands";
	private static LinuxCommands mInstance;
	
	
	public static LinuxCommands getInstance(){
		if(mInstance == null){
			mInstance = new LinuxCommands();
		}
		return mInstance;
	}
	
	private LinuxCommands(){
		
	}

	/**
	 * Link Arduino to Serial Port (Virtual Com)
	 * 
	 * @return
	 */
	public boolean attachedArduinoToSerialPort() {
		String[] linkArduinoToPort = new String[] { "sh", "-c",
				"sudo ln -s /dev/ttyACM0 /dev/ttyS0" };
		try {
			//in case its already linked
			deattachedArduinoToSerialPort();
			Process arduinoPortHandlerProcess = Runtime.getRuntime().exec(linkArduinoToPort);
			arduinoPortHandlerProcess.waitFor();
			arduinoPortHandlerProcess.destroy();
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
	public boolean deattachedArduinoToSerialPort() {
		String[] unlinkArduinoToPort = new String[] { "sh", "-c",
		"sudo rm /dev/ttyS0" };
		try {
			Process arduinoPortHandlerProcess = Runtime.getRuntime().exec(unlinkArduinoToPort);
			arduinoPortHandlerProcess.waitFor();
			arduinoPortHandlerProcess.destroy();
			return true;
		} catch (Exception e) {
			PegasusLogger.getInstance().e(TAG,"deattachedArduinoToSerialPort",e.getMessage());
			return false;
		}
	}

	/**
	 * enable bluetooth in Linux
	 */
	public boolean enableBluetooth(){
		String[] enableBluetooth = new String[] {"sh", "-c","sudo service bluetooth start"};
		try{
			
			Process enableBluetoothProccess = Runtime.getRuntime().exec(enableBluetooth);
			enableBluetoothProccess.waitFor();
			enableBluetoothProccess.destroy();
			return true;
		}
		catch(Exception e){
			System.out.println(TAG +" " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * enable bluetooth in Linux
	 */
	public boolean killProcess(int aProcessId){
		String[] killProcess = new String[] {"sh", "-c","sudo kill " + aProcessId};
		try{
			Process kill = Runtime.getRuntime().exec(killProcess);
			kill.waitFor();
			return true;
		}
		catch(Exception e){
			System.out.println(TAG +" " + e.getMessage());
			return false;
		}
	}
	
	
	/**
	 * return incoming data via process
	 * @param aInput - process input stream
	 * @return string with input from process
	 * @throws IOException
	 */
	public String getProcessInputStream(InputStream aInput) throws IOException{
		StringBuilder input = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(aInput));
		String line = "";
		while((line = in.readLine()) != null){
			input.append(line);
		}
		in.close();
		return input.toString();
	}
	
	
}
