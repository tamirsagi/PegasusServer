package PegasusVehicle;

public abstract class AbstractVehicle {
	
	private String mName;
	private int mID;
	private double mVehicleLength;
	private double mVehicleWidth;
	private double mRadious;
	private int mNumberOfSensors;
	
	
	public AbstractVehicle(){}
	
	public AbstractVehicle(double mVehicleLength,double mVehicleWidth ){
		setVehicleWidth(mVehicleWidth);
		setmVehicleLength(mVehicleLength);
	}
	
	public AbstractVehicle(int id, String name, double mVehicleLength,double mVehicleWidth){
		this(mVehicleLength,mVehicleWidth);
		setID(id);
		setName(name);
	}
	

	public double getmVehicleLength() {
		return mVehicleLength;
	}

	public void setmVehicleLength(double mVehicleLength) {
		this.mVehicleLength = mVehicleLength;
	}

	public double getmVehicleWidth() {
		return mVehicleWidth;
	}

	public void setVehicleWidth(double mVehicleWidth) {
		this.mVehicleWidth = mVehicleWidth;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public int getID() {
		return mID;
	}

	public void setID(int mID) {
		this.mID = mID;
	}
	
	public double getRadious(){
		return mRadious;
	}
	
	public void setRadious(double radious){
		mRadious = radious;
	}
	
	/**
	 * Handle the vehicle speed
	 * @param digitalSpeed
	 */
	public abstract void changeSpeed(int digitalSpeed);
	
	
	/**
	 * turn the vehicle right with a steering angle
	 * @param rotatioAngle
	 */
	public abstract void turnRight(double rotationAngle);
	
	/**
	 * turn the vehicle left with a steering angle
	 * @param rotatioAngle
	 */
	public abstract void turnLeft(double rotationAngle);
	
	/**
	 * set driving direction Forward
	 */
	public abstract void driveForward();
	
	/**
	 * set driving direction backward
	 */
	public abstract void driveBackward();
	
	/**
	 * stop the vehicle
	 */
	public abstract void stop();

	
	@Override
	public String toString() {
		return "Vehicle [mName=" + mName + ", mID=" + mID + ", mVehicleLength="
				+ mVehicleLength + ", mVehicleWidth=" + mVehicleWidth
				+ ", mRadious=" + mRadious + ", mNumberOfSensors="
				+ mNumberOfSensors + "]";
	}
	
	
	
}
