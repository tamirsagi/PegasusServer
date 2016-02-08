import communication.serialPorts.SerialPortHandler;

import control.Controller;

public class Main {

	public static void main(String[] args) {

		//Controller PegasusVehicleController = new Controller();
		SerialPortHandler mSerialPortHandler;
		mSerialPortHandler = SerialPortHandler.getInstance();
		mSerialPortHandler.startThread();
		//while (true) {
			for (int i = 90; i <= 100; i++){
				//mSerialPortHandler.changeSpeed(i);
				mSerialPortHandler.changeSteerMotor("L", i);
			}
			mSerialPortHandler.changeDrivingDirection("B");
			
//			for (int i = 50; i <= 90; i++){
//				//mSerialPortHandler.changeSpeed(i);
//				mSerialPortHandler.changeSteerMotor('L', i);
//			}
//			mSerialPortHandler.changeDrivingDirection('F');
//			for(long  i = 0; i < 1000000; i ++);
		}
	//}
}
