package serialPorts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import Control.GeneralParams;
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
	private static final String TAG = "SerialPortHandler";
	private static final String PortName = "/dev/ttyACM0"; //the mounted ttyPort for Arduino
	private static final int PORT_BAUD = 115200;
	private static SerialPortHandler mSerialPortHandler;
	private boolean mIsBoundedToUsbPort;
	private int mTimeout;
	private SerialPort mSerialPort;
	private int testCount;
//	private SerialInputHandler mSerialInputHandler;
//	private SerialOutputHandler mSerialOutputHandler;
	
	private BufferedReader mReader;
	private OutputStream mOutputStream;
	
	
	public static SerialPortHandler getInstance(){
		if(mSerialPortHandler == null){
			mSerialPortHandler = new SerialPortHandler();
		}
		return mSerialPortHandler;
	}
	
	private SerialPortHandler(){
		
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
					
					mReader = new BufferedReader(new  InputStreamReader(mSerialPort.getInputStream()));
					mOutputStream = mSerialPort.getOutputStream();

					mSerialPort.addEventListener(this);
					mSerialPort.notifyOnDataAvailable(true);
					mSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE); //disable flow control
					
					
					//mSerialInputHandler = new SerialInputHandler(mSerialPort.getInputStream());
//					mSerialOutputHandler = new SerialOutputHandler(mSerialPort.getOutputStream());
					
					//start threads
//					mSerialInputHandler.startThread();
//					mSerialOutputHandler.startThread();
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
			
		}
		disconnect();
	}
	
	public synchronized void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()){
			case SerialPortEvent.DATA_AVAILABLE:
				try{
					while(mReader.ready()){
						String input = mReader.readLine();
						System.out.println("Received from Arduino size: " + input.length() + "[" + input +"]");
						testCount++;
						sleep(40);
						writeMessage("to arduino #" + testCount +"\n");
					}
				}catch (Exception e) {
					System.out.println(TAG + " SerialEventListener " + e.getMessage());
				}
				break;
		}
	}
	

	/**
	 * add message to queue in order to write it
	 * @param msg - msg to send
	 */
	public void writeMessage(String msg){
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
			if(mReader != null)
				mReader.close();
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
	
	
//	
//	/**
//	 * this class handles the output stream via usb
//	 * @author Tamir Sagi
//	 *
//	 */
//	class SerialOutputHandler extends Thread{
//		private static final String TAG = "SerialOutputHandler";
//		private OutputStream mOutputStream;
//		private boolean mIsOnline;
//		private Queue<String> mMesseagesQueue;
//		
//		public SerialOutputHandler(OutputStream out){
//			mOutputStream = out;
//			mMesseagesQueue = new LinkedList<String>();
//			mIsOnline = true;
//		}
//		
//		@Override
//		public void run() {
//			while(mIsOnline){
//				try {
//					sleep(3000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.out.println("sent to arduino");
//				mMesseagesQueue.add("abcdefghi\n");
//				if(mMesseagesQueue.size() > 0){
//					writeMessage(mMesseagesQueue.poll());
//				}
//			}
//		}
//		
//		//write message to serial port
//		private void writeMessage(String msg){
//			byte[] msgToSent = msg.getBytes();
//			try{
//			mOutputStream.write(msgToSent);
//			}catch(IOException e){
//				System.out.println(TAG + " " + e.getMessage());
//			}
//		}
//		
//		//add message to Queue
//		public void addMessageToQueue(String msg){
//			mMesseagesQueue.add(msg);
//		}
//		
//		/**
//		 * start current Thread
//		 */
//		public void startThread(){
//			mIsOnline = true;
//			start();
//		}
//		
//		/**
//		 * stop current thread
//		 */
//		public void stopThread(){
//			mIsOnline = false;
//		}
//	}

	
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
	 * Change Back Motor speed
	 * @param digitalSpeed
	 */
	public void changeBackMotor(int digitalSpeed){
		String msgToArduino = GeneralParams.
				getFixedMessageToArduino(GeneralParams.ACTION_BACK_MOTOR,digitalSpeed);
		writeMessage(msgToArduino);
	}
	
	/**
	 * method change the steering angle
	 * @param direction 'R' - Right or 'L' - Left
	 * @param angle - the roation angle
	 */
	public void changeSteerMotor(char direction, double angle){
		String msgToArduino = GeneralParams.
				getFixedMessageToArduino(GeneralParams.ACTION_STEER_MOTOR, 
						direction,angle);
		writeMessage(msgToArduino);
	}
	
	
	/**
	 * Change driving direction
	 * @param direction 'F' - Forward, 'b' - Backward
	 */
	public void changeDrivingDirection(char direction){
		String msgToArduino = GeneralParams.
				getFixedMessageToArduino(GeneralParams.ACTION_DRIVING_DIRECTION, direction);
		
		writeMessage(msgToArduino);
	}
	
	
}
