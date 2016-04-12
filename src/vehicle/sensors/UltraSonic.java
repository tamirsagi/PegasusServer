package vehicle.sensors;

import communication.serialPorts.SerialPortHandler;

/**
 * represent ultra sonic sensor
 * @author Tamir
 *
 */
public class UltraSonic extends AbstractSensor  {
	
		public UltraSonic(int id){
			super(id);
			setType(SensorConstants.ULTRA_SONIC);
		}
		
		@Override
		public void registerToDataSupplier() {
			SerialPortHandler.getInstance().registerSensor(getId(), this);
		}
}
