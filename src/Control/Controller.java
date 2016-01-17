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
	
	
	public enum SteeringDirection{
		FORDWARD, BACKWARD
	}
	
	
	public Controller(){
		
		mBluetoothServer = BluetoothServer.getInstance();
		
		mSerialPortHandler = SerialPortHandler.getInstance();
		
		mBluetoothServer.registerMessagesListener(this);
		mBluetoothServer.start();
		
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
			case DRIVE:
				int digitalSpeed = (int)msg.get(GeneralParams.KEY_DIGITAL_SPEED);
				char steeringDirection = GeneralParams.KEY_STERRING_NONE;
				double rotationAngle = 0;
				if(msg.has(GeneralParams.KEY_STEERING_DIRECTION)){
					steeringDirection = (char)msg.get(GeneralParams.KEY_STEERING_DIRECTION);
					rotationAngle = (int)msg.get(GeneralParams.KEY_ROTATION_ANGLE);		//we send the angle as an int but might be double
				}
				mPegasusVehicle.drive(digitalSpeed,steeringDirection,rotationAngle);
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
	public void drive(int digitalSpeed) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void turnRight(double rotationAngle) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void turnLeft(double rotationAngle) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void driveForward() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void driveBackward() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
