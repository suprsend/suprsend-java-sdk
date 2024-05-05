package suprsend;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class is the entry point to suprsend-java-sdk. Suprsend Java SDK allows
 * you to programmatically interact with SuprSend Platform.
 * 
 * @author Suprsend
 */
public class Suprsend {
	protected String apiKey, apiSecret, baseUrl;
	protected String userAgent = String.format("suprsend/%s;java/%s", Version.VERSION,
			System.getProperty("java.version"));
	protected boolean debug = false;
	//
	public SubscriberFactory user;
	private EventCollector eventCollector;
	private WorkflowTrigger workflowTrigger;
	// bulk instances
	public BulkWorkflowsFactory bulkWorkflows;
	public BulkEventsFactory bulkEvents;
	public BulkSubscribersFactory bulkUsers;
	//
	public TenantsApi tenants;
	public BrandsApi brands;
	public WorkflowsApi workflows;
	//
	public SubscriberListsApi subscriberLists;

	private void initHelpers() {
		this.workflowTrigger = new WorkflowTrigger(this);
		this.eventCollector = new EventCollector(this);
		this.user = new SubscriberFactory(this);
		//
		this.bulkWorkflows = new BulkWorkflowsFactory(this);
		this.bulkEvents = new BulkEventsFactory(this);
		this.bulkUsers = new BulkSubscribersFactory(this);
		//
		this.tenants = new TenantsApi(this);
		this.brands = new BrandsApi(this);
		this.workflows = new WorkflowsApi(this);
		//
		this.subscriberLists = new SubscriberListsApi(this);
	}

