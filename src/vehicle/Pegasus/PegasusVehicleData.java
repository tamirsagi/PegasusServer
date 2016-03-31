package vehicle.Pegasus;

import vehicle.VehicleData;

public class PegasusVehicleData extends VehicleData {
	
	public static PegasusVehicleData mInstance;
	private int mNumberOfUltraSonicSensors;
	
	public static PegasusVehicleData getInstance(){
		if(mInstance == null){
			mInstance = new PegasusVehicleData();
		}
		return mInstance;
	}
	
	public void setNumberOfUltraSonicSensors(int num){
		mNumberOfUltraSonicSensors = num;
	}
	
	public int getNumberOfUltraSonicSensors(){return mNumberOfUltraSonicSensors;}

}
