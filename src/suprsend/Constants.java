package suprsend;

public final class Constants {
	private Constants(){
		// restrict instantiation
	}

	// Suprsend Urls
	public static final String DEFAULT_URL = "https://hub.suprsend.com/";
	public static final String DEFAULT_UAT_URL = "https://collector-staging.suprsend.workers.dev/";
	
	// a API call should not have apparent body size of more than 200KB
	public static final int BODY_MAX_APPARENT_SIZE_IN_BYTES = 200 * 1024;
	public static final String BODY_MAX_APPARENT_SIZE_IN_BYTES_READABLE = "200KB";

	// in general url-size wont exceed 2048 chars or 2048 utf-8 bytes
	public static final int ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES = 2100;

	// few keys added in-flight, amounting to almost 200 bytes increase per workflow-body
	public static final int RUNTIME_KEYS_POTENTIAL_SIZE_IN_BYTES = 200;

	// max workflow-records in one batch api call.
	public static final int MAX_RECORDS_IN_BATCH = 10;

	public static final boolean ALLOW_ATTACHMENTS_IN_BATCH = false;

	// In TZ Format: "%a, %d %b %Y %H:%M:%S %Z"
	public static final String HEADER_DATE_FMT = "EEE, d MMM yyyy HH:mm:ss z";
	
}
