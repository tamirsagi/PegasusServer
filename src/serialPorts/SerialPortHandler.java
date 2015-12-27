package serialPorts;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * This Class Supports 2 way communication via USB port  
 * @author Tamir Sagi
 *
 */
public class SerialPortHandler {
	private static final String TAG = "SerialPortHandler";
	private static final String PortName = "/dev/ttyACM0"; //the mounted ttyPort for Arduino
	private static SerialPortHandler mSerialPortHandler;
	private boolean mIsBoundedToUsbPort;
	private int mTimeout;
	private SerialPort mSerialPort;
	private SerialInputHandler mSerialInputHandler;
	private SerialOutputHandler mSerialOutputHandler;
	
	
	public static SerialPortHandler getInstance(){
		if(mSerialPortHandler == null){
			mSerialPortHandler = new SerialPortHandler();
		}
		return mSerialPortHandler;
	}
	
	private SerialPortHandler(){
		connect();
	}
	
	
	/**
	 * Connect to Serial Port, Define streams
	 */
	private void connect(){
		try{
			//add the port manually, because version of RXTX gaps
			System.setProperty("gnu.io.rxtx.SerialPorts", PortName);
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PortName);
			if(portIdentifier.isCurrentlyOwned()){
				System.out.println("Error : Port is currently in use");
			}
			else{
				CommPort commPort = portIdentifier.open(TAG,mTimeout);
				if(commPort instanceof SerialPort){
					mIsBoundedToUsbPort = true;
					mSerialPort = (SerialPort)commPort;
					mSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
					mSerialInputHandler = new SerialInputHandler(mSerialPort.getInputStream());
					mSerialOutputHandler = new SerialOutputHandler(mSerialPort.getOutputStream());
					
					//start threads
					mSerialInputHandler.startThread();
					mSerialOutputHandler.startThread();
				}
			}
		}catch(Exception eSerialPort){
			System.out.println(eSerialPort.getMessage());
		}
	}
	
	
	
	/**
	 * add message to queue in order to write it
	 * @param msg - msg to send
	 */
	public void writeMessage(String msg){
		mSerialOutputHandler.addMessageToQueue(msg);
	}
	
	
	
	/**
	 * close connection to serial port
	 */
	public void disconnect(){
		mSerialInputHandler.stopThread(); 		//stop input thread
		mSerialOutputHandler.stopThread();		//stop output thread
		mSerialPort.close();					//close serial port
	}
	
	
	
	
	/**
	 * This class holds the input streams and get data from it.
	 * it fire the data to Logic Control;
	 * @author Tamir Sagi
	 *
	 */
	class SerialInputHandler extends Thread{
		private static final String TAG = "SerialInputHandler";
		private  InputStream mReader;
		private boolean mIsOnline; 
		
		public SerialInputHandler(InputStream in){
			mReader = in;
		}

		@Override
		public void run() {
			while(mIsOnline){
				readFromInputStream();
			}
		}
		
		
		
		/**
		 * Read Data from Input Stream
		 * It Fires the data 
		 */
		private void readFromInputStream(){
			byte[] buffer;
			try{
				while(mReader.available() > 0){
					buffer = new byte[mReader.available()];
					mReader.read(buffer);
					String s = new String(buffer);
					System.out.println(s);
					// TODO - Launch event with the msg, add string builder t
					//append the message from inputStream
				}
			}catch(IOException e){
				System.out.println(TAG + " " + e.getMessage());
			}
		}
		
		/**
		 * start current Thread
		 */
		public void startThread(){
			mIsOnline = true;
			start();
		}
		
		/**
		 * stop current thread
		 */
		public void stopThread(){
			mIsOnline = false;
		}
	}//end of SerialInputHandler
	
	

	
	/**
	 * this class handles the output stream via usb
	 * @author Tamir Sagi
	 *
	 */
	class SerialOutputHandler extends Thread{
		private static final String TAG = "SerialOutputHandler";
		private OutputStream mOutputStream;
		private boolean mIsOnline;
		private Queue<String> mMesseagesQueue;
		
		public SerialOutputHandler(OutputStream out){
			mOutputStream = out;
			mMesseagesQueue = new LinkedList<String>();
			mIsOnline = true;
		}
		
		@Override
		public void run() {
			while(mIsOnline){
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("sent to arduino");
				mMesseagesQueue.add("Coonected to Arduino :D \n");
				if(mMesseagesQueue.size() > 0){
					writeMessage(mMesseagesQueue.poll());
				}
			}
		}
		
		//write message to serial port
		private void writeMessage(String msg){
			byte[] msgToSent = msg.getBytes();
			try{
			mOutputStream.write(msgToSent);
			mOutputStream.flush();
			}catch(IOException e){
				System.out.println(TAG + " " + e.getMessage());
			}
		}
		
		//add message to Queue
		public void addMessageToQueue(String msg){
			mMesseagesQueue.add(msg);
		}
		
		/**
		 * start current Thread
		 */
		public void startThread(){
			mIsOnline = true;
			start();
		}
		
		/**
		 * stop current thread
		 */
		public void stopThread(){
			mIsOnline = false;
		}
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
	public static void findAllAvailablePorts(){
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier port;
		while(ports.hasMoreElements()){
			port = (CommPortIdentifier)ports.nextElement();
			System.out.println("port name : " + port.getName());
			System.out.println("port type " + port.getPortType());
		}
	}
	
	
}
