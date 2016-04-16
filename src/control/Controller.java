package control;

import logs.logger.PegasusLogger;

import org.json.JSONException;
import org.json.JSONObject;

import vehicle.common.VehicleData;
import vehicle.common.constants.VehicleParams;
import vehicle.common.constants.VehicleState;
import vehicle.managers.driving_manager.DrivingManager;
import vehicle.managers.finder.ParkingFinder;
import vehicle.pegasus.PegasusVehicle;
import vehicle.pegasus.PegausVehicleProperties;

import communication.bluetooth.Constants.BluetoothServerStatus;
import communication.bluetooth.Server.BluetoothServer;
import communication.serialPorts.SerialPortHandler;
import communication.serialPorts.messages.MessageVaribles;

import control.constants.ApplicationStates;
import control.interfaces.OnParkingEventsListener;
import control.interfaces.OnSerialPortEventsListener;
import control.interfaces.OnServerEventsListener;
import control.interfaces.OnVehicleEventsListener;

public class Controller implements OnServerEventsListener, OnParkingEventsListener, OnSerialPortEventsListener,OnVehicleEventsListener {

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
		case ApplicationStates.READY:
			SerialPortHandler.getInstance().updateSystemReady();
			//PegasusVehicle.getInstance().sendSensorConfiguration();
			if(!ParkingFinder.getInstance().isAlive()){
				ParkingFinder.getInstance().registerParkingEventsListner(this);
				ParkingFinder.getInstance().startThread();
				ParkingFinder.getInstance().suspendThread();
			}
			if(!DrivingManager.getInstance().isAlive())
				DrivingManager.getInstance().startThread();
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
			String messageType = (String) receivedMsg
					.get(MessageVaribles.KEY_MESSAGE_TYPE);
			switch (MessageVaribles.MessageType.valueOf(messageType)) {
			case ACTION:
				handleActionFromClient(receivedMsg);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			PegasusLogger.getInstance().e(TAG,"OnMessageReceived", e.getMessage());
		}

	}

	/**
	 * Method handle action type message from client
	 * 
	 * @param receivedMsg
	 *            - JSON object
	 */
	private void handleActionFromClient(JSONObject receivedMsg) {
		try {
			String actionType = (String) receivedMsg
					.get(MessageVaribles.MessageType.ACTION.toString());
			switch (MessageVaribles.Action_Type.valueOf(actionType)) {
			case SETTINGS:
				break;
			case VEHICLE_ACTION:
				handleVehicleAction(receivedMsg);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			PegasusLogger.getInstance().e(TAG, "handleActionFromClient", e.getMessage());
		}
	}

	/**
	 * function handles message type of Action
	 * 
	 * @param msg
	 */
	private void handleVehicleAction(JSONObject msg) {
		try {
			String vehicleActionType = (String) msg
					.get(MessageVaribles.Action_Type.VEHICLE_ACTION.toString());
			switch (VehicleParams.VehicleActions.valueOf(vehicleActionType)) {
			case CHANGE_SPEED:
				int digitalSpeed = (int) msg
						.get(MessageVaribles.KEY_DIGITAL_SPEED);
				PegasusVehicle.getInstance().changeSpeed(digitalSpeed);
				break;
			case STEERING:
				String steeringDirection;
				double rotationAngle = 0;
				steeringDirection = msg
						.getString(MessageVaribles.KEY_STEERING_DIRECTION);
				rotationAngle = msg.getInt(MessageVaribles.KEY_ROTATION_ANGLE); // we send the angle as an int but might be double
				if (steeringDirection.equals(MessageVaribles.VALUE_STEERING_RIGHT))
					PegasusVehicle.getInstance().turnRight(rotationAngle);
				else if (steeringDirection.equals(MessageVaribles.VALUE_STEERING_LEFT))
					PegasusVehicle.getInstance().turnLeft(rotationAngle);
				break;
			case CHANGE_DIRECTION:
				String drivingDirection = (String) msg
						.get(MessageVaribles.KEY_DRIVING_DIRECTION);
				switch (VehicleParams.DrivingDirection
						.valueOf(drivingDirection)) {
				case FORWARD:
					PegasusVehicle.getInstance().driveForward();
					break;
				case REVERSE:
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
		switch (MessageVaribles.StatusCode.get(statusCode)) {
		case INFO_HARDWARE_STATUS_READY:
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
	
	
	//////////////////////////////////////// Parking \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	/**
	 * handle parking action
	 * @param parkingType
	 */
	public void findParkingSpot(int parkingType) {
		if(PegausVehicleProperties.getInstance().isDataLoaded()){
			PegasusLogger.getInstance().i(TAG, "findParkingSpot", "started looking for parking");
			VehicleData vehicleData = PegasusVehicle.getInstance().getVehicleData();
			double mMinSpace = vehicleData.getLength() + 2 * vehicleData.getMinimumRequiredSpaceToPark();
			ParkingFinder.getInstance().findParking(parkingType, mMinSpace);
			ParkingFinder.getInstance().resumeThread();
			PegasusVehicle.getInstance().setCurrentState(VehicleState.VEHICLE_LOOKING_FOR_PARKING);
		}else{
			//TODO - send callback parking cannot be performed
		}
	}
	
	////////////////////////////////////////Parking Finder events \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	
	@Override
	public void onParkingFound() {
		
	}
	@Override
	public void onParkingNoFound() {
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
