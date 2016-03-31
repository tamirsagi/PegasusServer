package vehicle.Sensor;

public class InfraRed extends AbstractSensor {
	
	public InfraRed(int id) {
		super(id);
		setType(Constants.INFRA_RED);
	}

}
