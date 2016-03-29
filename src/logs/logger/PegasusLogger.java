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
	public void i(String tag, String msg) {
		System.out.println(getLogFormat(I, tag, msg));
		
	}

	@Override
	public void d(String tag, String msg) {
		System.out.println(getLogFormat(D, tag, msg));
		
	}

	@Override
	public void e(String tag, String msg) {
		System.err.println(getLogFormat(E, tag, msg));
		
	}

	@Override
	public void v(String tag, String msg) {
		System.out.println(getLogFormat(V, tag, msg));
		
	}
	

}
