package vehicle.algorithms;

import logs.logger.PegasusLogger;

import org.json.JSONException;
import org.json.JSONObject;

import control.Interfaces.OnParkingEventsListener;

import vehicle.Interfaces.OnTimerListener;
import vehicle.Pegasus.PegasusVehicle;
import vehicle.Pegasus.Constants.SensorPositions;
import vehicle.algorithms.Constants.ParkingFinderStateKeys;
import vehicle.common.ActionTimer;
import vehicle.common.VehicleData;

public class ParkingFinder implements OnTimerListener{
	
	private static ParkingFinder mInstance;
	
	private static final String TAG = ParkingFinder.class.getSimpleName();
	private static final long FINDING_PARKING_SPOT_TIMEOUT = 60 * 1000 * 2; // 2 minutes for searching
	
	private static final int DEFAULT_VALUE = -1;
	
	private int mParkingType;
	private boolean mFound;
	private double mDistanceSinceStarted;
	private OnParkingEventsListener mListener;
	private ActionTimer mTimer;
	private double mMinSpaceToParallel;
	private String mcurrentRelevantSensor;
	
	
	
	public static ParkingFinder getInstance(){
		if(mInstance == null){
			mInstance = new ParkingFinder();
		}
		return mInstance;
	}
	
	private ParkingFinder(){
		VehicleData vehicleData = PegasusVehicle.getInstance().getVehicleData();
		mMinSpaceToParallel = vehicleData.getLength() + 2 * vehicleData.getMinimumRequiredSpaceToPark();
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
		
		
		
		
		
	}
	
	public int getParkingType(){
		return mParkingType;
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
