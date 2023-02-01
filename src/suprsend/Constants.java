package suprsend;

public final class Constants {
	private Constants() {
		// restrict instantiation
	}

	// Suprsend Urls
	public static final String DEFAULT_URL = "https://hub.suprsend.com/";
	public static final String DEFAULT_UAT_URL = "https://collector-staging.suprsend.workers.dev/";

	// a API call should not have apparent body size of more than 800KB
	public static final int BODY_MAX_APPARENT_SIZE_IN_BYTES = 800 * 1024;
	public static final String BODY_MAX_APPARENT_SIZE_IN_BYTES_READABLE = "800KB";

	// in general url-size wont exceed 2048 chars or 2048 utf-8 bytes
	public static final int ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES = 2100;

	// few keys added in-flight, amounting to almost 200 bytes increase per
	// workflow-body
	public static final int WORKFLOW_RUNTIME_KEYS_POTENTIAL_SIZE_IN_BYTES = 200;

	// max workflow-records in one bulk api call
	public static final int MAX_WORKFLOWS_IN_BULK_API = 100;
	// Max event-records in one bulk api call
	public static final int MAX_EVENTS_IN_BULK_API = 100;

	public static final boolean ALLOW_ATTACHMENTS_IN_BULK_API = false;
	public static final boolean ATTACHMENT_UPLOAD_ENABLED = false;

	// Single Identity event limit
	public static final int IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES = 2 * 1024;
	public static final String IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE = "2KB";
	public static final int MAX_IDENTITY_EVENTS_IN_BULK_API = 400;

	// In TZ Format: "%a, %d %b %Y %H:%M:%S %Z"
	public static final String HEADER_DATE_FMT = "EEE, d MMM yyyy HH:mm:ss z";

	public static final int CHUNK_APPARENT_SIZE_IN_BYTES = 800 * 1024; // 800 * 1024
	public static final String CHUNK_APPARENT_SIZE_IN_BYTES_READABLE = "800KB";
	public static final int MAX_RECORDS_IN_CHUNK = 100;

	public static final int SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES = 100 * 1024 ;// 100 * 1024
	public static final String SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE = "100KB";
}
