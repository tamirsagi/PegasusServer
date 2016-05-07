package communication.serialPorts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;


import communication.messages.MessageVaribles;


import logs.logger.PegasusLogger;


import util.MessagesMethods;
import vehicle.common.constants.VehicleParams;
import vehicle.interfaces.onSensorDataRecieved;

import control.Controller;
import control.interfaces.OnSerialPortEventsListener;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


/**
 * This Class Supports 2 way communication via USB port  
 * @author Tamir Sagi
 *
 */
public class SerialPortHandler extends Thread implements SerialPortEventListener {
	private static final String TAG = "Serial Port Handler";
	private static final String PortName = "/dev/ttyACM0"; 		//the mounted ttyPort for Arduino
	private static final int PORT_BAUD = 115200;
	
	private static SerialPortHandler mSerialPortHandler;
	private boolean mIsBoundedToUsbPort;
	private int mTimeout;
	private SerialPort mSerialPort;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private Queue<String> mMessagesToArduino;
	private boolean mIsWaiting;
	private HashMap<Integer,onSensorDataRecieved> mSensors;
	
	private OnSerialPortEventsListener mListener;		//keeps listeners
	
	public static SerialPortHandler getInstance(){
		if(mSerialPortHandler == null){
			mSerialPortHandler = new SerialPortHandler();
		}
		return mSerialPortHandler;
	}
	
	private SerialPortHandler(){
		setName(TAG);
		mSensors = new HashMap<Integer, onSensorDataRecieved>();
		mMessagesToArduino = new LinkedList<String>();
	}
	
	/**
	 * Register Listener
	 * @param name
	 * @param listener
	 */
	public void registerStatesListener(OnSerialPortEventsListener listener){
		mListener = listener;
	}
	
	/**
	 * Unregister Listener
	 * @param name
	 */
	public void unRegisterStatesListener(){
		mListener = null;
	}
	
	
	public void registerSensor(int sensorID, onSensorDataRecieved listener){
		mSensors.put(sensorID, listener);
	}
	
	/**
	 * Connect to Serial Port, Define streams
	 */
	private void connect(){
		try{
			//add the port manually, because version of RXTX gaps
			System.setProperty("gnu.io.rxtx.SerialPorts", PortName);
//			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PortName);
			CommPortIdentifier portIdentifier = getPortIdentifier(PortName);
			if(portIdentifier == null){
				fireSerialPortErrors("connect() : Could not find port " + PortName);
				mIsBoundedToUsbPort = false;
				return;
			}
			else if(portIdentifier.isCurrentlyOwned()){
				fireSerialPortErrors(" connect() : Error : Port: " + PortName +  " is currently in use");
				mIsBoundedToUsbPort = false;
				return;
			}
			
			else{
				CommPort commPort = portIdentifier.open(getName(),mTimeout);
				if(commPort instanceof SerialPort){
					mIsBoundedToUsbPort = true;
					mSerialPort = (SerialPort)commPort;
					mSerialPort.setSerialPortParams(PORT_BAUD, SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
					
					
					mInputStream = mSerialPort.getInputStream();
					mOutputStream = mSerialPort.getOutputStream();

					mSerialPort.addEventListener(this);
					mSerialPort.notifyOnDataAvailable(true);
					mSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE); //disable flow control
					fireStatusFromSerialPort(true);
				}
			}
		}catch(Exception eSerialPort){
			fireSerialPortErrors("connect() Exception " + eSerialPort.getMessage());
		}
	}
	
	
	@Override
	public void run() {
		if(!mIsBoundedToUsbPort){
			connect();
		}
		PegasusLogger.getInstance().i(getName(),"run", "Serial port thread is running...");
		while(mIsBoundedToUsbPort){
			synchronized (this) {
				try{
					while(mIsWaiting){
						wait();
					}
				}catch(Exception e){
					
				}
			}
		}
		disconnect();
	}
	
	private synchronized void readFromSerial() throws IOException{
		if(mInputStream.available() > 0){
			resumeThread();
			StringBuilder message = new StringBuilder();
			int available = 0;
			byte[] received = null;
			try {
				while ((available = mInputStream.available()) > 0) {
					received = new byte[available + 1];
					mInputStream.read(received);
					message.append(new String(received).trim());
					pullMessages(message);
				}
				if(message.length() > 0 ){
					pullMessages(message);
				}
				suspendThread();
				}catch (Exception e) {
					fireSerialPortErrors("Serial Event Listener Exception:" + e.getMessage());
				}
		}
	}
	
