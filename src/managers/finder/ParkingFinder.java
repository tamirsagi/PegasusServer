package managers.finder;

import logs.logger.PegasusLogger;
import managers.common.AbstractManager;
import managers.finder.constants.ParkingType;

import org.json.JSONException;
import org.json.JSONObject;

import vehicle.common.ActionTimer;
import vehicle.common.VehicleData;
import vehicle.interfaces.OnTimerListener;
import vehicle.pegasus.PegasusVehicle;
import vehicle.pegasus.constants.SensorPositions;
import control.interfaces.OnParkingEventsListener;

/**
 * Handle parking searching.
 * @author Tamir
 *
 */
public class ParkingFinder extends AbstractManager implements OnTimerListener{
	
	private static ParkingFinder mInstance;
	
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 60 * 1000 * 2; // 2 minutes for searching
	
	private static final int DEFAULT_VALUE = -1;
	
	private static final String KEY_SHOULD_ADD_TRAVELLED_DISTANCE = "should_add_travelled_distance";
	private static final String KEY_LAST_SENSOR_DATA = "last_Sensor_data";
	private static final String KEY_DISTANCE_SINCE_STARTED = "distance_since_started";
	
	private int mParkingType;
	private boolean mFound;
	private double mRelevantSpace = DEFAULT_VALUE;
	private OnParkingEventsListener mListener;
	private ActionTimer mTimer;
	private double mMinSpace;
	private JSONObject mCurrentParkingProcessParams;
	
	
	
	public static ParkingFinder getInstance(){
		if(mInstance == null){
			mInstance = new ParkingFinder(ParkingFinder.class.getSimpleName());
		}
		return mInstance;
	}
	
	private ParkingFinder(String aTag){
		super(aTag);
		
		mCurrentParkingProcessParams = new JSONObject();
	}
	
	@Override
	public void run() {
		PegasusLogger.getInstance().i(getTag(),"Parking Finder has been started...");
		while(mIsworking){
			
			synchronized (this) {
				try{
					while(mIsSuspended){
						wait();
					}
				}catch(InterruptedException e){
					PegasusLogger.getInstance().e(getTag(),e.getMessage());
				}
			}
			if(mRelevantSpace >= mMinSpace){
				mFound = true;
				mListener.onParkingFound();
				if(mTimer != null){
					mTimer.killThread();
				}
			}else{
				
			}
			
			
		}
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
	public void searchParking(int aParkingType, double aMinSpaceToPark){
		if(aMinSpaceToPark > 0){
			PegasusLogger.getInstance().i(getTag(), "findParking", "started looking for parking with min space of: " + aMinSpaceToPark);
			mMinSpace = aMinSpaceToPark;
			mParkingType = aParkingType;
			mFound = false;
			mRelevantSpace = 0;
			if(mTimer != null){
				mTimer.killThread();
			}
			mTimer = new ActionTimer(FINDING_PARKING_SPOT_TIMEOUT, this);
			mTimer.startTimer();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized void updateInput(int sensorId, double value){
		switch (mParkingType){
		case ParkingType.PARALLEL_RIGHT:
		case ParkingType.PARALLEL_LEFT:
			handleIncomingDataOnParallelParking(sensorId,value);
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
	private void handleIncomingDataOnParallelParking(int aSensorId, double aValue){
		try {
			if(!mFound){
				if(aSensorId == SensorPositions.INFRA_RED_TACHOMETER_ID){
					boolean relevantDistance = mCurrentParkingProcessParams.optBoolean(KEY_SHOULD_ADD_TRAVELLED_DISTANCE,true);
					if(relevantDistance){
						mRelevantSpace += aValue;
					}
				}else{
					mCurrentParkingProcessParams.put(KEY_LAST_SENSOR_DATA,aValue);
					if(aValue != 0){
						mRelevantSpace = 0;
						mCurrentParkingProcessParams.put(KEY_SHOULD_ADD_TRAVELLED_DISTANCE,false);
						
					}else{
						mCurrentParkingProcessParams.put(KEY_SHOULD_ADD_TRAVELLED_DISTANCE,true);
					}
				}
				mCurrentParkingProcessParams.put(KEY_DISTANCE_SINCE_STARTED, mRelevantSpace);
			}
		}catch (JSONException e) {
					e.printStackTrace();
				}
		
	}
	
	public int getParkingType(){
		return mParkingType;
	}

	
	@Override
	public void onTimerIsOver() {
		mFound = false;
		if(mListener != null){
			mListener.onParkingNoFound();
		}
	}

}
