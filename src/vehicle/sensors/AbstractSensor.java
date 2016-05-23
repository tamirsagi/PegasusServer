package vehicle.sensors;

import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vehicle.interfaces.onInputReceived;
import vehicle.interfaces.onSensorDataRecieved;

/**
 * class represent a sensor 
 * @author Tamir
 *
 */
public abstract class AbstractSensor implements onSensorDataRecieved{
	
	private int mType;
	private int mId;
	private String mPosition;
	private boolean mEnabled;
	protected double mLastValue;
	protected Lock mLock;
	

	public AbstractSensor(int id) {
		mId = id;
		mLock = new ReentrantLock(true);
	}

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public double getLastValue() {
		mLock.lock();
		try{
			return mLastValue;
		}finally{
			mLock.unlock();
		}
	}

	public void setValue(double mValue) {
		mLock.lock();
		try{
			mLastValue = mValue;
		}finally{
			mLock.unlock();
		}
	}
	
	public String getPosition() {
		return mPosition;
	}

	public void setPosition(String aPosition) {
		this.mPosition = aPosition;
	}

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setSensorState(boolean isEnabled) {
		mEnabled = isEnabled;
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}
	
	
	@Override
	public void onRecievedSensorData(double value) {
		setValue(value);
	}
	
	/**
	 * each sensor must register itself to data supplier
	 */
	public abstract void registerToDataSupplier();
}
