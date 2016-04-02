import logs.logger.PegasusLogger;
import Util.LinuxCommands;

import control.Controller;

public class Main {

	public static final String TAG = "MAIN";
	
	public static void main(String[] args) {

		if (LinuxCommands.attachedArduinoToSerialPort()
				&& LinuxCommands.enableBluetooth()) {
			Controller.getInstance().bootCompleted();
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
