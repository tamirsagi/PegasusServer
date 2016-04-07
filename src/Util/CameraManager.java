package Util;

import logs.logger.PegasusLogger;

public class CameraManager {
	
	private static CameraManager mInstance;
	private static final String TAG = "Camera Manager";
	private static final int DEFAULT_ID = -1;
	private static final int COMMAND_INDEX = 2;
	
	private static final int mPort = 8090;
	private static int mFPS = 100;
	private static int res_width = 640;
	private static int res_height = 480;
	
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
	
	public void turnCameraOff(){
		if(mIsCameraEnable && 
			 LinuxCommands.getInstance().killProcess(mUpperCameraProcessId)){
			PegasusLogger.getInstance().i(TAG, "turnCameraOn", "roof camera is disabled");
			mUpperCameraProcessId = DEFAULT_ID;
			mIsCameraEnable = false;
		}
	}

}
