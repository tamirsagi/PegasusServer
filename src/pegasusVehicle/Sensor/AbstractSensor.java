package pegasusVehicle.Sensor;

public abstract class AbstractSensor {

	private int mType;
	private int mId;
	private String mPosition;
	private boolean mEnabled;
	private double mIncomingData;
	private double mLastValue;
	

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
		this.mIncomingData = mValue;
	}

	public double getLastValue() {
		return mLastValue;
	}

	public void setLastValue(double mLastValue) {
		this.mLastValue = mLastValue;
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
	

}
