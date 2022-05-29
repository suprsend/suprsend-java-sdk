package suprsend;

import java.io.IOException;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.everit.json.schema.ValidationException;

/**
 * This class makes Dynamic Workflow URL call to SuprSend platform.
 * 
 * @author Suprsend
 */
class TriggerWorkflow {
	private static final Logger logger = Logger.getLogger(TriggerWorkflow.class.getName());

	Suprsend config;
	JSONObject data;
	String url;

	/**
	 * Constructor to initialize necessary data
	 * 
	 * @param config object of class Suprsend
	 * @param data   JSON data to be sent to workflow backend
	 */
	TriggerWorkflow(Suprsend config, JSONObject data) {
		this.config = config;
		this.data = data;
		this.url = getUrl();
	}

	/**
	 * URL for Dynamic Workflow
	 * 
	 * @return Formatted workflow backend URL
	 */
	private String getUrl() {
		String urlTemplate = "%s%s/trigger/";
		if (this.config.includeSignatureParam) {
			if (this.config.authEnabled) {
				urlTemplate = urlTemplate + "?verify=true";
			} else {
				urlTemplate = urlTemplate + "?verify=false";
			}
		}
		String urlFormatted = String.format(urlTemplate, this.config.baseUrl, this.config.workspaceKey);
		return urlFormatted;
	}

	/**
	 * Headers required to trigger workflow request
	 * 
	 * @return Headers as JSON object
	 */
	private JSONObject getHeaders() {
		return new JSONObject()
				.put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent)
				.put("Date", Utils.getCurrentDateTimeFormatted(Constants.HEADER_DATE_FMT));
	}

	private JSONObject getSuperProperties() {
		return new JSONObject()
				.put("$ss_sdk_version", this.config.userAgent);
	}

	/**
	 * This method registers Dynamic workflow request with SuprSend platform.
	 * 
	 * @return Request acceptance status
	 * @throws Exception
	 */
	protected JSONObject executeWorkflow() {
		JSONObject headers = getHeaders();
		JSONObject response = new JSONObject();
		try {
			String contentText;
			if (this.config.authEnabled) {
				// Signature and Authorization Header
				JSONObject sigResult = Signature.getRequestSignature(this.url, "POST", this.data, headers,
						this.config.workspaceSecret);
				contentText = sigResult.getString("contentTxt");
				headers.put("Authorization",
						String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
			} else {
				contentText = this.data.toString();
			}
			// --- Make HTTP POST request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, "POST", this.url, headers,
					contentText);
			int statusCode = resp.statusCode;
			String responseText = resp.responseText;
			//
			if (statusCode >= 200 && statusCode < 300) {
				response.put("success", true)
						.put("status", "success")
						.put("status_code", statusCode)
						.put("message", responseText);
			} else {
				response.put("success", false)
						.put("status", "fail")
						.put("status_code", statusCode)
						.put("message", responseText);
			}
		} catch (SuprsendException | IOException e) {
			response.put("success", false)
					.put("status", "fail")
					.put("status_code", 500)
					.put("message", e.toString());
		}
		return response;
	}

	/**
	 * Validate data against the JSON schema
	 * 
	 * @return Validated data
	 * @throws SuprsendException
	 */
	protected JSONObject validateData() throws SuprsendException, ValidationException {
		this.data = Utils.validateWorkflowSchema(this.data);
		this.data.put("properties", getSuperProperties());
		return this.data;
	}
}
