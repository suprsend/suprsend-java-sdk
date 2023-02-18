package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import org.json.JSONObject;

public class EventCollector {
	private static final Logger logger = Logger.getLogger(EventCollector.class.getName());

	private Suprsend config;
	private String url;
	private JSONObject commonHeaders;

	EventCollector(Suprsend config) {
		this.config = config;
		this.url = getUrl();
		this.commonHeaders = getCommonHeaders();
	}

	private String getUrl() {
		String urlTemplate = "%sevent/";
		return String.format(urlTemplate, this.config.baseUrl);
	}

	private JSONObject getCommonHeaders() {
		return new JSONObject()
				.put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent);
	}

	private JSONObject dynamicHeaders() {
		return new JSONObject().put("Date", Utils.getCurrentDateTimeFormatted(Constants.HEADER_DATE_FMT));
	}

	/**
	 * Headers required to trigger workflow request
	 * 
	 * @return Headers as JSON object
	 */
	private JSONObject getMergedHeaders() {
		JSONObject dynHeaders = dynamicHeaders();
		JSONObject merged = Utils.mergeJSONObjects(this.commonHeaders, dynHeaders);
		return merged;
	}

	public JSONObject collect(Event event) throws SuprsendException, UnsupportedEncodingException {
		JSONObject finalJson = event.getFinalJson(config, false);
		JSONObject eventDict = finalJson.getJSONObject("event");
		int eventSize = finalJson.getInt("apparent_size");
		return send(eventDict);
	}

	private JSONObject send(JSONObject event) {
		JSONObject headers = getMergedHeaders();
		JSONObject response = new JSONObject();
		try {
			String contentText;
			// Signature and Authorization Header
			JSONObject sigResult = Signature.getRequestSignature(this.url, HttpMethod.POST, event.toString(), headers,
					this.config.workspaceSecret);
			contentText = sigResult.getString("contentTxt");
			headers.put("Authorization",
					String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
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
