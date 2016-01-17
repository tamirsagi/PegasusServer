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
	 * Method sends an event to move the car in
	 * @param digitalSpeed - set a speed (0-255 digital value)
	 * @param direction		- steering direction (Right or left)
	 * @param rotationAngle - steering angle
	 */
	public abstract void drive(int digitalSpeed,char direction, double rotationAngle);
	
	/**
	 * Handle the vehcile speed
	 * @param digitalSpeed
	 */
	public abstract void drive(int digitalSpeed);
	
	
	/**
	 * turn the vehcile right with a steering angle
	 * @param rotatioAngle
	 */
	public abstract void turnRight(double rotationAngle);
	
	/**
	 * turn the vehcile left with a steering angle
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
