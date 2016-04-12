package vehicle.algorithms.common;


public abstract class AbstractManager extends Thread {
	
	protected String mTag;
	protected boolean mIsworking;
	protected boolean mIsSuspended;
	
	
	public AbstractManager(String aTag){
		setName(aTag);
		setTag(aTag);
	}
	
	public abstract void updateInput(int sensorId, double value);
	
	
	
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
		mIsSuspended = true;
	}
	
	public synchronized void resumeThread(){
		mIsSuspended = false;
		notify();
	}

}
