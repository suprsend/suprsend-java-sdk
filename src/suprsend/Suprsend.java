package suprsend;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is the entry point to suprsend-java-sdk. Suprsend Java SDK
 * allows you to programmatically interact with SuprSend Platform.
 * 
 * @author Suprsend
 */
public class Suprsend {
	protected String workspaceKey, workspaceSecret, baseUrl;
	protected String userAgent = String.format(
			"suprsend/%s;java/%s",
			Version.VERSION, System.getProperty("java.version"));
	protected boolean debug = false;
	protected boolean includeSignatureParam = true;
	protected boolean authEnabled = true;
	//
	public SubscriberFactory user;
	private EventCollector eventCollector;

	private void initHelpers() {
		this.eventCollector = new EventCollector(this);
		this.user = new SubscriberFactory(this);
	}

	/**
	 * constructor to initialize SDK with workspace-key and secret
	 * 
	 * @param workspaceKey    workspace_key provided by SuprSend
	 * @param workspaceSecret workspace_secret provided by SuprSend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String workspaceKey, String workspaceSecret) throws SuprsendException {
		this.workspaceKey = workspaceKey;
		this.workspaceSecret = workspaceSecret;
		this.baseUrl = getUrl(null, false);
		//
		cleanup();
		validate();
		initHelpers();
	}

	/**
	 * constructor to initialize SDK with workspace-key and secret.
	 * It also allows the capability of passing custom base URL
	 * 
	 * @param workspaceKey    workspace_key provided by SuprSend
	 * @param workspaceSecret workspace_secret provided by SuprSend
	 * @param baseUrl         custom base-url instead of suprsend platform url
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String workspaceKey, String workspaceSecret, String baseUrl) throws SuprsendException {
		this.workspaceKey = workspaceKey;
		this.workspaceSecret = workspaceSecret;
		this.baseUrl = getUrl(baseUrl, false);
		cleanup();
		validate();
		initHelpers();
	}

	/**
	 * constructor to initialize SDK with workspace-key and secret.
	 * It also allows the capability to print debug logs.
	 * If set to true will print the HTTP request logs sent to Suprsend platform
	 * 
	 * @param workspaceKey    workspace_key provided by SuprSend
	 * @param workspaceSecret workspace_secret provided by SuprSend
	 * @param debug           print logs of http-request to SuprSend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String workspaceKey, String workspaceSecret, boolean debug) throws SuprsendException {
		this.workspaceKey = workspaceKey;
		this.workspaceSecret = workspaceSecret;
		this.baseUrl = getUrl(null, false);
		//
		this.debug = debug;
		//
		cleanup();
		validate();
		initHelpers();
	}

	/**
	 * 
	 * @param workspaceKey    workspace_key provided by SuprSend
	 * @param workspaceSecret workspace_secret provided by SuprSend
	 * @param baseUrl         custom base-url instead of suprsend platform url
	 * @param debug           print logs of http-request to SuprSend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String workspaceKey, String workspaceSecret, String baseUrl, boolean debug)
			throws SuprsendException {

		this.workspaceKey = workspaceKey;
		this.workspaceSecret = workspaceSecret;
		this.baseUrl = getUrl(baseUrl, false);
		//
		this.debug = debug;
		//
		cleanup();
		validate();
		initHelpers();
	}

	/**
	 * 
	 * @param workspaceKey    workspace_key provided by SuprSend
	 * @param workspaceSecret workspace_secret provided by SuprSend
	 * @param baseUrl         custom base-url instead of suprsend platform url
	 * @param debug           print logs of http-request to SuprSend
	 * @param kwargs          extra parameters for SuprSend internal purpose
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String workspaceKey, String workspaceSecret, String baseUrl, boolean debug, JSONObject kwargs)
			throws SuprsendException {

		this.workspaceKey = workspaceKey;
		this.workspaceSecret = workspaceSecret;
		//
		boolean isUAT = kwargs.optBoolean("isUAT");
		this.baseUrl = getUrl(baseUrl, isUAT);
		//
		this.debug = debug;
		//
		cleanup();
		validate();
		initHelpers();
	}

	private void cleanup() {
		if (this.workspaceKey != null) {
			this.workspaceKey = this.workspaceKey.trim();
		}
		if (this.workspaceSecret != null) {
			this.workspaceSecret = this.workspaceSecret.trim();
		}
		if (this.baseUrl != null) {
			this.baseUrl = this.baseUrl.trim();
		}
	}

	/**
	 * Get base url to send requests/events to
	 */
	private String getUrl(String baseUrl, boolean isUAT) {
		if (baseUrl != null && !baseUrl.trim().isEmpty()) {
			// Trim URL to remove any spaces
			baseUrl = baseUrl.trim();
		} else {
			// set default URL in case custom base url not passed
			baseUrl = isUAT == true ? Constants.DEFAULT_UAT_URL : Constants.DEFAULT_URL;
			baseUrl = baseUrl.trim();
		}
		if (!"/".equals(baseUrl.substring(baseUrl.length() - 1))) {
			baseUrl = baseUrl + "/";
		}
		return baseUrl;
	}