	/**
	 * Add message to queue,
	 * @param msg - message which is sent to Hardwareunit(Arduino)
	 */
	public synchronized void addMessageToQueue(String msg){
		if(msg != null && !msg.isEmpty()){
			mMessagesToArduino.add(msg);
		}
		try {
			resumeThread();
			writeToSerial();
		} catch (InterruptedException | IOException e) {
			PegasusLogger.getInstance().e(getName(), "addMessageToQueue", e.getMessage());
		}
	}
	
	private void writeToSerial() throws InterruptedException, IOException{
		while(!mMessagesToArduino.isEmpty()){
				String msg = mMessagesToArduino.poll();
				PegasusLogger.getInstance().d(Thread.currentThread().getName(), "writeMessage", "before sending to arduino: " + msg);
				mOutputStream.write(msg.getBytes());
				mOutputStream.flush();
				sleep(20);
		}
		suspendThread();
	}
	
	/**
	 * received message is being notified here
	 */
	public void serialEvent(SerialPortEvent event) {
		
		switch (event.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				readFromSerial();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				break;
		}
	}
	
	/**
	 * pull all messages from current buffer
	 * if could not find either start or end symbol method will end.
	 * @param buffer -Message that received from stream
	 */
	private synchronized void pullMessages(StringBuilder buffer){
		int first = buffer.indexOf(MessageVaribles.START_MESSAGE);
		int last = buffer.indexOf(MessageVaribles.END_MESSAGE);
		String msgToSend  = "";
		while (first >= 0 && last >= 0) {
			if(first > last){ //in case half message received 
				buffer.delete(0, last + 1);
			}else{
				msgToSend = buffer.substring(first + 1, last);
				PegasusLogger.getInstance().d(getName(), "pullMessages", msgToSend);
				handleIncomingMessageFromHardwareUnit(msgToSend);
				buffer.delete(first, last + 1);
				
			}
			first = buffer.indexOf(MessageVaribles.START_MESSAGE);
			last = buffer.indexOf(MessageVaribles.END_MESSAGE);
		}
	}
	
	/**
	 * handle message from Hardware
	 * parse the message into JSON object and fetch the relevant data
	 * @param msg
	 */
	private void handleIncomingMessageFromHardwareUnit(String msg) {
		try {
			JSONObject received = MessagesMethods
					.convertSerialPortMessageToMap(msg);
			if (received.length() > 0) {
				int messageType = received
						.getInt(MessageVaribles.KEY_MESSAGE_TYPE);
				switch (messageType) {
				case MessageVaribles.MESSAGE_TYPE_ACTION:
					break;
				case MessageVaribles.MESSAGE_TYPE_ERROR:
					break;
				case MessageVaribles.MESSAGE_TYPE_INFO:
					handleInfoMessageFromHardwareUnit(received);
					break;
				default:
					break;
				}
			}

		}catch (Exception e) {
			PegasusLogger.getInstance().e(getName(), "onMessageReceivedFromHardwareUnit", "msg : " + msg 
					+ " exception:" + e.getMessage());
		}
	}
	
	
	/**
	 * Method handles info message from Serial Port
	 * 
	 * @param receivedMsg
	 */
	private void handleInfoMessageFromHardwareUnit(JSONObject receivedMsg) {
		PegasusLogger.getInstance().d(getName(), "handleInfoMessageFromHardwareUnit", receivedMsg.toString());
		try {
			String info_type = (String) receivedMsg
					.get(MessageVaribles.KEY_INFO_TYPE);
			switch (MessageVaribles.InfoType.valueOf(info_type)) {
			case STATUS:
				int status_code = receivedMsg
						.getInt(MessageVaribles.KEY_STATUS);
				mListener.updateHardwareStatus(status_code);
				break;
			case SENSOR_DATA:
				String sensorIdKey = "";
				for(int i = 1; i <= Controller.getInstance().getNumberOfUltraSonicSensor(); i++){
					sensorIdKey = MessageVaribles.KEY_SENSOR_ID + i;
					if(receivedMsg.has(sensorIdKey)){
						double sensorData = receivedMsg.getDouble(sensorIdKey);
						notifySensorForIncomingData(i,sensorData);
					}
					int tachometerId = Controller.getInstance().getTachometerId();
					sensorIdKey = MessageVaribles.KEY_SENSOR_ID + tachometerId;
					if(receivedMsg.has(sensorIdKey)){
						double sensorData = receivedMsg.getDouble(sensorIdKey);
						notifySensorForIncomingData(tachometerId,sensorData);
					}
				}
			default:
				break;

			}

		} catch (JSONException e) {
			PegasusLogger.getInstance().e(getName(), "handleInfoMessageFromSerialPort", e.getMessage());
		}
	}
	
