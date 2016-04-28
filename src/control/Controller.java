package control;

import logs.logger.PegasusLogger;
import managers.driving_manager.DrivingManager;

import org.json.JSONException;
import org.json.JSONObject;

import vehicle.common.constants.VehicleParams;
import vehicle.pegasus.PegasusVehicle;
import vehicle.pegasus.PegausVehicleProperties;

import communication.bluetooth.Constants.BluetoothServerStatus;
import communication.bluetooth.Server.BluetoothServer;
import communication.serialPorts.SerialPortHandler;
import communication.serialPorts.messages.MessageVaribles;

import control.constants.ApplicationStates;
import control.interfaces.OnSerialPortEventsListener;
import control.interfaces.OnServerEventsListener;
import control.interfaces.OnVehicleEventsListener;

public class Controller implements OnServerEventsListener, 
									OnSerialPortEventsListener,OnVehicleEventsListener{

	public static final String TAG = Controller.class.getSimpleName();
	private static Controller mInstance;
	private boolean mIsServerReady;
	private boolean mIsSerialPortReady;
	private boolean mIsHardwareReady;
	private StringBuilder mExtraDataToSend;
	
	private int mApplicationState = ApplicationStates.BOOTING;

	public static Controller getInstance(){
		if(mInstance == null){
			mInstance = new Controller();
		}
		return mInstance;
	}
	private  Controller() {
		mExtraDataToSend = new StringBuilder();
	}
	
	/**
	 * initialize system when boot is completed
	 */
	public void bootCompleted() {
		PegasusLogger.getInstance().d(TAG, "bootCompleted", "System Booted up");
		setState(ApplicationStates.INITIALIZE_VEHICLE);
	}

	/**
	 * change application states
	 * 
	 * @param state
	 */
	public void setState(int state) {
		PegasusLogger.getInstance().d(TAG,"setState", "State was "
				+ ApplicationStates.getStateName(mApplicationState)
				+ " and changed to:" + ApplicationStates.getStateName(state));
		mApplicationState = state;
		switch (state) {
		case ApplicationStates.INITIALIZE_VEHICLE:
			PegasusVehicle.getInstance().notifyWhenReady(this);
			break;
		case ApplicationStates.VEHICLE_READY:
			break;
		case ApplicationStates.INITIALIZE_SERIAL_PORT:
			SerialPortHandler.getInstance().registerStatesListener(this);
			SerialPortHandler.getInstance().startThread();
			break;
		case ApplicationStates.SERIAL_PORT_READY:
			break;
		case ApplicationStates.WAITING_FOR_HARDWARE:
			break;
		case ApplicationStates.HARDWARE_READY:
			BluetoothServer.getInstance().registerMessagesListener(TAG, this);
			BluetoothServer.getInstance().startThread();
			mApplicationState = ApplicationStates.WAITING_FOR_SERVER;
			break;
		case ApplicationStates.WAITING_FOR_SERVER:
			// TODO - notify arduino maybe? decide what happand
			break;
		case ApplicationStates.SERVER_READY:
			PegasusVehicle.getInstance().setName(
					BluetoothServer.getInstance().getLocalDevice()
							.getFriendlyName());
			mApplicationState = ApplicationStates.READY;
			PegasusLogger.getInstance().i(TAG,"Both Hardware and server Are ready");
		case ApplicationStates.READY:
			SerialPortHandler.getInstance().updateSystemReady();
			
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Thread.currentThread().setName("Tamir sagi");
					try{
						System.out.println("!@#!@#!@#@!# Thread is here");
						Thread.sleep(2000);
						System.out.println("$$$$$$ BACK TO BUISENSEESSS");
					}catch(Exception e){
						
					}
					setCurrentVehicleMode(VehicleParams.VEHICLE_MODE_AUTONOMOUS);
					//DrivingManager.getInstance().setCurrentMode(VehicleAutonomousMode.VEHICLE_AUTONOMOUS_FREE_DRIVING);
					
				}
			}).start();
			break;
		}

	}
	
	// ///////////////////////////////////// SERVER EVENTS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	@Override
	public void onUpdateServerStatusChanged(int code) {
		PegasusLogger.getInstance().d(TAG,"onUpdateServerStatusChanged", "State changed To"
				+ BluetoothServer.getInstance().getServerStatusName());
		switch (code) {
		case BluetoothServerStatus.DISCONNECTED:
			// TODO - > decide what happened when server is disconnected
			mIsServerReady = false;
			setState(ApplicationStates.WAITING_FOR_SERVER);
			break;
		case BluetoothServerStatus.CONNECTING:
			// TODO - > decide what happened when server is connecting
			break;
		case BluetoothServerStatus.CONNECTED:
			mIsServerReady = true;
			setState(ApplicationStates.SERVER_READY);
		}

	}

	@Override
	public void onMessageReceivedFromClient(String msg) {
		PegasusLogger.getInstance().d(TAG, "onMessageReceivedFromClient", msg);
		try {
			JSONObject receivedMsg = new JSONObject(msg);
			int messageType = receivedMsg.optInt(MessageVaribles.KEY_MESSAGE_TYPE);
			switch (messageType) {
			case MessageVaribles.MESSAGE_TYPE_ACTION:
				handleVehicleAction(receivedMsg);
				break;
			case MessageVaribles.MESSAGE_TYPE_SETTINGS:
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			PegasusLogger.getInstance().e(TAG,"OnMessageReceived", e.getMessage());
		}

	}

	/**
	 * function handles message type of Action
	 * 
	 * @param msg
	 */
	private void handleVehicleAction(JSONObject receivedMsg) {
		try {
			PegasusLogger.getInstance().d(TAG, "handleVehicleAction", receivedMsg.toString());
			int vehicleActionType = receivedMsg.optInt(MessageVaribles.KEY_VEHICLE_ACTION_TYPE);
			switch (vehicleActionType) {
			case VehicleParams.VEHICLE_ACTION_CHANGE_SPEED:
				int digitalSpeed = receivedMsg.optInt(MessageVaribles.KEY_DIGITAL_SPEED);
				PegasusVehicle.getInstance().changeSpeed(digitalSpeed);
				break;
			case VehicleParams.VEHICLE_ACTION_CHANGE_STEERING:
				String steeringDirection;
				double rotationAngle = 0;
				steeringDirection = receivedMsg.getString(MessageVaribles.KEY_STEERING_DIRECTION);
				rotationAngle = receivedMsg.getInt(MessageVaribles.KEY_ROTATION_ANGLE); // we send the angle as an int but might be double
				if (steeringDirection.equals(MessageVaribles.VALUE_STEERING_RIGHT))
					PegasusVehicle.getInstance().turnRight(rotationAngle);
				else if (steeringDirection.equals(MessageVaribles.VALUE_STEERING_LEFT))
					PegasusVehicle.getInstance().turnLeft(rotationAngle);
				break;
			case VehicleParams.VEHICLE_ACTION_CHANGE_DIRECTION:
				String drivingDirection = receivedMsg.getString(MessageVaribles.KEY_DRIVING_DIRECTION);
				switch (VehicleParams.DrivingDirection
						.valueOf(drivingDirection)) {
				case FORWARD:
					PegasusVehicle.getInstance().driveForward();
					break;
				case BACKWARD:
					PegasusVehicle.getInstance().driveBackward();
					break;
				}
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			PegasusLogger.getInstance().e(TAG, "handleAction" ,e.getMessage());
		}
	}

	// ////////////////////////////////////// VEHICLE EVENTS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	@Override
	public void onVehicleStateChanged(boolean aIsVehicleReady){
		if(aIsVehicleReady){
			PegasusLogger.getInstance().d(TAG,"onVehicleStateChanged", "changed state to :"	+ ApplicationStates.INITIALIZE_SERIAL_PORT);
			PegasusVehicle.getInstance().registerVehicleActionsListener(this);
			setState(ApplicationStates.INITIALIZE_SERIAL_PORT);
		}
	}
	
	// ////////////////////////////////////// SERIAL PORT EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	@Override
	public void onSerialPortStateChanged(boolean aIsReady) {
		if(aIsReady){
			PegasusLogger.getInstance().d(TAG,"onSerialPortReady", "changed state to :"	+ ApplicationStates.WAITING_FOR_HARDWARE);
			mIsSerialPortReady = true;
			PegasusVehicle.getInstance().registerAllSensorToDataProvider();
			setState(ApplicationStates.WAITING_FOR_HARDWARE);
		}else{
			//TODO -> Failure
		}
	}

	/**
	 * Update hardware status by status code
	 * 
	 * @param statusCode
	 */
	public void updateHardwareStatus(int statusCode) {
		PegasusLogger.getInstance().d(TAG, "updateHardwareStatus", "Status code:" + statusCode);
		switch (statusCode) {
		case MessageVaribles.MESSAGE_TYPE_INFO_HARDWARE_READY:
			if(!mIsHardwareReady){
				mIsHardwareReady = true;
				setState(ApplicationStates.HARDWARE_READY);
			}else{
				if (!mIsServerReady && !BluetoothServer.getInstance().isServerOnline()) {
					mIsHardwareReady = true;
					setState(ApplicationStates.WAITING_FOR_SERVER);

				} else{
					setState(ApplicationStates.READY);
				}
			}
			break;
		//TODO - add failure state
		default:
			break;
		}
	}

	@Override
	public void onSerialPortError(String msg) {
		PegasusLogger.getInstance().e(TAG, "onSerialPortError", msg);

	}
	
	/**
	 * set current control mode 
	 * @param aControlType either manual or autonomous
	 */
	public void setCurrentVehicleMode(int aVehicleMode){
		int lastControlType = PegasusVehicle.getInstance().getVehicleControlType();
		if(lastControlType != aVehicleMode){
			PegasusLogger.getInstance().i(TAG,"Changing state from " +  lastControlType + " to " + aVehicleMode);
			PegasusVehicle.getInstance().setControlType(aVehicleMode);
			switch(aVehicleMode){
			case VehicleParams.VEHICLE_MODE_AUTONOMOUS:
				if(!DrivingManager.getInstance().isAlive()){
					DrivingManager.getInstance().registerListener(PegasusVehicle.getInstance());
					DrivingManager.getInstance().startThread();
					while(!DrivingManager.getInstance().isAlive());
				}else if(DrivingManager.getInstance().isThreadSuspended()){
					DrivingManager.getInstance().resumeThread();
				}
				break;
			case VehicleParams.VEHICLE_MODE_MANUAL:
				if(DrivingManager.getInstance().isAlive()){
					DrivingManager.getInstance().suspendThread();
				}
				break;
			}
		}
	}
	
	
	
	//////////////////////////////////////// AUTONOMOUS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	/**
	 * Method initiate driving manager thread
	 */
	public void freeDrive(){
		PegasusLogger.getInstance().i(TAG, "freeDrive", "free driving...");
		DrivingManager.getInstance().freeDrive();
	}
	
	/**
	 * handle parking action
	 * @param parkingType
	 */
	public void findParkingSpot(int aParkingType) {
		if(PegausVehicleProperties.getInstance().isDataLoaded()){
			PegasusLogger.getInstance().i(TAG, "findParkingSpot", "started looking for parking");
			DrivingManager.getInstance().findParkingSpot(aParkingType);
		}else{
			//TODO - send callback parking cannot be performed
		}
	}
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
