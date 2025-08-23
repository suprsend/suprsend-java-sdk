package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.json.JSONObject;

class EventCollector {
	private static final Logger logger = Logger.getLogger(EventCollector.class.getName());

	private Suprsend config;
	private String url;

	EventCollector(Suprsend config) {
		this.config = config;
		this.url = String.format("%sv2/event/", this.config.baseUrl);
	}

	/**
	 * Headers required to trigger workflow request
	 * 
	 * @return Headers as JSON object
	 */
	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	JSONObject collect(Event event) throws InputValueException, SuprsendException, UnsupportedEncodingException {
		JSONObject finalJson = event.getFinalJson(config, false);
		JSONObject eventDict = finalJson.getJSONObject("event");
		// int eventSize = finalJson.getInt("apparent_size");
		return send(eventDict);
	}

	private JSONObject send(JSONObject event) {
		JSONObject headers = getHeaders();
		JSONObject response = new JSONObject();
		try {
			// Signature and Authorization Header
			JSONObject sigResult = Signature.getRequestSignature(this.url, HttpMethod.POST, event.toString(), headers,
					this.config.apiSecret);
			String contentText = sigResult.getString("contentTxt");
			headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			// --- Make HTTP POST request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, this.url,
					headers, contentText, this.config.httpClient);
			int statusCode = resp.statusCode;
			//
			if (statusCode >= 200 && statusCode < 300) {
				response.put("success", true).put("status", "success").put("status_code", statusCode).put("message",
						resp.jsonResponse.optString("message_id")).put("raw_response", resp.jsonResponse);
			} else {
				response.put("success", false).put("status", "fail").put("status_code", statusCode).put("message",
						resp.errMsg).put("raw_response",  resp.jsonResponse);
			}
		} catch (SuprsendException | IOException e) {
			response.put("success", false).put("status", "fail").put("status_code", 500).put("message", e.toString()).put("raw_response", JSONObject.NULL);
		}
		return response;
	}
}
