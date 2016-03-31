package vehicle.Sensor;

import java.util.Vector;

import vehicle.Interfaces.onInputReceived;

/**
 * class represent a sensor 
 * @author Tamir
 *
 */
public abstract class AbstractSensor {

	private int mType;
	private int mId;
	private String mPosition;
	private boolean mEnabled;
	private double mIncomingData;
	private Vector<Double> mLastValue;
	private Vector<onInputReceived> mListeners;
	

	public AbstractSensor(int id) {
		mId = id;
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
		return mLastValue;
	}

	public void setLastValue(double aLastValue) {
		if(mLastValue.get(mLastValue.size() - 1).doubleValue() != aLastValue){
			mLastValue.add(aLastValue);
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
}
