package suprsend;

public class Constants {
	public static String version = "0.2.0";
	
	public static int MAX_RECORDS_IN_BATCH = 10;
	public static int RUNTIME_KEYS_POTENTIAL_SIZE_IN_BYTES = 200;
	public static int BODY_MAX_APPARENT_SIZE_IN_BYTES = 200 * 1024;
	public static int ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES = 2100;
	
	public static String HEADER_DATE_FMT = "EEE, d MMM yyyy HH:mm:ss z";
	public static String BODY_MAX_APPARENT_SIZE_IN_BYTES_READABLE = "200KB";
	
	public static boolean ALLOW_ATTACHMENTS_IN_BATCH = false;
	
}
