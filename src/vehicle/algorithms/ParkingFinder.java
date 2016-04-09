package vehicle.algorithms;

import org.json.JSONObject;

import control.Interfaces.OnParkingEventsListener;

import vehicle.Interfaces.OnTimerListener;
import vehicle.common.ActionTimer;

public class ParkingFinder implements OnTimerListener{
	
	private static ParkingFinder mInstance;
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 60 * 1000 * 2; // 2 minutes for searching
	
	private int mParkingType;
	private boolean mFound;
	private double mDistanceSinceStarted;
	private OnParkingEventsListener mListener;
	private ActionTimer mTimer;
	
	
	public static ParkingFinder getInstance(){
		if(mInstance == null){
			mInstance = new ParkingFinder();
		}
		return mInstance;
	}
	
	private ParkingFinder(){
		
	}
	
	public void registerParkingEventsListner(OnParkingEventsListener aListner){
		if(aListner != null){
			mListener = aListner;
		}
	}
	
	/**
	 * find parking spot in a given position
	 * @param aParkingType given position
	 */
	public void findParking(int aParkingType){
		mParkingType = aParkingType;
		if(mTimer != null){
			mTimer.killThread();
		}
		mTimer = new ActionTimer(FINDING_PARKING_SPOT_TIMEOUT, this);
		mTimer.startTimer();
	}
	
	/**
	 * 
	 */
	public void updateInput(int sensorId, double value){
		switch (mParkingType){
		case ParkingType.PARALLEL_RIGHT:
			handleIncomingDataOnParallelRight(sensorId,value);
			break;
		case ParkingType.PARALLEL_LEFT:
			
			break;
		case ParkingType.ANGULAR_RIGHT:
			break;
			
		case ParkingType.ANGULAR_LEFT:
			break;
		}
	}
	
	/**
	 * Methods handles incoming data for right parallel parking searching
	 * @param incomingData
	 */
	private void handleIncomingDataOnParallelRight(int sensorId, double value){
		
		
		
		
	}

	
	@Override
	public void onTimerIsOver() {
		mFound = false;
		if(mListener != null){
			mListener.onParkingNoFound();
		}
	}
	
	
	
	
	

}
