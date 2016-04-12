package vehicle.common;

import vehicle.interfaces.OnTimerListener;

public class ActionTimer extends Thread {
	private static final String TAG = "ActionTimer";
	private long mMaxTime;
	private long mCurrentTime;
	private long mStartedTime;
	private boolean mIsTimerRunning;
	private boolean mTimerStopped;
	private OnTimerListener mListener;
	
	
	public ActionTimer(long aMaxTime, OnTimerListener aListener){
		mMaxTime = aMaxTime;
		mListener = aListener;
		
	}

	@Override
	public void run() {
		while(mIsTimerRunning){
			mCurrentTime = System.currentTimeMillis();
			mIsTimerRunning = (mCurrentTime - mStartedTime) < mMaxTime;
		}
		if(!mTimerStopped){
			mListener.onTimerIsOver();
		}
		
	}
	
	/**
	 * start Timer thread
	 */
	public void startTimer(){
		mStartedTime = System.currentTimeMillis();
		mIsTimerRunning = true;
		start();
	}
	
	/**
	 * stop timer when action is finished before timer is over
	 */
	public void stopTimer(){
		mTimerStopped = true;
		mIsTimerRunning = false;
		mStartedTime = 0;
	}
	
	/**
	 * stop thread immediately
	 */
	public void killThread(){
		mIsTimerRunning = false;
	}
	
	
	

}
