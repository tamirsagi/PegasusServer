package communication.serialPorts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import communication.messages.MessageVaribles;

import pegasusVehicle.params.VehicleParams;

import control.Interfaces.ISerialPortListener;

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
	private PrintWriter mOutput;
	
	private Queue<String> messagesToArduino;					//keeps messages in order to Hardware unit
	private HashMap<String,ISerialPortListener> listeners;		//keeps listeners
	
	public static SerialPortHandler getInstance(){
		if(mSerialPortHandler == null){
			mSerialPortHandler = new SerialPortHandler();
		}
		return mSerialPortHandler;
	}
	
	private SerialPortHandler(){
		setName(TAG);
		messagesToArduino = new LinkedList<>();
		listeners = new HashMap<String,ISerialPortListener>();
	}
	
	/**
	 * Register Listener
	 * @param name
	 * @param listener
	 */
	public void registerMessagesListener(String name,ISerialPortListener listener){
		listeners.put(name,listener);
	}
	
	/**
	 * Unregister Listener
	 * @param name
	 */
	public void unRegisterMessagesListener(String name){
		if(listeners.containsKey(name))
			listeners.remove(name);
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
					
					mInputStream = mSerialPort.getInputStream();
					mOutput = new PrintWriter(mSerialPort.getOutputStream(),true);

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
		System.out.println(TAG + " Service started");
		while(mIsBoundedToUsbPort){
			if(messagesToArduino.size() > 0)
				writeMessage(messagesToArduino.poll());
		}
		disconnect();
	}
	
	public synchronized void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			StringBuilder message = new StringBuilder();
			int available = 0;
			byte[] received = null;
			boolean messageStarted = false;
			try {
				
				while ((available = mInputStream.available()) > 0) {
					received = new byte[available + 1];
					mInputStream.read(received);
					message.append(new String(received));
					// find Start Message symbol
					if (!messageStarted) {
						int startMessagePos = message
								.indexOf(MessageVaribles.START_MESSAGE);
						if (startMessagePos >= 0) {
							if (startMessagePos == 0)
								message.deleteCharAt(0);
							else
								message.delete(0, startMessagePos);
							messageStarted = true;
						} else
							message = new StringBuilder();
					}
					if (message.length() > 0) {
						int endMessage = message
								.indexOf(MessageVaribles.END_MESSAGE);
						if (endMessage > 0) {
							if (message.length() > endMessage)
								message.delete(endMessage, message.length());
							System.out
									.println(TAG + " : " + message.toString());
							// fireMessageFromHardwareUnit(message.toString());
							message = new StringBuilder();
							messageStarted = false;
						}
					}
					sleep(100);
				}
				}catch (Exception e) {
					fireSerialPortErrors("Serial Event Listener Exception:" + e.getMessage());
				}
				break;
		}
	}
	

	/**
	 * send message to Arduino
	 * @param msg - msg to send
	 */
	public synchronized void writeMessage(String msg){
	  System.out.println("before sending to arduino: " + msg);
		if(mOutput != null)
			try {
				mOutput.println(msg);
				sleep(100);
			} catch (Exception e) {
				fireSerialPortErrors("writeMessage(String msg) Exception " + e.getMessage());
			}
	}

	
	/**
	 * close connection to serial port
	 */
	public void disconnect(){
//		mSerialOutputHandler.stopThread();		//stop output thread
		try {
			if(mInputStream != null)
				mInputStream.close();
			if(mOutput != null)
				mOutput.close();
			if(mSerialPort != null){
				mIsBoundedToUsbPort = false;
				mSerialPort.removeEventListener();
				mSerialPort.close();	//close serial port
			}
		} catch (IOException e) {
			fireSerialPortErrors("disconnect() Exception " + e.getMessage());
		}
	}
	
	/**
	 * Method starts the thread
	 */
	public void startThread(){
		connect();
		start();
	}
	
	/**
	 * method stop Serial Thread
	 */
	public void stopThread(){
		mIsBoundedToUsbPort = false;
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
			System.out.println("port name : " + port.getName());
			System.out.println("port type " + port.getPortType());
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
	 * fire incoming message from Serial Port
	 * @param msg
	 */
	private void fireMessageFromHardwareUnit(String msg){
		
		for(String key : listeners.keySet())
			listeners.get(key).onMessageReceivedFromHardwareUnit(msg);
	}
	
	
	/**
	 * Method fire status to listeners
	 * @param status
	 */
	private void fireStatusFromSerialPort(boolean status){
		for(String key : listeners.keySet())
			listeners.get(key).onSerialStatusChanged(status);
		
	}
	
	/**
	 * Method fire errors to listeners
	 * @param status
	 */
	private void fireSerialPortErrors(String msg){
		for(String key : listeners.keySet())
			listeners.get(key).onSerialPortError(msg);
		
	}
	
	
	/**
	 * Change driving direction
	 * @param direction 'F' - Forward, 'B' - Backward
	 */
	public void changeDrivingDirection(String direction){
		String msgToArduino = MessageVaribles.START_MESSAGE 
						+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MessageType.ACTION.getValue() + MessageVaribles.MESSAGE_SAPERATOR
						+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VehicleActions.CHANGE_DIRECTION.getValue() + MessageVaribles.MESSAGE_SAPERATOR
						+ MessageVaribles.KEY_DRIVING_DIRECTION + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + direction 
						+ MessageVaribles.END_MESSAGE;
		messagesToArduino.add(msgToArduino);
	}
	
	/**
	 * Change Back Motor speed
	 * @param digitalSpeed
	 */
	public void changeSpeed(int digitalSpeed){
		String msgToArduino = MessageVaribles.START_MESSAGE  
							+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MessageType.ACTION.getValue() + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VehicleActions.CHANGE_SPEED.getValue() + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_DIGITAL_SPEED + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + digitalSpeed 
							+ MessageVaribles.END_MESSAGE;
		messagesToArduino.add(msgToArduino);
	}
	
	/**
	 * method change the steering angle
	 * @param direction 'R' - Right or 'L' - Left
	 * @param angle - the rotation angle
	 */
	public void changeSteerMotor(String direction, double angle){
		String msgToArduino = MessageVaribles.START_MESSAGE 
							+ MessageVaribles.KEY_MESSAGE_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + MessageVaribles.MessageType.ACTION.getValue() + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_VEHICLE_ACTION_TYPE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + VehicleParams.VehicleActions.STEERING.getValue() + MessageVaribles.MESSAGE_SAPERATOR 
							+ MessageVaribles.KEY_STEERING_DIRECTION + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + direction + MessageVaribles.MESSAGE_SAPERATOR
							+ MessageVaribles.KEY_ROTATION_ANGLE + MessageVaribles.MESSAGE_KEY_VALUE_SAPERATOR + angle
							+ MessageVaribles.END_MESSAGE;
		messagesToArduino.add(msgToArduino);
	}
	
	
	
	
	
}
