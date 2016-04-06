package vehicle.common;

public class VehicleData {
	
	private double mLength;
	private double mWidth;
	private double mTurningRadious;
	private double mWheelDiameter;
	private double mWheelPerimeter;
	private double mSteeringAngle;
	
	public double getLength() {
		return mLength;
	}
	public void setLength(double mLength) {
		this.mLength = mLength;
	}
	public double getWidth() {
		return mWidth;
	}
	public void setWidth(double mWidth) {
		this.mWidth = mWidth;
	}
	public double getTurningRadious() {
		return mTurningRadious;
	}
	public void setTurningRadious(double mTurningRadious) {
		this.mTurningRadious = mTurningRadious;
	}
	public double geWheelDiameter() {
		return mWheelDiameter;
	}
	public void setWheelDiameter(double mWheelDiameter) {
		this.mWheelDiameter = mWheelDiameter;
		mWheelPerimeter = mWheelDiameter * Math.PI;
	}
	public double getSteeringAngle() {
		return mSteeringAngle;
	}
	public void setSteeringAngle(double mSteeringAngle) {
		this.mSteeringAngle = mSteeringAngle;
	}
	
	public double getWheelPerimeter(){return mWheelPerimeter;}
	

	
}
