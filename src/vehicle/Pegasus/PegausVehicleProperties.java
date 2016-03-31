package vehicle.Pegasus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import logs.logger.PegasusLogger;

/**
 * Handles properties file
 * @author Tamir
 *
 */
public class PegausVehicleProperties extends Properties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String TAG = "PegausVehicleProperties";
	public static PegausVehicleProperties mInstance;
	private static final String FILE_NAME = "PegasusVehicleConfig.properties";
	
	private InputStream mInputStream;
	
	public static PegausVehicleProperties getInstance(){
		if(mInstance == null){
			mInstance = new PegausVehicleProperties();
		}
		return mInstance;
	}
	
	private PegausVehicleProperties(){
		mInputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
		try {
			load(mInputStream);
			PegasusLogger.getInstance().i(TAG,"PegausVehicleProperties","Configuration file was loaded succefully");
			
		} catch (IOException e) {
			PegasusLogger.getInstance().e(TAG,"PegausVehicleProperties",e.getMessage());
		}
	}
	
	/**
	 * @param key
	 * @return - value if exist or an empty string otherwise
	 */
	public String getValue(String key,String defaultValue){
		if(contains(key)){
			return getProperty(key);
		}
		return defaultValue;
	}
	
	/**
	 * @param key
	 * @return - value if exist or an empty string otherwise
	 */
	public int getValue(String key,int defaultValue){
		if(contains(key)){
			return Integer.parseInt(getProperty(key));
		}
		return defaultValue;
	}
	
	/**
	 * @param key
	 * @return - value if exist or an empty string otherwise
	 */
	public double getValue(String key,double defaultValue){
		if(contains(key)){
			return Double.parseDouble(getProperty(key));
		}
		return defaultValue;
	}
	
	/**
	 * print all context
	 */
	public void printAllContext(){
		Enumeration<?> e = propertyNames();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String value = getProperty(key);
			PegasusLogger.getInstance().d(TAG,"PegausVehicleProperties", "key : " + key + " Value:" + value);
		}
	}

}
