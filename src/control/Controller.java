package control;

import java.util.HashMap;

import logs.logger.PegasusLogger;

import org.json.JSONException;
import org.json.JSONObject;

import Util.MessagesMethods;

import communication.bluetooth.Constants.BluetoothServerStatus;
import communication.bluetooth.Server.BluetoothServer;
import communication.messages.MessageVaribles;
import communication.serialPorts.SerialPortHandler;

import vehicle.Interfaces.onSensorDataRecieved;
import vehicle.Pegasus.PegasusVehicle;
import vehicle.Sensor.AbstractSensor;
import vehicle.algorithms.ParkingFinder;
import vehicle.common.constants.VehicleParams;

import control.Constants.ApplicationStates;
import control.Interfaces.ISerialPortListener;
import control.Interfaces.IServerListener;
import control.Interfaces.IVehicleActionsListener;
import control.Interfaces.onParkingState;

public class Controller implements IServerListener, ISerialPortListener,
		IVehicleActionsListener, onParkingState {

	public static final String TAG = Controller.class.getSimpleName();
	private static Controller mInstance;
	private boolean mIsServerReady;
	private boolean mIsSerialPortReady;
	private boolean mIsHardwareReady;
	
	private HashMap<Integer,onSensorDataRecieved> mSensorLisenters;
	private int mApplicationState = ApplicationStates.BOOTING;

	
	public static Controller getInstance(){
		if(mInstance == null){
			mInstance = new Controller();
		}
		return mInstance;
	}
	private  Controller() {
		mSensorLisenters = new HashMap<Integer, onSensorDataRecieved>();
		
	}

	/**
	 * initialize system when boot is completed
	 */
	public void bootCompleted() {
		PegasusLogger.getInstance().d(TAG, "bootCompleted", "System Booted up");
		PegasusVehicle.getInstance().registerVehicleActionsListener(this);
		setState(ApplicationStates.INITIALIZE_SERIAL_PORT);
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
		case ApplicationStates.INITIALIZE_SERIAL_PORT:
			SerialPortHandler.getInstance().registerMessagesListener(this);
			SerialPortHandler.getInstance().startThread();
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
			ParkingFinder.getInstance().registerParkingEventsListner(this);
		}

	}
	
	/**
	 * register sensors to get incoming data
	 * @param sensorID
	 * @param listener - sensor who listens for a new input
	 */
	public void registerSensor(int sensorID, onSensorDataRecieved listener){
		if(sensorID > 0 && listener != null){
			mSensorLisenters.put(sensorID, listener);
		}
	}

	// ///////////////////////////////////// SERVER EVENTS & Relevant Methods// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

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

	// //////////////////////////////////////VEHICLE EVENTS & Relevant Methods
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	@Override
	public void changeSpeed(int digitalSpeed) {
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeSpeed(digitalSpeed);
	}

	@Override
	public void turnRight(double rotationAngle) {
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeSteerMotor(
					MessageVaribles.VALUE_STEERING_RIGHT, rotationAngle);
	}

	@Override
	public void turnLeft(double rotationAngle) {
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeSteerMotor(
					MessageVaribles.VALUE_STEERING_LEFT, rotationAngle);
	}

	@Override
	public void driveForward() {
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeDrivingDirection(
					MessageVaribles.VALUE_DRIVING_FORWARD);
	}

	@Override
	public void driveBackward() {
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeDrivingDirection(
					MessageVaribles.VALUE_DRIVING_REVERSE);
	}

	@Override
	public void stop() {
		if (SerialPortHandler.getInstance().isBoundToSerialPort())
			SerialPortHandler.getInstance().changeSpeed(0);
	}
	
	/**
	 * handle parking action
	 * @param parkingType
	 */
	@Override
	public void findParkingSpot(int parkingType) {
		ParkingFinder.getInstance().findParking(parkingType);
	}

	// ////////////////////////////////////// SERIAL PORT EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	@Override
	public void onSerialPortReady() {
		PegasusLogger.getInstance().d(TAG,"onSerialPortReady", "changed state to :"	+ ApplicationStates.WAITING_FOR_HARDWARE);
		mIsSerialPortReady = true;
		setState(ApplicationStates.WAITING_FOR_HARDWARE);

	}

	@Override
	public void onHardwareReady() {
		PegasusLogger.getInstance().d(TAG, "onHardwareReady", "current state:" + mApplicationState);
		if (!mIsServerReady && !BluetoothServer.getInstance().isServerOnline()) {
			mIsHardwareReady = true;
			setState(ApplicationStates.WAITING_FOR_SERVER);

		} else {
			setState(ApplicationStates.READY);
		}

	}

	@Override
	public void onMessageReceivedFromHardwareUnit(String msg) {
		try {
			JSONObject received = MessagesMethods
					.convertSerialPortMessageToMap(msg);
			if (received.length() > 0) {
				int messageType = received
						.getInt(MessageVaribles.KEY_MESSAGE_TYPE);
				switch (MessageVaribles.MessageType.getMessageType(messageType)) {
				case ACTION:
					break;
				case ERROR:
					break;
				case INFO:
					handleInfoMessageFromHardwareUnit(received);
					break;
				case WARNING:
					break;
				default:
					break;
				}
			}

		}catch (Exception e) {
			PegasusLogger.getInstance().e(TAG, "onMessageReceivedFromHardwareUnit", "msg : " + msg 
					+ " exception:" + e.getMessage());
		}
	}

	/**
	 * Method handles info message from Serial Port
	 * 
	 * @param receivedMsg
	 */
	private void handleInfoMessageFromHardwareUnit(JSONObject receivedMsg) {
		PegasusLogger.getInstance().d(TAG, "handleInfoMessageFromHardwareUnit", receivedMsg.toString());
		try {
			String info_type = (String) receivedMsg
					.get(MessageVaribles.KEY_INFO_TYPE);
			switch (MessageVaribles.InfoType.valueOf(info_type)) {
			case STATUS:
				int status_code = receivedMsg
						.getInt(MessageVaribles.KEY_STATUS);
				updateHardwareStatus(status_code);
				break;
			case SENSOR_DATA:
				int sensorID = receivedMsg.getInt(MessageVaribles.KEY_SENSOR_ID);
				double sensorData = receivedMsg.getDouble(MessageVaribles.KEY_SENSOR_DATA);
				notifySensorForIncomingData(sensorID,sensorData);
			default:
				break;

			}

		} catch (JSONException e) {
			PegasusLogger.getInstance().e(TAG, "handleInfoMessageFromSerialPort", e.getMessage());
		}

	}

	/**
	 * Update hardware status by status code
	 * 
	 * @param statusCode
	 */
	private void updateHardwareStatus(int statusCode) {
		PegasusLogger.getInstance().d(TAG, "updateHardwareStatus", "Status code:" + statusCode);
		switch (MessageVaribles.StatusCode.get(statusCode)) {
		case INFO_HARDWARE_STATUS_READY:
			if(!mIsHardwareReady){
				mIsHardwareReady = true;
				setState(ApplicationStates.HARDWARE_READY);
			}
			break;
		//TODO - add failure state
		default:
			break;
		}
	}

	@Override
	public void onSerialStatusChanged(boolean status) {
		mIsSerialPortReady = status;
	}

	@Override
	public void onSerialPortError(String msg) {
		PegasusLogger.getInstance().e(TAG, "onSerialPortError", msg);

	}
	
	/**
	 * notify relevant sensor for new data
	 * @param sensorID
	 * @param value
	 */
	private void notifySensorForIncomingData(int sensorID,double value){
		if(sensorID > 0 && value >= 0){
			mSensorLisenters.get(sensorID).onRecievedSensorData(value);
		}
	}
	
	
	//////////////////////////////////////// Parking finder Area events \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	
	
	
	@Override
	public void onParkingFound() {
		
	}
	@Override
	public void onParkingNoFound() {
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
