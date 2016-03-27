package communication.messages;

/**
 * This class represent message object 
 * 
 * @author Tamir
 *
 */
public class Message {
	
	private String mSender;
	private String mTo;
	private String mWhat;
	
	
	
	public String getFrom(){
		return mSender;
	}
	
	
	public void setSender(String sender){
		mSender = sender;
	}
	
	public String getToWhom(){
		return mTo;
	}
	
	public void setTo(String to){
		mTo = to;
	}
	
	public String getMessage(){
		return mWhat;
	}
	
	public void setMessage(String msg){
		mWhat = msg;
	}


	@Override
	public String toString() {
		return "Message [Sender=" + mSender + ", To=" + mTo + ", What="
				+ mWhat + "]";
	}
	
	

}
