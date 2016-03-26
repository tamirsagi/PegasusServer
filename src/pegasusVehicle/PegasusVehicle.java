package pegasusVehicle;

import pegasusVehicle.params.VehicleParams;
import control.Interfaces.IVehicleActionsListener;

public class PegasusVehicle extends AbstractVehicle {
	private static final String TAG = "Pegasus Vehicle";

	private static final int MIN_DIGITAL_SPEED = 0;
	private static final int MAX_DIGITAL_SPEED = 255;
	private static final int STRAIGHT_STEER_ANGLE = 90;
	private static final int MIN_STEER_ANGLE = 50;
	private static final int MAX_STEER_ANGLE = 130;

	private IVehicleActionsListener mVehicleActionsListener;

	private VehicleParams.DrivingDirection mSteeringDirection;
	private int mDigitalSpeed;
	private double mSteeringAngle;

	/**
	 * Get class instance
	 * 
	 * @return
	 */
	public static AbstractVehicle getInstance() {
		if (mInstance == null)
			mInstance = new PegasusVehicle();

		return mInstance;
	}

	private PegasusVehicle() {
		mSteeringDirection = VehicleParams.DrivingDirection.FORWARD; // by
																		// default
		setUltraSonicSensors();

	}

	@Override
	public void registerVehicleActionsListener(IVehicleActionsListener listener) {
		mVehicleActionsListener = listener;
	}
	
	@Override
	public void setUltraSonicSensors() {
		mUltraSonicSensors = new Sensor[mNumberOfUltraSonicSensors];
		for (int i = 0; i < mNumberOfUltraSonicSensors; i++) {
			mUltraSonicSensors[i] = new Sensor(i + 1);
		}
	}

	@Override
	public void changeSpeed(int digitalSpeed) {
		mDigitalSpeed = digitalSpeed;
		mVehicleActionsListener.changeSpeed(digitalSpeed);
	}

	@Override
	public void turnRight(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE - rotationAngle; // from 0-40 to
																// 50 - 90
		mVehicleActionsListener.turnRight(rotationAngle);

	}

	@Override
	public void turnLeft(double rotationAngle) {
		mSteeringAngle = STRAIGHT_STEER_ANGLE + rotationAngle; // from 0-40 to
																// 90 - 130
		mVehicleActionsListener.turnLeft(rotationAngle);
	}

	@Override
	public void driveForward() {
		mSteeringDirection = VehicleParams.DrivingDirection.FORWARD;
		mVehicleActionsListener.driveForward();

	}

	@Override
	public void driveBackward() {
		mSteeringDirection = VehicleParams.DrivingDirection.REVERSE;
		mVehicleActionsListener.driveBackward();

	}

	@Override
	public void stop() {
		mDigitalSpeed = 0;
		mVehicleActionsListener.stop();

	}

	

}
