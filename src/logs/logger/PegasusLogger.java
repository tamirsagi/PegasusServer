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
	public synchronized void i(String tag, String method, String msg) {
		System.out.println(getLogFormat(I, tag, method, msg));
		
	}
	
	@Override
	public synchronized void i(String tag, String msg) {
		i(tag,"",msg);
		
	}

	@Override
	public synchronized void i(String msg) {
		i("","",msg);
		
	}

	@Override
	public synchronized void d(String tag, String method,String msg) {
		System.out.println(getLogFormat(D, tag, method, msg));
		
	}
	
	@Override
	public synchronized void d(String tag, String msg) {
		d(tag,"",msg);
		
	}

	@Override
	public synchronized void d(String msg) {
		d("","",msg);
		
	}

	@Override
	public synchronized void e(String tag, String method, String msg) {
		System.err.println(getLogFormat(E, tag, method, msg));
	}
	
	@Override
	public synchronized void e(String tag, String msg) {
		e(tag,"",msg);
	}
	
	@Override
	public synchronized void e(String msg) {
		e("","",msg);
	}

	@Override
	public synchronized void v(String tag, String method, String msg) {
		System.out.println(getLogFormat(V, tag, method, msg));
		
	}

	@Override
	public synchronized void v(String tag, String msg) {
		v(tag,"",msg);		
	}

	@Override
	public synchronized void v(String msg) {
		v("","",msg);			
	}

}
