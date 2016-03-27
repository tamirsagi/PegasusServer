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
	public void i(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void d(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void e(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void v(String msg) {
		// TODO Auto-generated method stub
		
	}

}
