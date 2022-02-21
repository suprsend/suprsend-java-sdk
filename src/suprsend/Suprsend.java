package suprsend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

/**
 * This class is the entry point to suprsend-java-sdk. It Suprsend Java SDK
 * allows you to trigger workflow which have been configured in suprsend system
 * 
 * @author Suprsend
 */
public class Suprsend {
	String envKey, envSecret, baseUrl;
	String sdkVersion = Constants.version;
	String userAgent = String.format("suprsend/%s;java/%s", this.sdkVersion, System.getProperty("java.version"));
	Boolean includeSignatureParam = true;
	Boolean authEnabled = true;
	Boolean isUAT = false;
	Boolean debug = false;
	final String defaultUrl = "https://hub.suprsend.com/";
	final String defaultUatUrl = "https://collector-staging.suprsend.workers.dev/";
	private BufferedReader reader;

	/**
	 * This constructor will help you initialize the Suprsend SDK with env key and
	 * secret
	 * 
	 * @param envKey    Environment key provided by Suprsend
	 * @param envSecret Environment secret key provided by Suprsend
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String envKey, String envSecret) throws SuprsendException {
		this.envKey = envKey;
		this.envSecret = envSecret;
		this.baseUrl = getUrl(null, this.isUAT);
		validate();
	}

	/**
	 * This constructor will help you initialize the Suprsend SDK with env key and
	 * secret. It also allows the capability of passing custom base URL
	 * 
	 * @param envKey    Environment key provided by Suprsend
	 * @param envSecret Environment secret key provided by Suprsend
	 * @param baseUrl   Custom base URL
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String envKey, String envSecret, String baseUrl) throws SuprsendException {
		this.envKey = envKey;
		this.envSecret = envSecret;
		this.baseUrl = getUrl(baseUrl, this.isUAT);
		validate();
	}

	/**
	 * This constructor will help you initialize the Suprsend SDK with env key and
	 * secret. It also allows the capability of passing custom base URL
	 * 
	 * @param envKey    Environment key provided by Suprsend
	 * @param envSecret Environment secret key provided by Suprsend
	 * @param debug     Custom base URL
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String envKey, String envSecret, Boolean debug) throws SuprsendException {
		this.envKey = envKey;
		this.envSecret = envSecret;
		this.baseUrl = getUrl(baseUrl, this.isUAT);
		this.debug = debug;
		if (this.debug == true) {
			new RequestLogs();
		}
		validate();
	}
	
	/**
	 * This constructor will help you initialize the Suprsend SDK with certain
	 * inputs
	 * 
	 * @param envKey    Environment key provided by Suprsend
	 * @param envSecret Environment secret key provided by Suprsend
	 * @param kwargs    Extra arguments to be passed which include any of the
	 *                  follows: authEnabled, includeSignatureParam, isUAT
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String envKey, String envSecret, JSONObject kwargs)
			throws SuprsendException {

		this.envKey = envKey;
		this.envSecret = envSecret;
		if (kwargs.has("isUAT") && (Boolean) kwargs.get("isUAT") == true) {
			this.isUAT = kwargs.getBoolean("isUAT");
		}
		this.baseUrl = getUrl(null, this.isUAT);
		this.authEnabled = ((kwargs.has("authEnabled") == false || kwargs.get("authEnabled") == null) ? false
				: (Boolean) kwargs.get("authEnabled"));
		this.includeSignatureParam = ((kwargs.has("includeSignatureParam") == false
				|| kwargs.get("includeSignatureParam") == null) ? false
						: (Boolean) kwargs.get("includeSignatureParam"));
		validate();
	}

	/**
	 * This constructor will help you initialize the Suprsend SDK with certain
	 * inputs
	 * 
	 * @param envKey    Environment key provided by Suprsend
	 * @param envSecret Environment secret key provided by Suprsend
	 * @param baseUrl   Base URl to Suprsend workflow system. Pass null to use
	 *                  default URL
	 * @param debug     If set to true will print the HTTP logs for the requests
	 *                  that will be sent to Suprsend backend
	 * @param kwargs    Extra arguments to be passed which include any of the
	 *                  follows: authEnabled, includeSignatureParam, isUAT
	 * @throws SuprsendException Custom exception thrown by SDK
	 */
	public Suprsend(String envKey, String envSecret, String baseUrl, Boolean debug, JSONObject kwargs)
			throws SuprsendException {

		this.envKey = envKey;
		this.envSecret = envSecret;
		if (kwargs.has("isUAT") && (Boolean) kwargs.get("isUAT") == true) {
			this.isUAT = kwargs.getBoolean("isUAT");
		}
		this.baseUrl = getUrl(baseUrl, this.isUAT);
		this.authEnabled = ((kwargs.has("authEnabled") == false || kwargs.get("authEnabled") == null) ? false
				: (Boolean) kwargs.get("authEnabled"));
		this.includeSignatureParam = ((kwargs.has("includeSignatureParam") == false
				|| kwargs.get("includeSignatureParam") == null) ? false
						: (Boolean) kwargs.get("includeSignatureParam"));
		this.debug = debug;
		if (this.debug == true) {
			new RequestLogs();
		}
		validate();
	}

