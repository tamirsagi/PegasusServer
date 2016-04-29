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


import communication.serialPorts.messages.MessageVaribles;


import logs.logger.PegasusLogger;


import util.MessagesMethods;
import vehicle.common.constants.VehicleParams;
import vehicle.interfaces.onSensorDataRecieved;

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
	private SerialPortParser mSerialPortParser;
	private InputStream mInputStream;
	private OutputStreamHandler mOutputStreamHandler;
	private boolean mIsWaiting;
	
	private OnSerialPortEventsListener mListener;		//keeps listeners
	
	public static SerialPortHandler getInstance(){
		if(mSerialPortHandler == null){
			mSerialPortHandler = new SerialPortHandler();
		}
		return mSerialPortHandler;
	}
	
	private SerialPortHandler(){
		setName(TAG);
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
		mSerialPortParser.registerSensor(sensorID, listener);
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
				CommPort commPort = portIdentifier.open(TAG,mTimeout);
				if(commPort instanceof SerialPort){
					mIsBoundedToUsbPort = true;
					mSerialPort = (SerialPort)commPort;
					mSerialPort.setSerialPortParams(PORT_BAUD, SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
					
					
					mSerialPortParser = new SerialPortParser();
					mSerialPortParser.startThread();
					mInputStream = mSerialPort.getInputStream();
					mOutputStreamHandler = new OutputStreamHandler(mSerialPort.getOutputStream());
					mOutputStreamHandler.startThread();
					
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
		PegasusLogger.getInstance().i(TAG,"run", "Serial port thread is running...");
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
	
	/**
	 * received message is being notified here
	 */
	public synchronized void serialEvent(SerialPortEvent event) {
		
		switch (event.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			StringBuilder message = new StringBuilder();
			int available = 0;
			byte[] received = null;
			try {
				mOutputStreamHandler.suspendThread();
				while ((available = mInputStream.available()) > 0) {
					received = new byte[available + 1];
					mInputStream.read(received);
					message.append(new String(received).trim());
					pullMessages(message);
					sleep(100);
				}
				if(message.length() > 0 ){
					pullMessages(message);
				}
				mOutputStreamHandler.resumeThread();
				
				}catch (Exception e) {
					fireSerialPortErrors("Serial Event Listener Exception:" + e.getMessage());
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
				PegasusLogger.getInstance().d(TAG, "pullMessages", msgToSend);
				handleIncomingMessageFromSerial(msgToSend);
				buffer.delete(first, last + 1);
			}
			first = buffer.indexOf(MessageVaribles.START_MESSAGE);
			last = buffer.indexOf(MessageVaribles.END_MESSAGE);
		}
	}
			
	
	/**
	 * close connection to serial port
	 */
	public void disconnect(){
		try {
			if(mInputStream != null)
				mInputStream.close();
			if(mOutputStreamHandler != null)
				mOutputStreamHandler.disconnect();
			if(mSerialPortParser != null){
				mSerialPortParser.stopThread();
			}
			if(mSerialPort != null){
				mSerialPort.removeEventListener();
				mSerialPort.close();	//close serial port
				stopThread();
			}
		} catch (IOException e) {
			fireSerialPortErrors("disconnect() Exception " + e.getMessage());
		}
	}
	
	/**
	 * Method starts the thread
	 */
	public void startThread(){
		PegasusLogger.getInstance().d(TAG, "startThread", "Starting Serial Port Thread");
		start();
	}
	
	/**
	 * method stop Serial Thread
	 */
	public void stopThread(){
		PegasusLogger.getInstance().d(TAG, "stopThread", "Stopping Serial Port Thread");
		mIsBoundedToUsbPort = false;
	}
	
	public void serialPortSuspended(){
		PegasusLogger.getInstance().i(Thread.currentThread().getName(), "suspedning...");
		mIsWaiting = true;
	}
	
	public synchronized void serialPortResumed(){
		PegasusLogger.getInstance().i(Thread.currentThread().getName(), "resuming...");
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
		mOutputStreamHandler.addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(TAG, "updateSystemReady", "Update System Ready:" + msgToArduino);
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
	 * handle incoming message from Serial Port
	 * @param msg
	 */
	private void handleIncomingMessageFromSerial(String aMsg){
		PegasusLogger.getInstance().d(TAG, "fireMessageFromHardwareUnit", aMsg);
		mSerialPortParser.addMessageToQueue(aMsg);
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
		PegasusLogger.getInstance().e(TAG, "fireSerialPortErrors", msg);
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
		mOutputStreamHandler.addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(TAG, "changeDrivingDirection", msgToArduino);
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
		mOutputStreamHandler.addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(TAG, "changeSpeed", msgToArduino);
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
		mOutputStreamHandler.addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(TAG, "changeSteerMotor", msgToArduino);
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
		mOutputStreamHandler.addMessageToQueue(msgToArduino);
		PegasusLogger.getInstance().d(TAG, "changeSensorState", msgToArduino);
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
			mOutputStreamHandler.addMessageToQueue(msgToArduino);
			PegasusLogger.getInstance().d(TAG, "configSensors", msgToArduino);
		}
	}
	
	
	/**
	 * this class handles output stream in a separate thread.
	 * @author Tamir
	 *
	 */
	private class OutputStreamHandler extends Thread{
		private static final String TAG = "SP-Output Stream Handler";
		private boolean mIsBoundToSerial;
		private PrintWriter mOutputHandler;
		private Queue<String> mMessagesToArduino;
		private boolean mIsSuspended;
		
		public OutputStreamHandler(OutputStream aOutputStream){
			setName(TAG);
			mOutputHandler = new PrintWriter(aOutputStream,true);
			mMessagesToArduino = new LinkedList<String>();
			
		}
		
		/**
		 * Add message to queue,
		 * @param msg - message which is sent to Hardwareunit(Arduino)
		 */
		public synchronized void addMessageToQueue(String msg){
			if(msg != null && !msg.isEmpty()){
				mMessagesToArduino.add(msg);
			}
		}
		
		@Override
		public void run() {
			while(mIsBoundToSerial){
				
				synchronized (this) {
					try{
						while(mIsSuspended){
							wait();
						}
					}catch(Exception e){
						
					}
				}
					if(mMessagesToArduino.size() > 0){
						serialPortSuspended();
						while(mMessagesToArduino.size() > 0){
							writeMessage(mMessagesToArduino.poll());
						}
						serialPortResumed();
					}
				}
				disconnect();
			}

		/**
		 * send message to Arduino
		 * @param msg - msg to send
		 */
		private void writeMessage(String msg){
			if(mOutputHandler != null)
				try {
					PegasusLogger.getInstance().d(Thread.currentThread().getName(), "writeMessage", "before sending to arduino: " + msg);
					mOutputHandler.println(msg);
					Thread.sleep(100);
				} catch (Exception e) {
					fireSerialPortErrors("writeMessage(String msg) Exception " + e.getMessage());
				}
		}


		/**
		 * start output thread
		 */
		public void startThread(){
			mIsBoundToSerial = true;
			start();
		}
		
		/**
		 * close connection to output port
		 */
		public void disconnect(){
			if(mOutputHandler != null){
				mOutputHandler.close();
			}
			mIsBoundToSerial = false;
		}
		
		public void suspendThread(){
			PegasusLogger.getInstance().i(getName(), "suspedning...");
			mIsSuspended = true;
		}
		
		public synchronized void resumeThread(){
			PegasusLogger.getInstance().i(getName(), "resuming...");
			mIsSuspended = false;
			notify();
		}
	}
	
	/**
	 * class parse messages from serial port
	 * @author Tamir
	 *
	 */
	private class SerialPortParser extends Thread{
		
		private final String TAG = SerialPortParser.class.getSimpleName();
		private Queue<String> mMessageToHandle;
		private boolean mIsAlive;
		private HashMap<Integer,onSensorDataRecieved> mSensorLisenters;
		public SerialPortParser(){
			setName(TAG);
			mMessageToHandle = new LinkedList<String>();
			mSensorLisenters = new HashMap<Integer, onSensorDataRecieved>();
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
		
		/**
		 * add message to queue
		 * @param aMsg incoming message from Hardware Unit
		 */
		public synchronized void addMessageToQueue(String aMsg){
			mMessageToHandle.add(aMsg);
		}
		
		@Override
		public void run() {
			while(mIsAlive){
				if(mMessageToHandle.size() > 0){
					handleIncomingMessageFromHardwareUnit(mMessageToHandle.poll());
				}
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
					mListener.updateHardwareStatus(status_code);
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
		 * notify relevant sensor for new data
		 * @param sensorID
		 * @param value
		 */
		private void notifySensorForIncomingData(int sensorID,double value){
			if(sensorID > 0 && value >= 0){
				mSensorLisenters.get(sensorID).onRecievedSensorData(value);
			}
		}
		
		/**
		 * start Serial Port PArser Thread
		 */
		public void startThread(){
			mIsAlive = true;
			start();
		}
		
		/**
		 * stop thread
		 */
		public void stopThread(){
			mIsAlive = false;
		}
	}
	
}
