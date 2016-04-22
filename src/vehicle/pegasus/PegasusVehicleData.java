package vehicle.pegasus;

import vehicle.common.VehicleData;

/**
 * hold Pegasus parameters
 * @author pi
 *
 */
public class PegasusVehicleData extends VehicleData {
	
	private static PegasusVehicleData mInstance;
	private int mNumberOfUltraSonicSensors;
	
	public static PegasusVehicleData getInstance(){
		if(mInstance == null){
			mInstance = new PegasusVehicleData();
		}
		return mInstance;
	}
	
	private PegasusVehicleData(){
		
	}
	
	public void setNumberOfUltraSonicSensors(int num){
		mNumberOfUltraSonicSensors = num;
	}
	
	public int getNumberOfUltraSonicSensors(){return mNumberOfUltraSonicSensors;}

	@Override
	public void setTurningRadious() {
		mTurningRadious = getWheelBase() / (2 * Math.sin(getSteeringAngle() * DEGREE_RADIANS_FACTOR));
		
	}

	@Override
	public void setMinimumRequiredSpaceToPark() {
		double diff_turning_radius_wheel_base = getTurningRadious() * getTurningRadious() - getWheelBase() * getWheelBase();
		mMinimumRequiredSpace = Math.sqrt(diff_turning_radius_wheel_base +
				 Math.pow(getWheelBase() + getDistanceCentreFrontWheelToFrontCar(), 2) -
				 Math.pow( (Math.sqrt(diff_turning_radius_wheel_base) - getWidth()), 2) ) - 
				 getWheelBase() - getDistanceCentreFrontWheelToFrontCar();
		
	}
	
	@Override
	public String toString() {
		return "PegasusVehicleData [mNumberOfUltraSonicSensors="
				+ mNumberOfUltraSonicSensors + "]";
	}

}
