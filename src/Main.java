
import util.CameraManager;
import util.LinuxCommands;
import logs.logger.PegasusLogger;

import control.Controller;

public class Main {

	public static final String TAG = "MAIN";
	
	public static void main(String[] args) {

//		//CameraManager.getInstance().turnCameraOn();
//		//CameraManager.getInstance().takeSnapshot();
//		//CameraManager.getInstance().turnCameraOff();
//		//has the snap
//		BufferedImage bim = ImageIO.read(new File(CameraManager.FILE_PATH + CameraManager.FILE_NAME));
//		int[][] array = new int[bim.getWidth()][bim.getHeight()];
//		for(int j = 155, i = 310; i >= 0; i--){
//			//for(int j = 0; j < bim.getWidth(); j++){
//				array[i][j] = bim.getRGB(j,i);
//				Color color = new Color(array[i][j]);
//				System.out.println(String.format("i=%s,j=%s,color=%s", i,j,color.toString()));
//		}
		
//		if (LinuxCommands.getInstance().attachedArduinoToSerialPort()
//				&& LinuxCommands.getInstance().enableBluetooth()) {
//			BluetoothServer.getInstance().registerMessagesListener(TAG, Controller.getInstance());
//			BluetoothServer.getInstance().startThread();
//		}
//		if(2>1)
//			return ;
		Thread.currentThread().setName("Main Program");
		if (LinuxCommands.getInstance().attachedArduinoToSerialPort()
				&& LinuxCommands.getInstance().enableBluetooth()) {
				Controller.getInstance().bootCompleted();
		}else{
			PegasusLogger.getInstance().e(TAG, "main", "Program Could not start");
			//TODO - Maybe reset linux
		}
	}

}
