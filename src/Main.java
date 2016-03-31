import vehicle.Pegasus.PegausVehicleProperties;
import logs.logger.PegasusLogger;
import Util.LinuxCommands;
import communication.serialPorts.SerialPortHandler;

import control.Controller;

public class Main {

	public static final String TAG = "MAIN";
	
	public static void main(String[] args) {
		PegausVehicleProperties.getInstance();
		if(2>1)return;
		
		if (LinuxCommands.attachedArduinoToSerialPort()
				&& LinuxCommands.enableBluetooth()) {
			Controller PegasusVehicleController = new Controller();
			PegasusVehicleController.bootCompleted();
		}else{
			PegasusLogger.getInstance().e(TAG, "main", "Program Could not start");//TODO - Maybe reset linux
		}
		// SerialPortHandler mSerialPortHandler;
		// mSerialPortHandler = SerialPortHandler.getInstance();
		// mSerialPortHandler.startThread();
		// while (true) {
		//
		// for (int i = 90; i <= 180; i++){
		// mSerialPortHandler.changeSpeed(i);
		// }
		// mSerialPortHandler.changeDrivingDirection("B");
		// for (int i = 90; i <= 180; i++){
		// mSerialPortHandler.changeSpeed(i);
		// }
		// mSerialPortHandler.changeDrivingDirection("F");
		// }
	}

}
