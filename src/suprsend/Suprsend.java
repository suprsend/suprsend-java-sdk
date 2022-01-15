package suprsend;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

import javax.xml.bind.ValidationException;

import org.json.JSONObject;

import suprsend.Constants;

public class Suprsend {
	String envKey;
	String envSecret;
	String sdkVersion;
	String userAgent;
	String baseUrl;
	Boolean authEnabled;
	Boolean includeSignatureParam;
	final String defaultUrl = "https://hub.suprsend.com/";
	final String defaultUatUrl = "https://collector-staging.suprsend.workers.dev/";
	private BufferedReader reader;
	
	public Suprsend(String envKey, String envSecret, String baseUrl, Boolean debug, JSONObject kwargs) throws ValidationException {
		this.envKey = envKey;
		this.envSecret = envSecret;
		this.sdkVersion = Constants.version;
		this.baseUrl = getUrl(baseUrl, kwargs);
		this.authEnabled = ((kwargs.get("authEnabled") == null) ? false : (Boolean)kwargs.get("authEnabled"));
		this.includeSignatureParam = ((kwargs.get("includeSignatureParam") == null) ? false : (Boolean)kwargs.get("includeSignatureParam"));
		this.userAgent = String.format("suprsend/%s;java/%s", this.sdkVersion, System.getProperty("java.version"));
		if (debug == true) {
			new RequestLogs();
		}
		validate();
	}
	
	private String getUrl(String baseUrl, JSONObject kwargs) {
		// Trim URL to remove any spaces
		if (baseUrl != null) {
			baseUrl = baseUrl.trim();
		}
		else {
			// If base URL is null then set default URL
			if ((Boolean) kwargs.get("isUAT") == true) {
				baseUrl = defaultUatUrl;
			}
			else {
				baseUrl = defaultUrl;
			}
			baseUrl = baseUrl.trim();
		}
		if (baseUrl.substring(baseUrl.length() - 1).equals("/") == false) {
			baseUrl = baseUrl + "/";
		}
		return baseUrl;
	}
	
	private void validate() throws ValidationException {
		if (this.envKey == null) {
			throw new ValidationException("Missing value for envKey");
		}
		if (this.envSecret == null) {
			throw new ValidationException("Missing value for envSecret");
		}
		if (this.baseUrl == null) {
			throw new ValidationException("Missing value for baseUrl");
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject addAttachment(JSONObject body, String filePath) throws Exception {
		// if data key not present, add it and set value={}.
		Boolean isPresent = body.has("data");
		if (isPresent == false || body.get("data") == null)  {
			body.put("data", new JSONObject());
		}
		JSONObject attachment = getAttachmentJSONForFile(filePath);
		JSONObject data = (JSONObject)body.get("data");
		if (data.get("$attachments") == null) {
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
	
	public JSONObject triggerWorkflow(JSONObject data) throws org.everit.json.schema.ValidationException, IOException, ValidationException, Exception {
		TriggerWorkflow workFlow = new TriggerWorkflow(this, data);
		workFlow.validateData();
		return workFlow.executeWorkflow();
	}
}
