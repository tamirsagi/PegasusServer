package Control;

import org.json.JSONException;
import org.json.JSONObject;

import serialPorts.SerialPortHandler;
import Bluetooth.BluetoothServer;
import Control.GeneralParams.ActionType;
import Control.GeneralParams.MessageType;
import PegasusVehicle.PegasusVehicle;

public class Controller implements OnMessagesListener, OnVehicleActions{
	
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

	@Override
	public void onMessageReceivedFromClient(String msg) {
		System.out.println("TAG: " + msg);
		try {
			JSONObject receivedMsg = new JSONObject(msg);
			String messageType = (String)receivedMsg.get(GeneralParams.KEY_MESSAGE_TYPE);
			switch(MessageType.valueOf(messageType)){
			case ACTION:
				handleAction(receivedMsg);
				break;
			default:
				break;
			
			}
		} catch (JSONException e) {
			System.err.println("OnMessageReceived Error: " + e.getMessage());
		}
		
	}

	@Override
	public void sendMessageToClient(String msg) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * function handles message type of Action
	 * @param msg
	 */
	private void handleAction(JSONObject msg){
		try {
			String actionType = (String)msg.get(MessageType.ACTION.toString());
			switch(ActionType.valueOf(actionType)){
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
			case SETTINGS:
				break;
			default:
				break;
			
			
			
			}
		} catch (JSONException e) {
			System.err.println("handleAction Error : " + e.getMessage());
		}
		
		
	}

	/*
	 *      Vehicle Events
	 *  */

	
	@Override
	public void changeSpeed(int digitalSpeed) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}


	
	@Override
	public void onMessageReceivedFromSerialPort(String msg) {
		
		
	}


	

}
