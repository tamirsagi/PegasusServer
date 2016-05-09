package vehicle.pegasus;

import logs.logger.PegasusLogger;

import org.json.JSONException;
import org.json.JSONObject;

import vehicle.common.VehicleData;
import vehicle.common.constants.VehicleConfigKeys;

/**
 * hold Pegasus parameters
 * @author pi
 *
 */
public class PegasusVehicleData extends VehicleData {
	private static final String TAG  = PegasusVehicleData.class.getSimpleName();
	
	private static PegasusVehicleData mInstance;
	private int mNumberOfUltraSonicSensors;
	
	public static void createInstance(JSONObject aProperties){
		mInstance = new PegasusVehicleData(aProperties);
	}
	
	public static PegasusVehicleData getInstance(){
		if(mInstance == null){
			try {
				mInstance = new PegasusVehicleData(PegasusVehicleProperties.getInstance().toJsonObject());
			} catch (JSONException e) {
				PegasusLogger.getInstance().e(TAG,"Could not get instance , " + e.getMessage());
			}
		}
		return mInstance;
	}
	
	private PegasusVehicleData(JSONObject aProperties){
		super(aProperties);
		int numberOfUltraSonicSensors = aProperties.optInt(VehicleConfigKeys.KEY_NUMBER_OF_ULTRA_SONIC_SENSORS,PegasusVehicleProperties.DEFAULT_VALUE);
		setNumberOfUltraSonicSensors(numberOfUltraSonicSensors);
		
		
	}
	
	public void setNumberOfUltraSonicSensors(int num){
		mNumberOfUltraSonicSensors = num;
	}
	
	public int getNumberOfUltraSonicSensors(){return mNumberOfUltraSonicSensors;}


	@Override
	public void setMinExtraSpaceOnParallelParking() {
		double diff_turning_radius_wheel_base = getFrontWheelTurningRadious() * getFrontWheelTurningRadious() - getWheelBase() * getWheelBase();
		mMinExtraSpaceOnParallelParking = Math.sqrt(diff_turning_radius_wheel_base +
				 Math.pow(getWheelBase() + getDistanceCentreFrontWheelToFrontCar(), 2) -
				 Math.pow( (Math.sqrt(diff_turning_radius_wheel_base) - getWidth()), 2) ) - 
				 getWheelBase() - getDistanceCentreFrontWheelToFrontCar();
		
	}
	
	@Override
	public String toString() {
		return String.format("[Vehicle Data : %s , Number Of Sensors:%s]",
				super.toString(),mNumberOfUltraSonicSensors);
	}

	@Override
	public void setTurningRadius() {
		mBackWheelTurningRadious = getWheelBase() / Math.tan(getSteeringAngle() * DEGREE_RADIANS_FACTOR);
		mFrontWheelTurningRadious = getWheelBase() / Math.sin(getSteeringAngle() * DEGREE_RADIANS_FACTOR);
	}

}
