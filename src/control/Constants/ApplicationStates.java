package control.Constants;

public class ApplicationStates {

	public static final int BOOTING = -1;
	public static final int INITIALIZE_SERIAL_PORT = 0;
	public static final int WAITING_FOR_HARDWARE = 1;
	public static final int HARDWARE_READY = 2;
	public static final int WAITING_FOR_SERVER = 3;
	public static final int SERVER_READY = 4;
	public static final int READY = 5;

	public static String getStateName(int code) {
		switch (code) {
		case BOOTING:
			return "BOOTING";
		case INITIALIZE_SERIAL_PORT:
			return "INITIALIZE_SERIAL_PORT";
		case WAITING_FOR_HARDWARE:
			return "WAITING_FOR_HARDWARE";
		case HARDWARE_READY:
			return "HARDWARE_READY";
		case WAITING_FOR_SERVER:
			return "WAITING_FOR_SERVER";
		case SERVER_READY:
			return "SERVER_READY";
		case READY:
			return "READY";
		default:
			return "UNKNOWN_STATE";

		}

	}

}
