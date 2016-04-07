package vehicle.Pegasus;

import vehicle.common.VehicleData;

/**
 * hold Pegasus parameters
 * @author pi
 *
 */
public class PegasusVehicleData extends VehicleData {
	
	public static PegasusVehicleData mInstance;
	private int mNumberOfUltraSonicSensors;
	
	public static PegasusVehicleData getInstance(){
		if(mInstance == null){
			mInstance = new PegasusVehicleData();
		}
		return mInstance;
	}
	
	private PegasusVehicleData(){}
	
	public void setNumberOfUltraSonicSensors(int num){
		mNumberOfUltraSonicSensors = num;
	}
	
	public int getNumberOfUltraSonicSensors(){return mNumberOfUltraSonicSensors;}

	@Override
	public String toString() {
		return "PegasusVehicleData [mNumberOfUltraSonicSensors="
				+ mNumberOfUltraSonicSensors + "]";
	}
	
	

}
