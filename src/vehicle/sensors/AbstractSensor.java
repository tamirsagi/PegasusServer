package vehicle.sensors;

import java.util.Vector;

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
	private double mIncomingData;
	private Vector<Double> mLastValues;
	private Vector<onInputReceived> mListeners;
	

	public AbstractSensor(int id) {
		mId = id;
		mLastValues = new Vector<Double>();
	}

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public double getValue() {
		return mIncomingData;
	}

	public void setValue(double mValue) {
		setLastValue(mValue);
		this.mIncomingData = mValue;
	}

	public Vector<Double> getLastValues() {
		return mLastValues;
	}

	public void setLastValue(double aLastValue) {
		if(mLastValues.size() > 0 && mLastValues.get(mLastValues.size() - 1).doubleValue() != aLastValue){
			mLastValues.add(aLastValue);
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
	
	public void registerListener(onInputReceived aListner){
		if(mListeners == null){
			mListeners = new Vector<>();
		}
		mListeners.add(aListner);
	}
	
	/**
	 * update listeners when data is received
	 * @param value - data from sensor
	 */
	public void receivedData(double value){
		setValue(value);
		for(onInputReceived listener : mListeners){
			listener.onReceived(getId(), getValue());
		}
	}
	
	@Override
	public void onRecievedSensorData(double value) {
		receivedData(value);
	}
	
	/**
	 * each sensor must register itself to data supplier
	 */
	public abstract void registerToDataSupplier();
}
