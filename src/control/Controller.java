package control;

import org.json.JSONException;
import org.json.JSONObject;

import communication.bluetooth.BluetoothServer;
import communication.messages.GeneralMethods;
import communication.messages.MessageVaribles;
import communication.serialPorts.SerialPortHandler;

import pegasusVehicle.AbstractVehicle;
import pegasusVehicle.PegasusVehicle;
import pegasusVehicle.params.VehicleParams;

import control.Interfaces.ISerialPortListener;
import control.Interfaces.IServerListener;
import control.Interfaces.IVehicleActionsListener;


public class Controller implements IServerListener,ISerialPortListener, IVehicleActionsListener{
	
	public static final String TAG = Controller.class.getSimpleName();
	
	private BluetoothServer mBluetoothServer;
	private SerialPortHandler mSerialPortHandler;
	private AbstractVehicle mPegasusVehicle;
	
	private boolean mIsServerReady;
	private boolean mIsSerialPortReady;
	private boolean mIsHardwareReady;
	
	
	
	
	public Controller(){
		
		startup();
		
		mBluetoothServer = BluetoothServer.getInstance();
		mBluetoothServer.registerMessagesListener(TAG,this);
		mBluetoothServer.start();
		
		mSerialPortHandler = SerialPortHandler.getInstance();
		mSerialPortHandler.registerMessagesListener(TAG,this);
		mSerialPortHandler.startThread();
		
		
		mPegasusVehicle = PegasusVehicle.getInstance();
		mPegasusVehicle.registerVehicleActionsListener(this);
		
	}
	
	
	
	
	private void startup(){
		String[] linkArduinoToPort = new String[] {"sh", "-c","sudo ln -s /dev/ttyACM0 /dev/ttyS0"}; 
		String[] enableBluetooth = new String[] {"sh", "-c","sudo service bluetooth start"};
		
		try{
			Process linkArduinoToPortProccess = Runtime.getRuntime().exec(linkArduinoToPort);
			linkArduinoToPortProccess.waitFor();
			Process enableBluetoothProccess = Runtime.getRuntime().exec(enableBluetooth);
			enableBluetoothProccess.waitFor();
		}
		catch(Exception e){
			System.out.println(TAG +" " + e.getMessage());
		}
		
	}
	
	

	
///////////////////////////////////////SERVER EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	@Override
	public void onServerStatusChanged(boolean isReady) {
		mIsServerReady = isReady;	
		mPegasusVehicle.setName(mBluetoothServer.getLocalDevice().getFriendlyName());
	}
	
	@Override
	public void onMessageReceivedFromClient(String msg) {
//		System.out.println(TAG + " " + msg);
		try {
			JSONObject receivedMsg = new JSONObject(msg);
			String messageType = (String)receivedMsg.get(MessageVaribles.KEY_MESSAGE_TYPE);
			switch(MessageVaribles.MessageType.valueOf(messageType)){
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
			String actionType = (String)receivedMsg.get(MessageVaribles.MessageType.ACTION.toString());
			switch(MessageVaribles.Action_Type.valueOf(actionType)){
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
			String vehicleActionType = (String)msg.get(MessageVaribles.Action_Type.VEHICLE_ACTION.toString());
			switch(VehicleParams.VehicleActions.valueOf(vehicleActionType)){
			case CHANGE_SPEED:
				int digitalSpeed = (int)msg.get(MessageVaribles.KEY_DIGITAL_SPEED);
				mPegasusVehicle.changeSpeed(digitalSpeed);
				break;
			case STEERING:
				String steeringDirection;
				double rotationAngle = 0;
				steeringDirection = msg.getString(MessageVaribles.KEY_STEERING_DIRECTION);
				rotationAngle = msg.getInt(MessageVaribles.KEY_ROTATION_ANGLE);			//we send the angle as an int but might be double
				if(steeringDirection.equals(MessageVaribles.VALUE_STEERING_RIGHT))
					mPegasusVehicle.turnRight(rotationAngle);
				else if(steeringDirection.equals(MessageVaribles.VALUE_STEERING_LEFT))
					mPegasusVehicle.turnLeft(rotationAngle);
				break;
			case CHANGE_DIRECTION:
				String drivingDirection = (String)msg.get(MessageVaribles.KEY_DRIVING_DIRECTION);
				switch(VehicleParams.DrivingDirection.valueOf(drivingDirection)){
				case FORWARD:
					mPegasusVehicle.driveForward();
					break;
				case REVERSE:
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
	
	@Override
	public void onServerError(String msg){
		System.out.println("ERROR From SERVER:" + msg);
		
	}

////////////////////////////////////////VEHICLE EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	
	@Override
	public void changeSpeed(int digitalSpeed) {
		if(mSerialPortHandler.isBoundToSerialPort())
			mSerialPortHandler.changeSpeed(digitalSpeed);
	}


	@Override
	public void turnRight(double rotationAngle) {
		if(mSerialPortHandler.isBoundToSerialPort())
			mSerialPortHandler.changeSteerMotor(MessageVaribles.VALUE_STEERING_RIGHT, rotationAngle);
	}


	@Override
	public void turnLeft(double rotationAngle) {
		if(mSerialPortHandler.isBoundToSerialPort())
			mSerialPortHandler.changeSteerMotor(MessageVaribles.VALUE_STEERING_LEFT, rotationAngle);
	}


	@Override
	public void driveForward() {
		if(mSerialPortHandler.isBoundToSerialPort())
			mSerialPortHandler.changeDrivingDirection(MessageVaribles.VALUE_DRIVING_FORWARD);
	}


	@Override
	public void driveBackward() {
		if(mSerialPortHandler.isBoundToSerialPort())
			mSerialPortHandler.changeDrivingDirection(MessageVaribles.VALUE_DRIVING_REVERSE);
	}


	@Override
	public void stop() {
		
		
	}

//////////////////////////////////////// SERIAL PORT EVENTS & Relevant Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	@Override
	public void onMessageReceivedFromHardwareUnit(String msg) {
		try{
		JSONObject received = GeneralMethods.convertSerialPortMessageToMap(msg);
		if(received.length() > 0){
			int messageType = received.getInt(MessageVaribles.KEY_MESSAGE_TYPE);
			switch(MessageVaribles.MessageType.getMessageType(messageType)){
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
			String info_type = (String)receivedMsg.get(MessageVaribles.KEY_INFO_TYPE);
			switch(MessageVaribles.InfoType.valueOf(info_type)){
			case STATUS:
				int status_code = receivedMsg.getInt(MessageVaribles.KEY_STATUS);
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
		switch(MessageVaribles.StatusCode.get(statusCode)){
		case INFO_HARDWARE_STATUS_READY:
			mIsHardwareReady = true;
			break;
		case INFO_SERVER_STATUS_READY:
			break;
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
		System.out.println("msg from Serial Port: " + msg);
		
	}




	


	

}
