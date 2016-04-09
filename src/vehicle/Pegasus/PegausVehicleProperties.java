package vehicle.Pegasus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import logs.logger.PegasusLogger;

/**
 * Handles properties file
 * @author Tamir
 *
 */
public class PegausVehicleProperties extends Properties {
	
	private static final long serialVersionUID = 1L;
	public static String DEFAULT_VALUE_ZERO = "0";
	
	private static final String TAG = "PegausVehicleProperties";
	public static PegausVehicleProperties mInstance;
	private static final String FILE_NAME = "PegasusVehicleConfig.properties";
	
	
	private InputStream mInputStream;
	private PrintWriter mWriter;
	
	public static PegausVehicleProperties getInstance(){
		if(mInstance == null){
			mInstance = new PegausVehicleProperties();
		}
		return mInstance;
	}
	
	private PegausVehicleProperties(){
		try {
		mInputStream = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
		mWriter = new PrintWriter(new File(getClass().getClassLoader().getResource(FILE_NAME).getPath()));
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
	public String getValue(String key){
			return getValue(key,"");
	}
	
	/**
	 * @param key
	 * @return - value if exist or an default value otherwise
	 */
	public String getValue(String key,String defaultValue){
			return getProperty(key,defaultValue);
	}
	
	/**
	 * save values to resource for later usage
	 * @param key
	 * @param value
	 */
	public void save(String key, String value){
		String val = getValue(key);
		if(val != null && !val.isEmpty()){
			remove(key);
		}
		setProperty(key, value);
	}
	
	
	/**
	 * print all context
	 */
	private void printAllContext(){
		Enumeration<?> e = propertyNames();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String value = getProperty(key);
			PegasusLogger.getInstance().d(TAG,"PegausVehicleProperties", "key : " + key + " Value:" + value);
		}
	}
	
	private void loadAllContext(){
		Enumeration<?> e = propertyNames();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String value = getProperty(key);
			put(key,value);
		}
	}

}
