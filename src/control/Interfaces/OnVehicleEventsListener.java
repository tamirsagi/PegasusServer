package control.Interfaces;

public interface OnVehicleEventsListener {
	
	/**
	 * updates when state is changed
	 * @param aIsVehicleReady
	 */
	void onVehicleStateChanged(boolean aIsVehicleReady);

}
