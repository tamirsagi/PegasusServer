package vehicle.algorithms.common;


public class AbstractManager extends Thread {
	
	protected boolean mIsworking;
	protected boolean mIsSuspended;
	
	
	
	
	/**
	 * start thread
	 */
	public void startThread(){
		mIsworking = true;
		start();
	}
	
	
	public void stopThread(){
		mIsworking = false;
	}
	
	public void suspendThread(){
		mIsSuspended = true;
	}
	
	public synchronized void resumeThread(){
		mIsSuspended = false;
		notify();
	}

}
