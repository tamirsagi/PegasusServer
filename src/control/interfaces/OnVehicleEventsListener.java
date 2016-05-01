package control.interfaces;

import org.json.JSONObject;

public interface OnVehicleEventsListener {
	
	/**
	 * updates when state is changed
	 * @param aIsVehicleReady
	 */
	void onVehicleStateChanged(boolean aIsVehicleReady);
	
	/**
	 * send vehicle distance to application
	 * @param aDistance
	 */
	void onSendVehicleRealTimeData(double aSpeed, double aDistance);

}
