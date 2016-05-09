package vehicle.sensors;

import communication.serialPorts.SerialPortHandler;

/**
 * represents infra red sensor
 * @author Tamir
 *
 */
public class Tachometer extends AbstractSensor {
	
	public Tachometer(int id) {
		super(id);
		setType(SensorConstants.INFRA_RED);
	}
	
	@Override
	public void registerToDataSupplier() {
		SerialPortHandler.getInstance().registerSensor(getId(), this);
		
	}

	@Override
	public void setValue(double mValue) {
		mLock.lock();
		try{
			mLastValue += mValue;
		}finally{
			mLock.unlock();
		}
	}
	
	/**
	 * reset tachometer data when state is changed
	 */
	public void resetTachometer(){
		mLock.lock();
		try{
			mLastValue = 0;
		}finally{
			mLock.unlock();
		}
	}
	
	

}
