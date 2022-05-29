package suprsend;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.everit.json.schema.ValidationException;

public class EventCollector {
	private static final Logger logger = Logger.getLogger(EventCollector.class.getName());

	private Suprsend config;
	private String url;
	private JSONObject commonHeaders;
	private JSONObject superProps;

	public static List<String> RESERVED_EVENT_NAMES = Arrays.asList(
			"$identify",
			"$notification_delivered", "$notification_dismiss", "$notification_clicked",
			"$app_launched", "$user_login", "$user_logout");

	EventCollector(Suprsend config) {
		this.config = config;
		this.url = getUrl();
		this.commonHeaders = getCommonHeaders();
		this.superProps = getSuperProperties();
	}

	private String getUrl() {
		String urlTemplate = "%sevent/";
		if (this.config.includeSignatureParam) {
			if (this.config.authEnabled) {
				urlTemplate = urlTemplate + "?verify=true";
			} else {
				urlTemplate = urlTemplate + "?verify=false";
			}
		}
		return String.format(urlTemplate, this.config.baseUrl);
	}

	private JSONObject getCommonHeaders() {
		return new JSONObject()
				.put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent);
	}

	private JSONObject getSuperProperties() {
		return new JSONObject()
				.put("$ss_sdk_version", this.config.userAgent);
	}

	private JSONObject dynamicHeaders() {
		return new JSONObject()
				.put("Date", Utils.getCurrentDateTimeFormatted(Constants.HEADER_DATE_FMT));
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

	private void checkEventPrefix(String eventName) throws SuprsendException {
		if (EventCollector.RESERVED_EVENT_NAMES.contains(eventName) == false) {
			String eLower = eventName.toLowerCase();
			if (eLower.startsWith("$") ||
					(eLower.length() >= 3 && "ss_".equals(eLower.substring(0, 3)))) {
				throw new SuprsendException("eventName starting with [$,ss_] are reserved");
			}
		}
	}

	private String validateEventName(String eventName) throws SuprsendException {
		if (eventName == null || eventName.trim().isEmpty()) {
			throw new SuprsendException("eventName must be passed");
		}
		eventName = eventName.trim();
		checkEventPrefix(eventName);
		return eventName;
	}

	public JSONObject collect(String distinctID, String eventName, JSONObject properties)
			throws SuprsendException, ValidationException {
		eventName = validateEventName(eventName);
		//
		JSONObject merged = Utils.mergeJSONObjects(properties, this.superProps);
		//
		JSONObject event = new JSONObject()
				.put("$insert_id", UUID.randomUUID().toString())
				.put("$time", Instant.now().getEpochSecond() * 1000)
				.put("event", eventName)
				.put("env", this.config.workspaceKey)
				.put("distinct_id", distinctID)
				.put("properties", merged);
		JSONObject validatedEvent = Utils.validateEventSchema(event);
		//
		return send(validatedEvent);
	}

	private JSONObject send(JSONObject event) {
		JSONObject headers = getMergedHeaders();
		JSONObject response = new JSONObject();
		try {
			String contentText;
			if (this.config.authEnabled) {
				// Signature and Authorization Header
				JSONObject sigResult = Signature.getRequestSignature(this.url, "POST", event, headers,
						this.config.workspaceSecret);
				contentText = sigResult.getString("contentTxt");
				headers.put("Authorization",
						String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
			} else {
				contentText = event.toString();
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
}
