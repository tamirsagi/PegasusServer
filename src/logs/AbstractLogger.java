package logs;

public abstract class AbstractLogger {
	
	protected static AbstractLogger mInstance;
	
	
	public static AbstractLogger getInstance() throws Exception{
		if(mInstance == null){
			throw new Exception("Logger has not initizlized yet");
		}
		return mInstance;
	}
	
	
	/**
	 * Log Info message
	 * @param msg - Message to Log
	 */
	public abstract void i(String msg);
	
	
	/**
	 * Log debug message
	 * @param msg - Message to Log
	 */
	public abstract void d(String msg);
	
	
	/**
	 * Log error message
	 * @param msg - Message to Log
	 */
	public abstract void e(String msg);
	
	
	/**
	 * Verbose messages
	 * @param msg
	 */
	public abstract void v(String msg);

}
