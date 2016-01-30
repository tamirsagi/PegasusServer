package Control;

import org.json.JSONException;
import org.json.JSONObject;

import serialPorts.SerialPortHandler;
import Bluetooth.BluetoothServer;
import Control.Interfaces.IServerListener;
import Control.Interfaces.ISerialPortListener;
import Control.Interfaces.IVehicleActionsListener;
import Helper.GeneralMethods;
import Helper.GeneralParams;
import Helper.GeneralParams.Action_Type;
import Helper.GeneralParams.Info_Type;
import Helper.GeneralParams.Vehicle_Actions;
import Helper.GeneralParams.MessageType;
import PegasusVehicle.PegasusVehicle;

public class Controller implements IServerListener,ISerialPortListener, IVehicleActionsListener{
	
	public static final String TAG = " Pegasus Controller";
	
	private BluetoothServer mBluetoothServer;
	private SerialPortHandler mSerialPortHandler;
	private PegasusVehicle mPegasusVehicle;
	
	private boolean mIsServerReady;
	private boolean mIsSerialPortReady;
	private boolean mIsHardwareReady;
	
	public enum SteeringDirection{
		FORDWARD, BACKWARD
	}
	
	
	public Controller(){
		
		mBluetoothServer = BluetoothServer.getInstance();
		mBluetoothServer.registerMessagesListener(TAG,this);
		mBluetoothServer.start();
		
		mSerialPortHandler = SerialPortHandler.getInstance();
		mSerialPortHandler.registerMessagesListener(TAG,this);
		mSerialPortHandler.startThread();
		
		
		mPegasusVehicle = PegasusVehicle.getInstance();
		mPegasusVehicle.registerVehicleActionsListener(this);
		mPegasusVehicle.setName(mBluetoothServer.getLocalDevice().getFriendlyName());
	}

	
///////////////////////////////////////SERVER EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	@Override
	public void onMessageReceivedFromClient(String msg) {
		System.out.println("TAG: " + msg);
		try {
			JSONObject receivedMsg = new JSONObject(msg);
			String messageType = (String)receivedMsg.get(GeneralParams.KEY_MESSAGE_TYPE);
			switch(MessageType.valueOf(messageType)){
			case ACTION:
				handleActionFromClient(receivedMsg);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			System.err.println("OnMessageReceived Error: " + e.getMessage());
		}
		
	}

	/**
	 * Method handle action type message from client
	 * @param receivedMsg - JSON object
	 */
	private void handleActionFromClient(JSONObject receivedMsg){
		try{
			String actionType = (String)receivedMsg.get(MessageType.ACTION.toString());
			switch(Action_Type.valueOf(actionType)){
			case SETTINGS:
				break;
			case VEHICLE_ACTION:
				handleVehicleAction(receivedMsg);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			System.err.println(TAG + " handleActionFromClient " + e.getMessage());
		}
	}
	
	
	/**
	 * function handles message type of Action
	 * @param msg
	 */
	private void handleVehicleAction(JSONObject msg){
		try {
			String vehicleActionType = (String)msg.get(Action_Type.VEHICLE_ACTION.toString());
			switch(Vehicle_Actions.valueOf(vehicleActionType)){
			case CAHNGE_SPEED:
				int digitalSpeed = (int)msg.get(GeneralParams.KEY_DIGITAL_SPEED);
				mPegasusVehicle.changeSpeed(digitalSpeed);
				break;
			case STEERING:
				char steeringDirection;
				double rotationAngle = 0;
				steeringDirection = (char)msg.get(GeneralParams.KEY_STEERING_DIRECTION);
				rotationAngle = (int)msg.get(GeneralParams.KEY_ROTATION_ANGLE);			//we send the angle as an int but might be double
				if(steeringDirection == GeneralParams.VALUE_STEERING_RIGHT)
					mPegasusVehicle.turnRight(rotationAngle);
				else
					mPegasusVehicle.turnLeft(rotationAngle);
				break;
			case CHANGE_DIRECTION:
				char drivingDirection = (char)msg.get(GeneralParams.KEY_DRIVING_DIRECTION);
				switch(drivingDirection){
				case GeneralParams.VALUE_DRIVING_FORWARD:
					mPegasusVehicle.driveForward();
					break;
				case GeneralParams.VALUE_DRIVING_BACKWARD:
					mPegasusVehicle.driveBackward();
					break;
				}
				break;
			default:
				break;
			
			
			
			}
		} catch (JSONException e) {
			System.err.println("handleAction Error : " + e.getMessage());
		}
		
		
	}

////////////////////////////////////////VEHICLE EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	
	@Override
	public void changeSpeed(int digitalSpeed) {
		mSerialPortHandler.changeSpeed(digitalSpeed);
	}


	@Override
	public void turnRight(double rotationAngle) {
		mSerialPortHandler.changeSteerMotor(GeneralParams.VALUE_STEERING_RIGHT, rotationAngle);
	}


	@Override
	public void turnLeft(double rotationAngle) {
		mSerialPortHandler.changeSteerMotor(GeneralParams.VALUE_STEERING_LEFT, rotationAngle);
	}


	@Override
	public void driveForward() {
		mSerialPortHandler.changeDrivingDirection(GeneralParams.VALUE_DRIVING_FORWARD);
	}


	@Override
	public void driveBackward() {
		mSerialPortHandler.changeDrivingDirection(GeneralParams.VALUE_DRIVING_BACKWARD);
	}


	@Override
	public void stop() {
		
		
	}

//////////////////////////////////////// SERIAL PORT EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	@Override
	public void onMessageReceivedFromHardwareUnit(String msg) {
		try{
		JSONObject received = GeneralMethods.convertSerialPortMessageToMap(msg);
		int messageType = received.getInt(GeneralParams.KEY_MESSAGE_TYPE);
		switch(MessageType.getMessageType(messageType)){
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
		
		
		}catch(Exception e){
			System.out.println( TAG + " onMessageReceivedFromHardwareUnit " + e.getMessage());
		}
		
	}
	
	/**
	 * Method handles info message from Serial Port
	 * @param receivedMsg
	 */
	private void handleInfoMessageFromHardwareUnit(JSONObject receivedMsg){
		
		try {
			String info_type = (String)receivedMsg.get(GeneralParams.KEY_INFO_TYPE);
			switch(Info_Type.valueOf(info_type)){
			case STATUS:
				int status_code = receivedMsg.getInt(GeneralParams.KEY_STATUS);
				updateHardwareStatus(status_code);
				break;
			default:
				break;
			
			}
			
		} catch (JSONException e) {
			System.out.println(TAG + " handleInfoMessageFromSerialPort " + e.getMessage());
		}
		
	}
	
	/**
	 * Update hardware status by status code
	 * @param statusCode
	 */
	private void updateHardwareStatus(int statusCode){
		switch(statusCode){
		case GeneralParams.INFO_HARDWARE_STATUS_READY:
			mIsHardwareReady = true;
			break;
		}
	}


	

}
