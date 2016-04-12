package vehicle.common;

public abstract class VehicleData {
	
	private double mLength;
	private double mWidth;
	protected double mTurningRadious;
	private double mWheelDiameter;
	private double mWheelPerimeter;
	private double mSteeringAngle;
	private double mWheelBase;
	protected double mMinimumRequiredSpace;
	private double mDistCentreFrontWheelToFrontCar;
	
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
	
	public abstract void setTurningRadious() ;
	
	
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
		setTurningRadious();
	}
	
	public double getWheelBase(){
		return mWheelBase;
	}
	
	public void setWheelBase(double aValue){
		mWheelBase = aValue;
	}
	
	public void setDistanceCenterFrontWheelToFrontCar(double aValue){
		mDistCentreFrontWheelToFrontCar = aValue;
	}
	
	public double getDistanceCentreFrontWheelToFrontCar(){
		return mDistCentreFrontWheelToFrontCar;
	}
	
	public double getWheelPerimeter(){
		return mWheelPerimeter;
	}
	
	public double getMinimumRequiredSpaceToPark(){
		return mMinimumRequiredSpace;
		}
	
	/**
	 * calculate the minimum distance shall be added to vehicle length in order to park in parallel
	 * 
	 */
	public abstract void setMinimumRequiredSpaceToPark();
	
}
