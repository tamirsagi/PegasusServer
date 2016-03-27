package pegasusVehicle;

public class Sensor {

	private int mId;
	private double mValue;
	private double mLastValue;
	private boolean mEnabled;
	private String mPosition; 	// keep the position on the vehicle (for example : "FL" = Front Left)

	public Sensor(int id) {
		mId = id;
	}

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public double getValue() {
		return mValue;
	}

	public void setValue(double mValue) {
		this.mValue = mValue;
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

}
