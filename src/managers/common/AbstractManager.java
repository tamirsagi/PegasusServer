package managers.common;

import logs.logger.PegasusLogger;


public abstract class AbstractManager extends Thread {
	
	protected String mTag;
	protected boolean mIsworking;
	protected boolean mIsSuspended;
	
	
	protected AbstractManager(String aTag){
		setName(aTag);
		setTag(aTag);
	}
	
	private void setTag(String aTag){
		mTag = aTag;
	}
	
	public String getTag(){
		return mTag;
	}
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
		PegasusLogger.getInstance().i(getName(), "suspedning...");
		mIsSuspended = true;
	}
	
	public synchronized void resumeThread(){
		PegasusLogger.getInstance().i(getName(), "resuming...");
		mIsSuspended = false;
		notify();
	}
	
	public boolean isThreadSuspended(){
		return mIsSuspended;
	}

}
