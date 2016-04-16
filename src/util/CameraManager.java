package util;

import java.io.File;
import java.io.IOException;

import logs.logger.PegasusLogger;

public class CameraManager {
	
	private static CameraManager mInstance;
	private static final String TAG = "Camera Manager";
	private static final String FILE_PATH = "";//TODO - TBD
	private static final String FILE_NAME = "output.jpg";
	private static final String IP_ADDRESS = "192.168.42.1";
	private static final int DEFAULT_ID = -1;
	private static final int COMMAND_INDEX = 2;
	
	private static final int PORT_NUMBER = 8090;
	private static int FPS = 100;
	private static int RESOLUTION_WIDTH = 640;
	private static int RESOLUTION_HEIGHT = 480;
	
	private int mUpperCameraProcessId = DEFAULT_ID;
	private boolean mIsCameraEnable;
	

	public static CameraManager getInstance(){
		if(mInstance == null){
			mInstance = new CameraManager();
		}
		return mInstance;
	}
	
	private CameraManager(){
		//TODO - load camera settings from file
	}
	
	/**
	 * turn Logitech camera on, the one on the roof.
	 * @return
	 */
	public boolean turnCameraOn(){
		String[] enableCameraCommand = new String[] {"sh", "-c",
				"sudo mjpg_streamer -i \"/usr/lib/input_uvc.so -d /dev/video0 -y -r 640x480 -f 10\" -o \"/usr/lib/output_http.so -p 8090 -w /var/www/mjpg_streamer\" -b"};
		try{
			Process cameraProcess = Runtime.getRuntime().exec(enableCameraCommand);
			cameraProcess.waitFor();
			cameraProcess.destroy();
			enableCameraCommand[COMMAND_INDEX] = "ps -C mjpg_streamer -o pid";
			cameraProcess = Runtime.getRuntime().exec(enableCameraCommand);
			String output = "";
			if(cameraProcess.waitFor() == 0){
				output = LinuxCommands.getInstance().getProcessInputStream(cameraProcess.getInputStream());
				String[] args = output.split(" ");
				mUpperCameraProcessId = Integer.parseInt(args[args.length - 1]);
				if(mUpperCameraProcessId > 0){
					PegasusLogger.getInstance().i(TAG, "turnCameraOn", "Camera is on, PID = " + mUpperCameraProcessId);
					mIsCameraEnable = true;
				}
			}
			
			System.out.println(mUpperCameraProcessId);
			cameraProcess.destroy();
			turnCameraOff();
			return true;
		}
		catch(Exception e){
			System.out.println(TAG +" " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Method use camera and take snapshop
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void takeSnapshot() throws InterruptedException, IOException{
		if(mIsCameraEnable){
			String[] command = new String[] {"sh", "-c",
					"wget http://192.168.42.1:8090/?action=snapshot -O " + FILE_PATH + FILE_NAME};
			Process snapshot = Runtime.getRuntime().exec(command);
			snapshot.waitFor();
			snapshot.destroy();
			if(new File(FILE_PATH + FILE_NAME).exists()){
				PegasusLogger.getInstance().i(TAG,"takeSnapShop()" , "Snapshot has been taken succefully");
				PegasusLogger.getInstance().i(TAG,"takeSnapShop()" , "Converting jpg file to bitmap..");
				command[COMMAND_INDEX] = "mogrify -format bmp" +  FILE_PATH + FILE_NAME;
				Process toBitmap = Runtime.getRuntime().exec(command);
				toBitmap.waitFor();
				toBitmap.destroy();
			}
		}
		
	}
	
	public void turnCameraOff(){
		if(mIsCameraEnable && 
			 LinuxCommands.getInstance().killProcess(mUpperCameraProcessId)){
			PegasusLogger.getInstance().i(TAG, "turnCameraOn", "roof camera is disabled");
			mUpperCameraProcessId = DEFAULT_ID;
			mIsCameraEnable = false;
		}
	}

}
