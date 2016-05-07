package vehicle.pegasus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import logs.logger.PegasusLogger;

/**
 * Handles properties file
 * @author Tamir
 *
 */
public class PegasusVehicleProperties extends Properties {
	private static final String TAG = "PegausVehicleProperties";
	
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_VALUE = -1;
	public static String DEFAULT_SENSOR_DISTANCE_VALUE = "60";
	
	
	private static PegasusVehicleProperties mInstance;
	private static final String FILE_NAME = "PegasusVehicleConfig.properties";
	private static final String RESOURCE_LINUX_FORMAT = "/Resources/";
	private static final String RESOURCE_WIN_FORMAT = "\\Resources\\";
	
	private String mRunningOs; 
	private String mFilePath;
	
	
	private InputStream mInputStream;
	private PrintWriter mWriter;
	private boolean mIsLoaded;
	
	public static PegasusVehicleProperties getInstance(){
		if(mInstance == null){
			mInstance = new PegasusVehicleProperties();
		}
		return mInstance;
	}
	
	private PegasusVehicleProperties(){
		mRunningOs = System.getProperty("os.name");
		try {
			if(mRunningOs.contains("Windows")){
				mFilePath = System.getProperty("user.dir") + RESOURCE_WIN_FORMAT + FILE_NAME;
			}else{
				mFilePath = System.getProperty("user.dir") + RESOURCE_LINUX_FORMAT + FILE_NAME;
			}
			mInputStream = new FileInputStream(new File(mFilePath));
			load(mInputStream);
			if(keySet().size() > 0){
				PegasusLogger.getInstance().i(TAG,"PegausVehicleProperties","Configurations file was loaded succefully");
				mIsLoaded = true;
			}else{
				PegasusLogger.getInstance().i(TAG,"PegausVehicleProperties","Configurations file was not loaded");
				mIsLoaded = false;
			}
			
		} catch (IOException e) {
			PegasusLogger.getInstance().e(TAG,"PegausVehicleProperties",e.getMessage());
			mIsLoaded = false;
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
	 * method create JSON object from this class instance
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJsonObject() throws JSONException{
		JSONObject properties = new JSONObject();
		Enumeration<?> e = propertyNames();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String value = getProperty(key);
			properties.put(key, value);
		}
		return properties;
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
	
	public boolean isDataLoaded(){
		return mIsLoaded = keySet().size() > 0;
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
