package vehicle.sensors;

import communication.serialPorts.SerialPortHandler;

/**
 * represents infra red sensor
 * @author Tamir
 *
 */
public class InfraRed extends AbstractSensor {
	
	public InfraRed(int id) {
		super(id);
		setType(SensorConstants.INFRA_RED);
	}
	
	@Override
	public void registerToDataSupplier() {
		SerialPortHandler.getInstance().registerSensor(getId(), this);
		
	}

}
