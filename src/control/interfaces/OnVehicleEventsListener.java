package control.interfaces;

import org.json.JSONObject;

public interface OnVehicleEventsListener {
	
	/**
	 * updates when state is changed
	 * @param aIsVehicleReady
	 */
	void onVehicleStateChanged(boolean aIsVehicleReady);
	
	/**
	 * send vehicle speed to application
	 * @param aSpeed
	 */
	void onSendVehicleSpeed(double aSpeed);
	
	/**
	 * send vehicle distance to application
	 * @param aDistance
	 */
	void onSendVehicleDistance(double aDistance);

}
