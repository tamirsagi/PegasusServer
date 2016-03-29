package logs.logger;

import logs.AbstractLogger;

public class PegasusLogger extends AbstractLogger {

	
	
	public static AbstractLogger getInstance(){
		if(mInstance == null){
			mInstance = new PegasusLogger();
		}
		return mInstance;
	}
	
	private PegasusLogger(){
		
		
	}
	

	@Override
	public void i(String tag, String method, String msg) {
		System.out.println(getLogFormat(I, tag, method, msg));
		
	}

	@Override
	public void d(String tag, String method,String msg) {
		System.out.println(getLogFormat(D, tag, method, msg));
		
	}

	@Override
	public void e(String tag, String method, String msg) {
		System.err.println(getLogFormat(E, tag, method, msg));
		
	}

	@Override
	public void v(String tag, String method, String msg) {
		System.out.println(getLogFormat(V, tag, method, msg));
		
	}
	

}
