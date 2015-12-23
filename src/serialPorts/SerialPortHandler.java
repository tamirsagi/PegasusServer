package serialPorts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

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
	private static SerialPortHandler mSerialPortHandler;
	private String mPortName;
	private boolean mIsBoundedToUsbPort;
	private int mTimeout;
	private SerialPort mSerialPort;
	private SerialInputHandler mSerialInputHandler;
	private SerialOutputHandler mSerialOutputHandler;
	
	
	
	/**
	 * Connect to Serial Port, Define streams
	 */
	public void connect(){
		try{
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(mPortName);
			if(portIdentifier.isCurrentlyOwned()){
				System.out.println("Error : Port is currently in use");
			}
			else{
				CommPort commPort = portIdentifier.open(TAG,mTimeout);
				if(commPort instanceof SerialPort){
					mIsBoundedToUsbPort = true;
					mSerialPort = (SerialPort)commPort;
					mSerialPort.setSerialPortParams(57600, SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
					mSerialInputHandler = new SerialInputHandler(mSerialPort.getInputStream());
					mSerialOutputHandler = new SerialOutputHandler(mSerialPort.getOutputStream());
					
					//start threads
					mSerialInputHandler.start();
					mSerialOutputHandler.start();
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
		private InputStream mInputStream;
		private boolean mIsOnline; 
		
		public SerialInputHandler(InputStream in){
			mInputStream = in;
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
				while(this.mInputStream.available() > 0){
					buffer = new byte[this.mInputStream.available()];
					this.mInputStream.read(buffer);
					String s = new String(buffer);
					// TODO - Launch event with the msg, add string builder t
					//append the message from inputStream
				}
			}catch(IOException e){
				System.out.println(TAG + " " + e.getMessage());
			}
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
		}
		
		@Override
		public void run() {
			while(mIsOnline){
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
			}catch(IOException e){
				System.out.println(TAG + " " + e.getMessage());
			}
		}
		
		//add message to Queue
		public void addMessageToQueue(String msg){
			mMesseagesQueue.add(msg);
		}
		
		/**
		 * stop current thread
		 */
		public void stopThread(){
			mIsOnline = false;
		}
	}
	
	
	
}