	/**
	 * Get Suprsend backend URL
	 * 
	 * @param baseUrl Base URL to be used if passed by client
	 * @param kwargs  Extra parameter which contains isUAT. This parameter defines
	 *                the environment to be used
	 * @return Base URL which will be used to trigger workflow
	 */
	private String getUrl(String baseUrl, Boolean isUAT) {
		// Trim URL to remove any spaces
		if (baseUrl != null) {
			baseUrl = baseUrl.trim();
		} else {
			// If base URL is null then set default URL
			if (isUAT == true) {
				baseUrl = defaultUatUrl;
			} else {
				baseUrl = defaultUrl;
			}
			baseUrl = baseUrl.trim();
		}
		if (baseUrl.substring(baseUrl.length() - 1).equals("/") == false) {
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
		if (this.envKey == null) {
			throw new SuprsendException("Missing value for envKey");
		}
		if (this.envSecret == null) {
			throw new SuprsendException("Missing value for envSecret");
		}
		if (this.baseUrl == null) {
			throw new SuprsendException("Missing value for baseUrl");
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject addAttachment(JSONObject body, String filePath) throws Exception {
		// if data key not present, add it and set value={}.
		Boolean isPresent = body.has("data");
		if (isPresent == false || body.get("data") == null) {
			body.put("data", new JSONObject());
		}
		JSONObject attachment = getAttachmentJSONForFile(filePath);
		JSONObject data = (JSONObject) body.get("data");
		if (data.has("$attachments") == false || data.get("$attachments") == null) {
			data.put("$attachments", new ArrayList<>());
		}
		ArrayList<JSONObject> attachments = (ArrayList<JSONObject>) data.get("$attachments");
		attachments.add(attachment);
		return body;
	}

	private JSONObject getAttachmentJSONForFile(String filePath) throws Exception {
		JSONObject response = new JSONObject();
		String st;
		String fileName;
		File file = new File(filePath);
		reader = new BufferedReader(new FileReader(file));
		while ((st = reader.readLine()) != null) {
			fileName = file.getName();
			String mimeType = Files.probeContentType(file.toPath());
			// Base encode string
			String encodedString = Base64.getEncoder().encodeToString(st.getBytes());
			response.put("fileName", fileName);
			response.put("contentType", mimeType);
			response.put("data", encodedString);
		}

		return response;
	}

	/**
	 * Method which needs to be called to trigger workflow
	 * 
	 * @param data Data that needs to be passed
	 * @return Trigger workflow response. 202 if successfully triggered
	 * @throws ValidationException
	 * @throws IOException
	 * @throws SuprsendException
	 * @throws Exception
	 */
	public JSONObject triggerWorkflow(JSONObject data)
			throws ValidationException, IOException, SuprsendException, Exception {
		TriggerWorkflow workFlow = new TriggerWorkflow(this, data);
		workFlow.validateData();
		return workFlow.executeWorkflow();
	}
}