	/**
	 * notify relevant sensor for new data
	 * @param sensorID
	 * @param value
	 */
	private void notifySensorForIncomingData(int sensorID,double value){
		if(sensorID > 0 && value >= 0){
			mSensors.get(sensorID).onRecievedSensorData(value);
		}
	}
		
			
	
	/**
	 * close connection to serial port
	 */
	public void disconnect(){
		try {
			if(mInputStream != null)
				mInputStream.close();
			if(mOutputStream != null)
				mOutputStream.close();
			if(mSerialPort != null){
				mSerialPort.removeEventListener();
				mSerialPort.close();	//close serial port
				stopThread();
			}
			PegasusLogger.getInstance().i(getName(),"run", "Serial port thread is stopped...");
		} catch (IOException e) {
			fireSerialPortErrors("disconnect() Exception " + e.getMessage());
		}
	}
	
	/**
	 * Method starts the thread
	 */
	public void startThread(){
		PegasusLogger.getInstance().d(getName(), "startThread", "Starting Serial Port Thread");
		start();
	}
	
	/**
	 * method stop Serial Thread
	 */
	public void stopThread(){
		PegasusLogger.getInstance().d(getName(), "stopThread", "Stopping Serial Port Thread");
		mIsBoundedToUsbPort = false;
	}
	
	public void suspendThread(){
		PegasusLogger.getInstance().i(getName(), "suspedning...");
		mIsWaiting = true;
	}
	
	public synchronized void resumeThread(){
		PegasusLogger.getInstance().i(getName(), "resuming...");
		mIsWaiting = false;
		notify();
	}
	
	/**
	 * update Hardware unit that system is ready
	 */
	public void updateSystemReady(){
		String msgToArduino = MessageVaribles.START_MESSAGE 
				+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MESSAGE_TYPE_INFO + MessageVaribles.MESSAGE_SAPERATOR
				+ MessageVaribles.KEY_INFO_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.InfoType.STATUS.toString() + MessageVaribles.MESSAGE_SAPERATOR
				+ MessageVaribles.KEY_STATUS + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MESSAGE_TYPE_INFO_SERVER_READY 
				+ MessageVaribles.END_MESSAGE;
		addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(getName(), "updateSystemReady", "Update System Ready:" + msgToArduino);
	}

	
	/**
	 * @param PortType
	 * @return port type
	 */
	public String getPortName(int PortType){
		switch(PortType){
		case CommPortIdentifier.PORT_I2C:
			return "IC2";
		case CommPortIdentifier.PORT_PARALLEL:
			return "PARALLEL";
		case CommPortIdentifier.PORT_RAW:
			return "RAW";
		case CommPortIdentifier.PORT_RS485:
			return "RS485";
		case CommPortIdentifier.PORT_SERIAL:
			return "SERIAL";
		default:
				return "unknown";
		}
	}
	
	/**
	 * prints out all available ports
	 */
	public static void printAllAvailablePorts(){
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier port;
		while(ports.hasMoreElements()){
			port = (CommPortIdentifier)ports.nextElement();
			PegasusLogger.getInstance().e(TAG, "printAllAvailablePorts", "port name : " + port.getName() + " port type " + port.getPortType());
		}
	}
	
	/**
	 * 
	 * @param portName
	 * @return wanted port otherwise null
	 */
	private CommPortIdentifier getPortIdentifier(String portName){
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier port;
		while(ports.hasMoreElements()){
			port = (CommPortIdentifier)ports.nextElement();
			if(port.getName().equals(portName))
				return port;
		}
		return null;
	}
	
	/**
	 * Method returns whether Serial port is bonded
	 * @return
	 */
	public boolean isBoundToSerialPort(){
		return mIsBoundedToUsbPort;
	}

	
	/**
	 * Method fire status to listeners
	 * @param status
	 */
	private void fireStatusFromSerialPort(boolean aIsReady){
		mListener.onSerialPortStateChanged(aIsReady);
	}
	
