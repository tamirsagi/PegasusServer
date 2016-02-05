package PegasusVehicle;

import Control.Interfaces.IVehicleActionsListener;
import Helper.GeneralParams.*;

public class PegasusVehicle extends AbstractVehicle{
	
	private static final int MIN_DIGITAL_SPEED = 0;
	private static final int MAX_DIGITAL_SPEED = 255;
	private static final int STRAIGHT_STEER_ANGLE = 90;
	private static final int MIN_STEER_ANGLE = 50;
	private static final int MAX_STEER_ANGLE = 130;
	
	private static PegasusVehicle mPegasusVehicle;
	private static final String TAG = "Pegasus Vehicle";
	
	private IVehicleActionsListener mVehicleActionsListener;
	
	private SteeringDirection mSteeringDirection;
	private int mDigitalSpeed;
	private double mSteeringAngle;
	
	/**
	 * Get class instance
	 * @return
	 */
	public static PegasusVehicle getInstance(){
		if(mPegasusVehicle == null)
			mPegasusVehicle = new PegasusVehicle();
		
		return mPegasusVehicle;
	}
	
	private PegasusVehicle(){
		mSteeringDirection = SteeringDirection.FORDWARD;  // by default
		
	}
	
	public void registerVehicleActionsListener(IVehicleActionsListener listener){
		mVehicleActionsListener = listener;
	}


	@Override
	public void changeSpeed(int digitalSpeed) {
		mDigitalSpeed = digitalSpeed;
		mVehicleActionsListener.changeSpeed(digitalSpeed);
	}


	@Override
	public void turnRight(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE - rotationAngle; //from 0-40 to 50 - 90
		mVehicleActionsListener.turnRight(rotationAngle);
		
	}


	@Override
	public void turnLeft(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE + rotationAngle;		//from 0-40 to 90 - 130
		mVehicleActionsListener.turnLeft(rotationAngle);
	}


	@Override
	public void driveForward() {
		mSteeringDirection = SteeringDirection.FORDWARD;
		mVehicleActionsListener.driveForward();
		
	}


	@Override
	public void driveBackward() {
		mSteeringDirection = SteeringDirection.BACKWARD;
		mVehicleActionsListener.driveBackward();
		
	}


	@Override
	public void stop() {
		mDigitalSpeed = 0;
		mVehicleActionsListener.stop();
		
	}
	
	
	
	
	
	
	

}
