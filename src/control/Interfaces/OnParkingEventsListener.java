package control.Interfaces;

public interface OnParkingEventsListener {
	
	/**
	 * fire an event that a parking spot has been found
	 */
	void onParkingFound();
	
	/**
	 * fire an event that parking not found within timeout
	 */
	void onParkingNoFound();

}
