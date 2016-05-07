package vehicle.common;

import org.json.JSONObject;

import vehicle.common.constants.VehicleConfigKeys;
import vehicle.pegasus.PegasusVehicleData;
import vehicle.pegasus.PegasusVehicleProperties;

public abstract class VehicleData {
	
	public static final int MIN_REQUIRED_DISTANCE_SAFE_FACTOR = 2;
	protected static final double DEGREE_RADIANS_FACTOR = Math.PI / 180;
	private double mLength;
	private double mWidth;
	protected double mFrontWheelTurningRadious; //big radius which is created from the fron turned wheel
	protected double mBackWheelTurningRadious; //small radius which is created from the back wheel
	private double mWheelDiameter;
	private double mWheelPerimeter;
	private double mSteeringAngle;
	private double mWheelBase;
	private double mFrontWheelDistance;
	protected double mMinimumRequiredSpace;
	private double mDistCentreFrontWheelToFrontCar;
	
	
	protected VehicleData(JSONObject aProperties){
		double length = aProperties.optDouble(VehicleConfigKeys.KEY_LENGTH,PegasusVehicleProperties.DEFAULT_VALUE);
		double width = aProperties.optDouble(VehicleConfigKeys.KEY_WIDTH,PegasusVehicleProperties.DEFAULT_VALUE);
		double steeringAngle = aProperties.optDouble(VehicleConfigKeys.KEY_MAX_STEERING_ANGLE_FACTOR,PegasusVehicleProperties.DEFAULT_VALUE);
		double wheelDiameter = aProperties.optDouble(VehicleConfigKeys.KEY_WHEEL_DIAMETER,PegasusVehicleProperties.DEFAULT_VALUE);
		double wheelBase = aProperties.optDouble(VehicleConfigKeys.KEY_WHEEL_BASE,PegasusVehicleProperties.DEFAULT_VALUE);
		double centreFrontWheelToFrontCar = aProperties.optDouble(VehicleConfigKeys.KEY_FRONT_WHEEL_FRONT_CAR,PegasusVehicleProperties.DEFAULT_VALUE);
		double wheelDistance = aProperties.optDouble(VehicleConfigKeys.KEY_FRONT_WHEEL_DISTANCE,PegasusVehicleProperties.DEFAULT_VALUE);
		
		setLength(length);
		setWidth(width);
		setWheelDiameter(wheelDiameter);
		setWheelBase(wheelBase);
		setSteeringAngle(steeringAngle);
		setDistanceCenterFrontWheelToFrontCar(centreFrontWheelToFrontCar);
		setMinimumRequiredSpaceToPark();
		setFrontWheelDistance(wheelDistance);
	}
	
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
	
	public double getBackWheelTurningRadius(){
		return mBackWheelTurningRadious;
	}
	
	public double getFrontWheelTurningRadious() {
		return mFrontWheelTurningRadious;
	}
	
	public abstract void setTurningRadius();
	
	
	public double getWheelDiameter() {
		return mWheelDiameter;
	}
	
	public void setWheelDiameter(double mWheelDiameter) {
		this.mWheelDiameter = mWheelDiameter;
		mWheelPerimeter = mWheelDiameter * Math.PI;
	}
	public double getSteeringAngle() {
		return mSteeringAngle;
	}
	
	/**
	 * save the angle in degree
	 * @param mSteeringAngle
	 */
	public void setSteeringAngle(double mSteeringAngle) {
		this.mSteeringAngle = mSteeringAngle;
		setTurningRadius();
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
	
	public double getFrontWheelDistance(){return mFrontWheelDistance;}
	
	public void setFrontWheelDistance(double aFrontWheelDistance){
		mFrontWheelDistance = aFrontWheelDistance;
	}
	
	/**
	 * calculate the minimum distance shall be added to vehicle length in order to park in parallel
	 * 
	 */
	public abstract void setMinimumRequiredSpaceToPark();

	@Override
	public String toString() {
		return String.format("[Length:%s\nWidth:%s\nWheel Base:%s\nWheel Diameter:%s\n" +
				"WheelPerimeter:%s\nFront Wheel Distance:%s\nDistance Center Front Wheel To Front Car:%s\n" +
				"Steering Angle:%s,Front Wheel Turning Radius:%s\nBack Wheel Turning Radius:%s\n" +
				"Minimum Space Required To Parallel:%s]",getLength(),getWidth(),getWheelBase(),getWheelDiameter(),
				getWheelPerimeter(),getFrontWheelDistance(),getDistanceCentreFrontWheelToFrontCar(),
				getSteeringAngle(),getFrontWheelTurningRadious(),getBackWheelTurningRadius(),getMinimumRequiredSpaceToPark());
	}
	
	
	
}
