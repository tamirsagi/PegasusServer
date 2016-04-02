package vehicle.Sensor;

import control.Controller;

public class InfraRed extends AbstractSensor {
	
	public InfraRed(int id) {
		super(id);
		setType(Constants.INFRA_RED);
	}
	
	@Override
	public void registerToDataSupplier() {
		Controller.getInstance().registerSensor(getId(), this);
		
	}

}
