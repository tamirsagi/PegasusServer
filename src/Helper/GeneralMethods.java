package Helper;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class GeneralMethods {
	
	/**
	 * Method receives message from Serial Port and convert it into hashmap
	 * @param msg according to protocol key:value,key:value.. etc
	 * @return JSON object with all entries
	 * @throws JSONException 
	 */
	public static JSONObject convertSerialPortMessageToMap(String msg) throws JSONException{
		JSONObject parsedMsg = new JSONObject();
		String[] key_value_pairs = msg.split("" + GeneralParams.MESSAGE_SAPERATOR);
		for(int i = 0; i < key_value_pairs.length; i ++){
			String[] key_value = msg.split("" + GeneralParams.MESSAGE_KEY_VALUE_SAPERATOR);
			if(key_value.length > 2){ 		//size of 2 means key and value are present
				parsedMsg.put(key_value[0],key_value[1]);
			}
		}
		return parsedMsg;
	}

}
