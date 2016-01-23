package serialPorts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import Control.GeneralParams;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import Control.OnMessagesListener;

/**
 * This Class Supports 2 way communication via USB port  
 * @author Tamir Sagi
 *
 */
public class SerialPortHandler extends Thread implements SerialPortEventListener {
	private static final String TAG = "SerialPortHandler";
	private static final String PortName = "/dev/ttyACM0"; //the mounted ttyPort for Arduino
	private static final int PORT_BAUD = 115200;
	private static SerialPortHandler mSerialPortHandler;
	private boolean mIsBoundedToUsbPort;
	private int mTimeout;
	private SerialPort mSerialPort;
	private int testCount;
	
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	
	private Queue<String> messagesToArduino;
	private HashMap<String,OnMessagesListener> listeners;
	
	public static SerialPortHandler getInstance(){
		if(mSerialPortHandler == null){
			mSerialPortHandler = new SerialPortHandler();
		}
		return mSerialPortHandler;
	}
	
	private SerialPortHandler(){
		messagesToArduino = new LinkedList<>();
		listeners = new HashMap<String,OnMessagesListener>();
	}
	
	/**
	 * Register Listener
	 * @param name
	 * @param listener
	 */
	public void registerMessagesListener(String name,OnMessagesListener listener){
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
			CommPortIdentifier portIdentifier = getPort(PortName);
			if(portIdentifier == null){
				System.out.println("Could not find port " + PortName);
				return;
			}
			else if(portIdentifier.isCurrentlyOwned()){
				System.out.println("Error : Port: " + PortName +  " is currently in use");
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
					mOutputStream = mSerialPort.getOutputStream();

					mSerialPort.addEventListener(this);
					mSerialPort.notifyOnDataAvailable(true);
					mSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE); //disable flow control
				}
			}
		}catch(Exception eSerialPort){
			System.out.println(TAG + " " + eSerialPort.getMessage());
		}
	}
	
	
	@Override
	public void run() {
		System.out.println("Service started");
		while(mIsBoundedToUsbPort){
			if(messagesToArduino.size() > 0){
				writeMessage(messagesToArduino.poll());
				try {
					sleep(10);
				} catch (InterruptedException e) {
					System.out.println(TAG + " " + e.getMessage());
				}
			}
		}
		disconnect();
	}
	
	public synchronized void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()){
			case SerialPortEvent.DATA_AVAILABLE:
				StringBuilder message = new StringBuilder();
				int available = 0;
				byte[] received = null;
				try{
					while((available = mInputStream.available()) > 0){
						received = new byte[available];
						mInputStream.read(received);
						if(received[available - 1] == GeneralParams.END_MESSAGE){
							System.out.println(TAG + " : " + message.toString());
							fireMessageFromSerialPort(message.toString());
						}
						else
							message.append(new String(received));
					}
				}catch (Exception e) {
					System.out.println(TAG + " Serial Event Listener " + e.getMessage());
				}
				break;
		}
	}
	

	/**
	 * send message to Arduino
	 * @param msg - msg to send
	 */
	public synchronized void writeMessage(String msg){
		if(mOutputStream != null)
			try {
				mOutputStream.write(msg.getBytes());
				mOutputStream.flush();
			} catch (IOException e) {
				System.out.println(TAG + " " + e.getMessage());
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
			if(mOutputStream != null)
				mOutputStream.close();
			if(mSerialPort != null){
				mIsBoundedToUsbPort = false;
				mSerialPort.removeEventListener();
				mSerialPort.close();	//close serial port
			}
		} catch (IOException e) {
			System.out.println(TAG + " " + e.getMessage());
		}
	}
	
	public void startThread(){
		start();
		connect();
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
	private CommPortIdentifier getPort(String portName){
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
	 * fire incoming message from Serial Port
	 * @param msg
	 */
	private void fireMessageFromSerialPort(String msg){
		
		for(String key : listeners.keySet())
			listeners.get(key).onMessageReceivedFromSerialPort(msg);
	}
	
	
	/**
	 * Change Back Motor speed
	 * @param digitalSpeed
	 */
	public void changeBackMotor(int digitalSpeed){
		String msgToArduino = GeneralParams.KEY_ACTION_TYPE + ":" + GeneralParams.ACTION_BACK_MOTOR +",DS:" + digitalSpeed + GeneralParams.END_MESSAGE;
		messagesToArduino.add(msgToArduino);
	}
	
	/**
	 * method change the steering angle
	 * @param direction 'R' - Right or 'L' - Left
	 * @param angle - the rotation angle
	 */
	public void changeSteerMotor(char direction, double angle){
		String msgToArduino = GeneralParams.KEY_ACTION_TYPE + ":" + GeneralParams.ACTION_STEER_MOTOR 
				+ "," + GeneralParams.KEY_STEERING_DIRECTION + ":" + direction 
				+ "," + GeneralParams.KEY_ROTATION_ANGLE + ":" + angle
				+ GeneralParams.END_MESSAGE;
		messagesToArduino.add(msgToArduino);
	}
	
	
	/**
	 * Change driving direction
	 * @param direction 'F' - Forward, 'B' - Backward
	 */
	public void changeDrivingDirection(char direction){
		String msgToArduino = GeneralParams.KEY_ACTION_TYPE + ":" + GeneralParams.ACTION_DRIVING_DIRECTION +
				"," + GeneralParams.KEY_DRIVING_DIRECTION + ":" + direction + GeneralParams.END_MESSAGE;
		messagesToArduino.add(msgToArduino);
	}
	
	
}
