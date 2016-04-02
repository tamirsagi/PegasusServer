package vehicle.Sensor;

import control.Controller;

public class UltraSonic extends AbstractSensor  {
	
	
		public UltraSonic(int id){
			super(id);
			setType(Constants.ULTRA_SONIC);
		}
		

		@Override
		public void registerToDataSupplier() {
			Controller.getInstance().registerSensor(getId(), this);
			
		}

}
