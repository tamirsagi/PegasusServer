package PegasusVehicle;

import Control.GeneralParams;
import Control.OnVehicleActions;
import Control.Controller.SteeringDirection;

public class PegasusVehicle extends AbstractVehicle{
	
	
	private static PegasusVehicle mPegasusVehicle;
	private static final String TAG = "Pegasus Vehicle";
	
	private OnVehicleActions mVehicleActionsListener;
	
	private SteeringDirection mSteeringDirection;
	
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
	
	public void registerVehicleActionsListener(OnVehicleActions listener){
		mVehicleActionsListener = listener;
	}



	@Override
	public void drive(int digitalSpeed, char direction, double rotationAngle) {
		drive(digitalSpeed);
		if(direction != ' '){
			if(direction == GeneralParams.KEY_STERRING_RIGHT)
				turnRight(rotationAngle);
			else
				turnLeft(rotationAngle);
		}
		
	}


	@Override
	public void drive(int digitalSpeed) {
		mVehicleActionsListener.drive(digitalSpeed);
	}


	@Override
	public void turnRight(double rotationAngle) {
		mVehicleActionsListener.turnRight(rotationAngle);
		
	}


	@Override
	public void turnLeft(double rotationAngle) {
		mVehicleActionsListener.turnLeft(rotationAngle);
	}


	@Override
	public void driveForward() {
		mVehicleActionsListener.driveForward();
		
	}


	@Override
	public void driveBackward() {
		mVehicleActionsListener.driveBackward();
		
	}


	@Override
	public void stop() {
		mVehicleActionsListener.stop();
		
	}
	
	
	
	
	
	
	

}
