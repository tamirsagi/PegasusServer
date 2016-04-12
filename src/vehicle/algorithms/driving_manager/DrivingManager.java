package vehicle.algorithms.driving_manager;

import vehicle.algorithms.common.AbstractManager;
import logs.logger.PegasusLogger;

public class DrivingManager extends AbstractManager {

	private static DrivingManager mInstance;
	
	
	public static DrivingManager getInstance(){
		if(mInstance == null){
			mInstance = new DrivingManager(DrivingManager.class.getSimpleName());
		}
		return mInstance;
	}
	
	private DrivingManager(String aTag){
		super(aTag);
	}

	@Override
	public void run() {
		PegasusLogger.getInstance().i(getTag(),"Driving Manager has been started...");
		while(mIsworking){
			
			synchronized (this) {
				try{
					while(mIsSuspended){
						wait();
					}
				}catch(InterruptedException e){
					PegasusLogger.getInstance().e(getTag(),e.getMessage());
				}
			}
			
		}
	}
	
	@Override
	public void updateInput(int sensorId, double value){
		
	}
	
	

	

}
