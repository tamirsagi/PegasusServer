package vehicle.algorithms.driving_manager;

import vehicle.algorithms.common.AbstractManager;
import logs.logger.PegasusLogger;

public class DrivingManager extends AbstractManager {

	private static DrivingManager mInstance;
	private static final String TAG = DrivingManager.class.getSimpleName();
	
	
	public static DrivingManager getInstance(){
		if(mInstance == null){
			mInstance = new DrivingManager();
		}
		return mInstance;
	}
	
	private DrivingManager(){
		setName(TAG);
	}

	@Override
	public void run() {
		PegasusLogger.getInstance().i(TAG,"Driving Manager has been started...");
		while(mIsworking){
			
			synchronized (this) {
				try{
					while(mIsSuspended){
						wait();
					}
				}catch(InterruptedException e){
					PegasusLogger.getInstance().e(TAG,e.getMessage());
				}
			}
			
			
		}
	}
	
	

	

}
