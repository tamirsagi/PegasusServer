package vehicle.interfaces;

/**
 * Interface is used to listen to controller for any incoming sensor data
 * the sensor will get notified for any input.
 * @author Tamir Sagi
 *
 */
public interface onSensorDataRecieved {
	
	void onRecievedSensorData(double value);

}