	/**
	 * Method fire errors to listeners
	 * @param status
	 */
	private void fireSerialPortErrors(String msg){
		PegasusLogger.getInstance().e(getName(), "fireSerialPortErrors", msg);
		mListener.onSerialPortError(msg);
	}
	
	
	/**
	 * Change driving direction
	 * @param direction 'F' - Forward, 'B' - Backward
	 */
	public void changeDrivingDirection(String direction){
		String msgToArduino = MessageVaribles.START_MESSAGE 
						+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MESSAGE_TYPE_ACTION + MessageVaribles.MESSAGE_SAPERATOR
						+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VEHICLE_ACTION_CHANGE_DIRECTION + MessageVaribles.MESSAGE_SAPERATOR
						+ MessageVaribles.KEY_DRIVING_DIRECTION + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + direction 
						+ MessageVaribles.END_MESSAGE;
		addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(getName(), "changeDrivingDirection", msgToArduino);
	}
	
	/**
	 * Change Back Motor speed
	 * @param digitalSpeed
	 */
	public void changeSpeed(int digitalSpeed){
		String msgToArduino = MessageVaribles.START_MESSAGE  
							+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MESSAGE_TYPE_ACTION + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VEHICLE_ACTION_CHANGE_SPEED + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_DIGITAL_SPEED + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + digitalSpeed 
							+ MessageVaribles.END_MESSAGE;
		addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(getName(), "changeSpeed", msgToArduino);
	}
	
	/**
	 * method change the steering angle
	 * @param direction 'R' - Right or 'L' - Left
	 * @param angle - the rotation angle
	 */
	public void changeSteerMotor(String direction, double angle){
		String msgToArduino = MessageVaribles.START_MESSAGE 
							+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MESSAGE_TYPE_ACTION + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VEHICLE_ACTION_CHANGE_STEERING + MessageVaribles.MESSAGE_SAPERATOR 
							+ MessageVaribles.KEY_STEERING_DIRECTION + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + direction + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_ROTATION_ANGLE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + angle
							+ MessageVaribles.END_MESSAGE;
		addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(getName(), "changeSteerMotor", msgToArduino);
	}
	
	
	/**
	 * Change sensor state
	 * @param sensorID - sensor id
	 * @param state - 0 disable, 1 - enable
	 */
	public void changeSensorState(int sensorID,int state){
		String msgToArduino = MessageVaribles.START_MESSAGE 
				+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MESSAGE_TYPE_ACTION + MessageVaribles.MESSAGE_SAPERATOR
				+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VEHICLE_ACTION_CHANGE_SENSOR_STATE + MessageVaribles.MESSAGE_SAPERATOR 
				+ MessageVaribles.KEY_SENSOR_ID + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + sensorID + MessageVaribles.MESSAGE_SAPERATOR
				+ MessageVaribles.KEY_SENSOR_STATE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + state
				+ MessageVaribles.END_MESSAGE;
		addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(getName(), "changeSensorState", msgToArduino);
	}
	
	
	/**
	 * set sensors max distance, 
	 * @param sensorConfigurations sensor configuration containing relevant settings,
	 * key = id, value = max distance
	 */
	public void configSensors(JSONObject sensorConfigurations,int aDefaultMaxDistance){
		if(sensorConfigurations != null && sensorConfigurations.length() > 0){
			StringBuilder msgToSend = new StringBuilder();
			msgToSend.append(MessageVaribles.START_MESSAGE);
			msgToSend.append(MessageVaribles.KEY_MESSAGE_TYPE);
			msgToSend.append(MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR);
			msgToSend.append(MessageVaribles.MESSAGE_TYPE_SETTINGS);
			msgToSend.append(MessageVaribles.MESSAGE_SAPERATOR);
			msgToSend.append(MessageVaribles.KEY_SETTINGS_TYPE);
			msgToSend.append(MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR);
			msgToSend.append(MessageVaribles.SETTINGS_SET_SENSORS);
			Iterator<String> it = sensorConfigurations.keys();
			while(it.hasNext()){
				msgToSend.append(MessageVaribles.MESSAGE_SAPERATOR);
				String key = it.next();
				int maxDistance = sensorConfigurations.optInt(key, aDefaultMaxDistance);
				msgToSend.append(MessageVaribles.KEY_SETTINGS_SENSOR_PREFIX + key);
				msgToSend.append(MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR);
				msgToSend.append(maxDistance);
			}
			msgToSend.append(MessageVaribles.END_MESSAGE);	
			String msgToArduino = msgToSend.toString();
			addMessageToQueue(msgToArduino);
			PegasusLogger.getInstance().d(getName(), "configSensors", msgToArduino);
		}
	}
	
	
	
}
