package vehicle.algorithms;

import logs.logger.PegasusLogger;

import org.json.JSONObject;

import vehicle.Interfaces.OnTimerListener;
import vehicle.Pegasus.PegasusVehicle;
import vehicle.algorithms.common.AbstractManager;
import vehicle.common.ActionTimer;
import vehicle.common.VehicleData;
import control.Interfaces.OnParkingEventsListener;

public class ParkingFinder extends AbstractManager implements OnTimerListener{
	
	private static ParkingFinder mInstance;
	
	private static final String TAG = ParkingFinder.class.getSimpleName();
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 60 * 1000 * 2; // 2 minutes for searching
	
	private static final int DEFAULT_VALUE = -1;
	
	private int mParkingType;
	private boolean mFound;
	private double mDistanceSinceStarted;
	private OnParkingEventsListener mListener;
	private ActionTimer mTimer;
	private double mMinSpace;
	private JSONObject mCurrentParkingProcessParams;
	
	
	
	public static ParkingFinder getInstance(){
		if(mInstance == null){
			mInstance = new ParkingFinder();
		}
		return mInstance;
	}
	
	private ParkingFinder(){
		VehicleData vehicleData = PegasusVehicle.getInstance().getVehicleData();
		mMinSpace = vehicleData.getLength() + 2 * vehicleData.getMinimumRequiredSpaceToPark();
		mCurrentParkingProcessParams = new JSONObject();
	}
	
	@Override
	public void run() {
		PegasusLogger.getInstance().i(TAG,"PArking Finder has been started...");
		while(mIsworking){
			
			synchronized (this) {
				try{
					while(mIsSuspended){
						wait();
					}
				}catch(InterruptedException e){
					PegasusLogger.getInstance().e(TAG,e.getMessage());
				}
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
	public void findParking(int aParkingType){
		PegasusLogger.getInstance().i(TAG, "findParking", "started looking for parking");
		mParkingType = aParkingType;
		mFound = false;
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
		if(!mFound){
			if(aValue == 0){
				
				
				
				
			}
		}
		
		
		
		
	}
	
	public int getParkingType(){
		return mParkingType;
	}
	
	/**
	 * set minimum space required for a proper parking spot
	 * @param aMinSpace
	 */
	public void setMinSpaceForParking(double aMinSpace){
		mMinSpace = aMinSpace;
	}
	
	public synchronized void  setTravelledDistance(double aDistance){
		mDistanceSinceStarted += aDistance;
	}

	
	@Override
	public void onTimerIsOver() {
		mFound = false;
		if(mListener != null){
			mListener.onParkingNoFound();
		}
	}

}
