package vehicle.sensors;

import communication.serialPorts.SerialPortHandler;

/**
 * represent ultra sonic sensor
 * @author Tamir
 *
 */
public class UltraSonic extends AbstractSensor  {
	
		private int mMaxDistance;
		public UltraSonic(int id,int maxDistance){
			super(id);
			setType(SensorConstants.ULTRA_SONIC);
			setMaxDistance(maxDistance);
		}
		
		@Override
		public void registerToDataSupplier() {
			SerialPortHandler.getInstance().registerSensor(getId(), this);
		}
		
		public void setMaxDistance(int aMaxDistance){
			mMaxDistance = aMaxDistance;
		}
		
		public int getMaxDistance(){
			return mMaxDistance;
		}
}
