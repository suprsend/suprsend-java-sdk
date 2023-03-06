package suprsend;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

/**
 * This class makes Dynamic Workflow URL call to SuprSend platform.
 * 
 * @author Suprsend
 */
class WorkflowTrigger {
	private static final Logger logger = Logger.getLogger(WorkflowTrigger.class.getName());

	private Suprsend config;
	private String url;

	/**
	 * Constructor to initialize necessary data
	 * 
	 * @param config object of class Suprsend
	 */
	WorkflowTrigger(Suprsend config) {
		this.config = config;
		this.url = String.format("%s%s/trigger/", this.config.baseUrl, this.config.apiKey);
	}

	/**
	 * Headers required to trigger workflow request
	 * 
	 * @return Headers as JSON object
	 */
	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent)
				.put("Date", Utils.getCurrentDateTimeHeader());
	}

	/**
	 * This method registers Dynamic workflow request with SuprSend platform.
	 * 
	 * @return Request acceptance status
	 * @throws SuprsendException
	 * @throws UnsupportedEncodingException
	 */
	JSONObject trigger(Workflow workflow)
			throws SuprsendException, UnsupportedEncodingException {
		JSONObject o = workflow.getFinalJson(config, false);
		JSONObject validatedBody = o.getJSONObject("event");
		int apparentSize = o.getInt("apparent_size");
		return send(validatedBody);
	}

	JSONObject send(JSONObject workflowBody) {
		JSONObject headers = getHeaders();
		JSONObject response = new JSONObject();
		try {
			String contentText;
			// Signature and Authorization Header
			JSONObject sigResult = Signature.getRequestSignature(this.url, HttpMethod.POST, workflowBody.toString(), headers,
					this.config.apiSecret);
			contentText = sigResult.getString("contentTxt");
			headers.put("Authorization",
					String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			// --- Make HTTP POST request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, this.url, headers,
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
}