	/**
	 * Validate for mandatory parameters
	 * 
	 * @throws SuprsendException Throw custom exception with relevant message.
	 */
	private void validate() throws SuprsendException {
		if (this.workspaceKey == null || this.workspaceKey.isEmpty()) {
			throw new SuprsendException("Missing value for workspaceKey");
		}
		if (this.workspaceSecret == null || this.workspaceSecret.isEmpty()) {
			throw new SuprsendException("Missing value for workspaceSecret");
		}
		if (this.baseUrl == null || this.baseUrl.isEmpty()) {
			throw new SuprsendException("Missing value for baseUrl");
		}
	}

	public JSONObject addAttachment(JSONObject body, String filePath) throws Exception {
		// if data key not present, add it and set value={}.
		if (body.opt("data") == null) {
			body.put("data", new JSONObject());
		}
		JSONObject data = body.getJSONObject("data");
		if (data.optJSONArray("$attachments") == null) {
			data.put("$attachments", new JSONArray());
		}
		JSONArray attachments = data.getJSONArray("$attachments");
		JSONObject attachment = getAttachmentJSONForFile(filePath);
		attachments.put(attachment);
		return body;
	}

	private JSONObject getAttachmentJSONForFile(String filePath) throws Exception {
		// Handle ~ in path
		filePath = filePath.replaceFirst("^~", System.getProperty("user.home"));
		//
		File file = new File(filePath);
		if (!file.exists()) {
			throw new SuprsendException(String.format("file not found: %s", filePath));
		}
		String fileName = file.getName();
		String mimeType = Files.probeContentType(file.toPath());
		//
		byte[] data = Files.readAllBytes(file.toPath());
		String encodedString = Base64.getEncoder().encodeToString(data);
		//
		return new JSONObject()
				.put("fileName", fileName)
				.put("contentType", mimeType)
				.put("data", encodedString);
	}

	/**
	 * Method which needs to be called to trigger workflow
	 * 
	 * @param data Data that needs to be passed
	 * @return Trigger workflow response. 202 if successfully triggered
	 * @throws ValidationException if payload schema validation fails
	 * @throws SuprsendException   SuprsendException
	 */
	public JSONObject triggerWorkflow(JSONObject data) throws SuprsendException, ValidationException {
		TriggerWorkflow workFlow = new TriggerWorkflow(this, data);
		workFlow.validateData();
		return workFlow.executeWorkflow();
	}

	/**
	 * You can track and send events to SuprSend platform by using track method.
	 * 
	 * @param distinctID uniquely Identifiable User id
	 * @param eventName  event name to track
	 * @param properties event properties
	 * @return
	 *         {
	 *         "success": True,
	 *         "status": "success",
	 *         "status_code": resp.status_code,
	 *         "message": resp.text,
	 *         }
	 * @throws SuprsendException SuprsendException
	 */
	public JSONObject track(String distinctID, String eventName, JSONObject properties)
			throws SuprsendException, ValidationException {
		return this.eventCollector.collect(distinctID, eventName, properties);
	}
}
