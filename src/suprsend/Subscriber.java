package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class Subscriber {
	private static final Logger logger = Logger.getLogger(Subscriber.class.getName());

	private Suprsend config;
	private String distinctID, url;
	private List<JSONObject> events;
	private List<String> errors, info;
	private int appendCount, removeCount, unsetCount;
	private JSONObject superProperties;
	private SubscriberInternalHelper helper;

	Subscriber(Suprsend config, String distinctID) {
		this.config = config;
		this.distinctID = distinctID;
		this.url = getUrl();
		this.superProperties = getSuperProperties();
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
		this.appendCount = this.removeCount = this.unsetCount = 0;
		//
		this.events = new ArrayList<JSONObject>();
		this.helper = new SubscriberInternalHelper(this.distinctID, this.config.workspaceKey);
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
		String urlFormatted = String.format(urlTemplate, this.config.baseUrl);
		return urlFormatted;
	}

	/**
	 * @return Headers as JSON object
	 */
	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent)
				.put("Date", Utils.getCurrentDateTimeFormatted(Constants.HEADER_DATE_FMT));
	}

	private JSONObject getSuperProperties() {
		return new JSONObject().put("$ss_sdk_version", this.config.userAgent);
	}

	public List<String> getWarnings() {
		return this.info;
	}

	public List<String> getErrors() {
		return this.errors;
	}

	public List<JSONObject> getEvents() {
		// Don't mutate the original array. Make a copy of events.TODO
		List<JSONObject> allEvents = this.events;
		for (JSONObject e : allEvents) {
			e.put("properties", this.superProperties);
		}
		// Add $identify event by default, if new properties get added
		if (allEvents.size() == 0 || this.appendCount > 0) {
			JSONObject userIdentifyEvent = new JSONObject()
					.put("$insert_id", UUID.randomUUID().toString())
					.put("$time", Instant.now().getEpochSecond() * 1000)
					.put("env", this.config.workspaceKey)
					.put("event", "$identify")
					.put("properties", Utils.mergeJSONObjects(
							// # Don't add $anon_id to properties,
							new JSONObject().put("$identified_id", this.distinctID),
							getSuperProperties()
							)
						);
			// Add $identify event at the 0th index, so that $identify runs before
			// $append/$remove/$reset
			allEvents.add(0, userIdentifyEvent);
		}
		return allEvents;
	}

	private JSONObject validateEventSize(JSONObject eventDict) throws UnsupportedEncodingException, SuprsendException {
		int apparentSize = Utils.getApparentIdentityEventSize(eventDict);
		if (apparentSize > Constants.IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("User Event size too big - %d Bytes, must not cross %s", apparentSize,
					Constants.IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new SuprsendException(errMsg);
		}
		return new JSONObject().put("event", eventDict).put("apparent_size", apparentSize);
	}

	private ArrayList<String> validateBody(boolean isPartOfBulk) throws SuprsendException {
		ArrayList<String> warningsList = new ArrayList<String>();
		if (!this.info.isEmpty()) {
			String msg = String.format("[distinct_id: %s] %s", this.distinctID, String.join("\n", this.info));
			warningsList.add(msg);
			// print on console as well
			System.out.println(String.format("WARNING: %s", msg));
		}
		if (!this.errors.isEmpty()) {
			String msg = String.format("[distinct_id: %s] %s", this.distinctID, String.join("\n", this.errors));
			warningsList.add(msg);
			String errMsg = String.format("ERROR: %s", msg);
			if (isPartOfBulk) {
				// print on console in case of bulk-api
				System.out.println(errMsg);
			} else {
				// raise error in case of single api
				throw new SuprsendException(errMsg);
			}
		}
		//
		return warningsList;
	}

	public JSONObject save() {
		JSONObject headers = getHeaders();
		JSONObject response = new JSONObject();
		try {
			validateBody(false);
			List<JSONObject> allEvents = getEvents();
			// # --- validate event size
			for (JSONObject ev : allEvents) {
				JSONObject o = validateEventSize(ev);
			}
			//
			String contentText;
			if (this.config.authEnabled) {
				// Signature and Authorization Header
				JSONObject sigResult = Signature.getRequestSignature(this.url, "POST", allEvents, headers,
						this.config.workspaceSecret);
				contentText = sigResult.getString("contentTxt");
				headers.put("Authorization",
						String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
			} else {
				contentText = this.events.toString();
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

	private void collectEvent() {
		JSONObject response = this.helper.getIdentityEvent();

		JSONArray errors = response.optJSONArray("errors");
		if (errors != null && !errors.isEmpty()) {
			for (int i = 0; i < errors.length(); i++) {
				this.errors.add(errors.getString(i));
			}
		}
		JSONArray info = response.optJSONArray("info");
		if (info != null && !info.isEmpty()) {
			for (int i = 0; i < info.length(); i++) {
				this.info.add(info.getString(i));
			}
		}
		JSONObject event = response.optJSONObject("event");
		if (event != null && !event.isEmpty()) {
			this.events.add(event);
			this.appendCount = this.appendCount + response.getInt("append");
			this.removeCount = this.removeCount + response.getInt("remove");
			this.unsetCount = this.unsetCount + response.getInt("unset");
		}
	}

	// =========================================================== Append
	public void append(JSONObject arg1) {
		String caller = "append";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			if (arg1.get(key) instanceof String) {
				this.helper.appendKV(key, arg1.getString(key), arg1, caller);
			} else {
				this.helper.appendKV(key, arg1.getJSONObject(key), arg1, caller);
			}
		}
		collectEvent();
	}

	public void append(String arg1, String arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	public void append(String arg1, JSONObject arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	// =========================================================== Remove
	public void remove(JSONObject arg1) {
		String caller = "remove";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			if (arg1.get(key) instanceof String) {
				this.helper.removeKV(key, arg1.getString(key), arg1, caller);
			} else {
				this.helper.removeKV(key, arg1.getJSONObject(key), arg1, caller);
			}
		}
		collectEvent();
	}

	public void remove(String arg1, String arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	public void remove(String arg1, JSONObject arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	// =========================================================== Unset

	public void unset(String key) {
		String caller = "unset";
		this.helper.unsetK(key, caller);
		collectEvent();
	}

	public void unset(List<String> key) {
		String caller = "unset";
		for (String k : key) {
			this.helper.unsetK(k, caller);
		}
		collectEvent();
	}

	// =========================================================== Email

	public void addEmail(String value) {
		String caller = "add_email";
		this.helper.addEmail(value, caller);
		collectEvent();
	}

	public void removeEmail(String value) {
		String caller = "remove_email";
		this.helper.removeEmail(value, caller);
		collectEvent();
	}

	// =========================================================== SMS

	public void addSms(String value) {
		String caller = "add_sms";
		this.helper.addSms(value, caller);
		collectEvent();
	}

	public void removeSms(String value) {
		String caller = "remove_sms";
		this.helper.removeSms(value, caller);
		collectEvent();
	}

	// =========================================================== Whatsapp

	public void addWhatsapp(String value) {
		String caller = "add_whatsapp";
		this.helper.addWhatsapp(value, caller);
		collectEvent();
	}

	public void removeWhatsapp(String value) {
		String caller = "remove_whatsapp";
		this.helper.removeWhatsapp(value, caller);
		collectEvent();
	}

	// =========================================================== Androidpush

	public void addAndroidpush(String value) {
		String caller = "add_androidpush";
		this.helper.addAndroidpush(value, null, caller);
		collectEvent();
	}

	public void addAndroidpush(String value, String provider) {
		String caller = "add_androidpush";
		this.helper.addAndroidpush(value, provider, caller);
		collectEvent();
	}

	public void removeAndroidpush(String value) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidpush(value, null, caller);
		collectEvent();
	}

	public void removeAndroidpush(String value, String provider) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidpush(value, provider, caller);
		collectEvent();
	}

	// =========================================================== Iospush
	public void addIospush(String value) {
		String caller = "add_iospush";
		this.helper.addIospush(value, null, caller);
		collectEvent();
	}

	public void addIospush(String value, String provider) {
		String caller = "add_iospush";
		this.helper.addIospush(value, provider, caller);
		collectEvent();
	}

	public void removeIospush(String value) {
		String caller = "remove_iospush";
		this.helper.removeIospush(value, null, caller);
		collectEvent();
	}

	public void removeIospush(String value, String provider) {
		String caller = "remove_iospush";
		this.helper.removeIospush(value, provider, caller);
		collectEvent();
	}

	// =========================================================== Webpush
	public void addWebpush(JSONObject value) {
		String caller = "add_webpush";
		this.helper.addWebpush(value, null, caller);
		collectEvent();
	}

	public void addWebpush(JSONObject value, String provider) {
		String caller = "add_webpush";
		this.helper.addWebpush(value, provider, caller);
		collectEvent();
	}

	public void removeWebpush(JSONObject value) {
		String caller = "remove_webpush";
		this.helper.removeWebpush(value, null, caller);
		collectEvent();
	}

	public void removeWebpush(JSONObject value, String provider) {
		String caller = "remove_webpush";
		this.helper.removeWebpush(value, provider, caller);
		collectEvent();
	}

	// =========================================================== Slack

	public void addSlackEmail(String value) {
		String caller = "add_slack_email";
		this.helper.addSlack(new JSONObject().put("email", value), caller);
		collectEvent();
	}

	public void removeSlackEmail(String value) {
		String caller = "remove_slack_email";
		this.helper.removeSlack(new JSONObject().put("email", value), caller);
		collectEvent();
	}

	public void addSlackUserid(String value) {
		String caller = "add_slack_userid";
		this.helper.addSlack(new JSONObject().put("user_id", value), caller);
		collectEvent();
	}

	public void removeSlackUserid(String value) {
		String caller = "remove_slack_userid";
		this.helper.removeSlack(new JSONObject().put("user_id", value), caller);
		collectEvent();
	}

}