	/**
	 * constructor to initialize SDK with workspace-key and secret
	 * 
	 * @param apiKey    api_key provided by SuprSend
	 * @param apiSecret api_secret provided by SuprSend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String apiKey, String apiSecret) throws SuprsendException {
		this(apiKey, apiSecret, null, false, new JSONObject());
	}

	/**
	 * constructor to initialize SDK with workspace-key and secret. It also allows
	 * the capability of passing custom base URL
	 * 
	 * @param apiKey    api_key provided by SuprSend
	 * @param apiSecret api_secret provided by SuprSend
	 * @param baseUrl   custom base-url instead of suprsend platform url
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String apiKey, String apiSecret, String baseUrl) throws SuprsendException {
		this(apiKey, apiSecret, baseUrl, false, new JSONObject());
	}

	/**
	 * constructor to initialize SDK with workspace-key and secret. It also allows
	 * the capability to print debug logs. If set to true will print the HTTP
	 * request logs sent to Suprsend platform
	 * 
	 * @param apiKey    api_key provided by SuprSend
	 * @param apiSecret api_secret provided by SuprSend
	 * @param debug     print logs of http-request to SuprSend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String apiKey, String apiSecret, boolean debug) throws SuprsendException {
		this(apiKey, apiSecret, null, debug, new JSONObject());
	}

	/**
	 * 
	 * @param apiKey    api_key provided by SuprSend
	 * @param apiSecret api_secret provided by SuprSend
	 * @param baseUrl   custom base-url instead of suprsend platform url
	 * @param debug     print logs of http-request to SuprSend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String apiKey, String apiSecret, String baseUrl, boolean debug) throws SuprsendException {
		this(apiKey, apiSecret, baseUrl, debug, new JSONObject());
	}

	/**
	 * 
	 * @param apiKey    api_key provided by SuprSend
	 * @param apiSecret api_secret provided by SuprSend
	 * @param baseUrl   custom base-url instead of suprsend platform url
	 * @param debug     print logs of http-request to SuprSend
	 * @param kwargs    extra parameters for SuprSend internal purpose
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String apiKey, String apiSecret, String baseUrl, boolean debug, JSONObject kwargs)
			throws SuprsendException {

		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		//
		this.baseUrl = getUrl(baseUrl);
		//
		this.debug = debug;
		//
		cleanup();
		validate();
		initHelpers();
	}

	private void cleanup() {
		if (this.apiKey != null) {
			this.apiKey = this.apiKey.trim();
		}
		if (this.apiSecret != null) {
			this.apiSecret = this.apiSecret.trim();
		}
		if (this.baseUrl != null) {
			this.baseUrl = this.baseUrl.trim();
		}
	}

	/**
	 * Get base url to send requests/events to
	 */
	private String getUrl(String baseUrl) {
		if (baseUrl != null && !baseUrl.trim().isEmpty()) {
			// Trim URL to remove any spaces
			baseUrl = baseUrl.trim();
		} else {
			// set default URL in case custom base url not passed
			baseUrl = Constants.DEFAULT_URL;
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
		if (this.apiKey == null || this.apiKey.isEmpty()) {
			throw new SuprsendException("Missing value for apiKey");
		}
		if (this.apiSecret == null || this.apiSecret.isEmpty()) {
			throw new SuprsendException("Missing value for apiSecret");
		}
		if (this.baseUrl == null || this.baseUrl.isEmpty()) {
			throw new SuprsendException("Missing value for baseUrl");
		}
	}

	/**
	 * @deprecated use WorkflowTriggerRequest.addAttachment() instead
	 * 
	 * @param body     workflow body
	 * @param filePath attachment file path
	 * @return modified workflow body after adding attachment
	 * @throws InputValueException SuprsendException
	 */
	@Deprecated
	public JSONObject addAttachment(JSONObject body, String filePath) throws InputValueException {
		// if data key not present, add it and set value={}.
		if (body.opt("data") == null) {
			body.put("data", new JSONObject());
		}
		//
		JSONObject attachment = Attachment.getAttachmentJSON(filePath);
		if (attachment != null) {
			// add the attachment to body->data->$attachments
			JSONObject data = body.getJSONObject("data");
			if (data.optJSONArray("$attachments") == null) {
				data.put("$attachments", new JSONArray());
			}
			JSONArray attachments = data.getJSONArray("$attachments");
			attachments.put(attachment);
		}
		return body;
	}

	/**
	 * @deprecated Method which needs to be called to trigger workflow
	 * 
	 * @param data Data that needs to be passed
	 * @return Trigger workflow response. 202 if successfully triggered
	 * @throws SuprsendException            SuprsendException
	 * @throws UnsupportedEncodingException if utf-8 encoding not supported
	 */
	@Deprecated
	public JSONObject triggerWorkflow(JSONObject data) throws SuprsendException, UnsupportedEncodingException {
		Workflow wfIns = new Workflow(data, null, null);
		return this.workflowTrigger.trigger(wfIns);
	}

	/**
	 * Method which needs to be called to trigger workflow
	 * 
	 * @param wf instance of Workflow class
	 * @return Trigger workflow response. 202 if successfully triggered
	 * @throws SuprsendException            SuprsendException
	 * @throws UnsupportedEncodingException if utf-8 encoding not supported
	 */
	public JSONObject triggerWorkflow(Workflow wf) throws SuprsendException, UnsupportedEncodingException {
		return this.workflowTrigger.trigger(wf);
	}

	/**
	 * @deprecated You can track and send events to SuprSend platform by using track
	 *             method. Use trackEvent() instead.
	 * 
	 * @param distinctId uniquely Identifiable User id
	 * @param eventName  event name to track
	 * @param properties event properties
	 * @return { "success": True, "status": "success", "status_code":
	 *         resp.status_code, "message": resp.text, }
	 * @throws SuprsendException            SuprsendException
	 * @throws UnsupportedEncodingException if utf-8 encoding not supported
	 */
	@Deprecated
	public JSONObject track(String distinctId, String eventName, JSONObject properties)
			throws SuprsendException, UnsupportedEncodingException {
		Event event = new Event(distinctId, eventName, properties, null, null);
		return this.eventCollector.collect(event);
	}

	/**
	 * You can track and send events to SuprSend platform by using trackEvent
	 * method.
	 * 
	 * @param event instance of Event class
	 * @return { "success": True, "status": "success", "status_code":
	 *         resp.status_code, "message": resp.text, }
	 * @throws SuprsendException            SuprsendException
	 * @throws UnsupportedEncodingException if utf-8 encoding not supported
	 */
	public JSONObject trackEvent(Event event) throws SuprsendException, UnsupportedEncodingException {
		return this.eventCollector.collect(event);
	}

}
