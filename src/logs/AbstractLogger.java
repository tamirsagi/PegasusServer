package logs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractLogger {
	private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static Date date = new Date();
	protected static final String I = "I";
	protected static final String D = "D";
	protected static final String E = "E";
	protected static final String V = "V";
	
	protected static AbstractLogger mInstance;
	
	
	public static AbstractLogger getInstance() throws Exception{
		if(mInstance == null){
			throw new Exception("Logger has not initizlized yet");
		}
		return mInstance;
	}
	
	private String getCurrentTime(){
		return dateFormat.format(date);
	}
	
	
	/**
	 * 
	 * @param tag - sender
	 * @param msg - message
	 * @return - Log format
	 */
	public String getLogFormat(String logType, String tag,String method,String msg){
		return String.format("[%s]%s/%s - %s():[%s]",getCurrentTime(),logType,tag,method,msg);
	}
	
	/**
	 * Log Info message
	 * @param msg - Message to Log
	 */
	public abstract void i(String tag, String method, String msg);
	
	/**
	 * Log Info message
	 * @param msg - Message to Log
	 */
	public abstract void i(String tag,String msg);
	
	/**
	 * Log Info message
	 * @param msg - Message to Log
	 */
	public abstract void i(String msg);
	
	
	/**
	 * Log debug message
	 * @param msg - Message to Log
	 */
	public abstract void d(String tag, String method, String msg);
	
	/**
	 * Log debug message
	 * @param msg - Message to Log
	 */
	public abstract void d(String tag, String msg);
	
	/**
	 * Log debug message
	 * @param msg - Message to Log
	 */
	public abstract void d(String msg);
	
	
	/**
	 * Log error message
	 * @param msg - Message to Log
	 */
	public abstract void e(String tag, String method, String msg);
	
	/**
	 * Log error message
	 * @param msg - Message to Log
	 */
	public abstract void e(String tag, String msg);
	
	/**
	 * Log error message
	 * @param msg - Message to Log
	 */
	public abstract void e(String msg);
	
	
	/**
	 * Verbose messages
	 * @param msg
	 */
	public abstract void v(String tag, String method, String msg);
	
	/**
	 * Verbose messages
	 * @param msg
	 */
	public abstract void v(String tag, String msg);
	
	/**
	 * Verbose messages
	 * @param msg
	 */
	public abstract void v(String msg);

}
