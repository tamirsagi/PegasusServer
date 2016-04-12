package vehicle.interfaces;

/**
 * The vehicle implements that interface and waits for any sensor to notify its new data
 * @author Tamir Sagi
 *
 */
public interface onInputReceived {
		
	/**
	 * incoming data from sensor
	 * @param value - numeric value
	 */
	public void onReceived(int sensorID,double value);
	

}
