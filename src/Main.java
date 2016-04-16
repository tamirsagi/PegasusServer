import util.LinuxCommands;
import vehicle.pegasus.PegausVehicleProperties;
import logs.logger.PegasusLogger;

import control.Controller;

public class Main {

	public static final String TAG = "MAIN";
	
	public static void main(String[] args) {

		if (LinuxCommands.getInstance().attachedArduinoToSerialPort()
				&& LinuxCommands.getInstance().enableBluetooth()) {
			Controller.getInstance().bootCompleted();
		}else{
			PegasusLogger.getInstance().e(TAG, "main", "Program Could not start");//TODO - Maybe reset linux
		}
	}

}
