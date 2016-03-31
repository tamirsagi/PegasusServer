package vehicle.Interfaces;

public interface onInputReceived {
		
	/**
	 * incoming data from sensor
	 * @param value - numeric value
	 */
	public void onReceived(int sensorID,double value);
	

}
