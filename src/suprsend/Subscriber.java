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
	private String distinctId, url;
	private JSONObject superProperties;
	//
	private List<String> errors, info;
	private List<JSONObject> userOperations;

	private SubscriberInternalHelper helper;

	private List<String> __warningsList;

	Subscriber(Suprsend config, String distinctId) {
		this.config = config;
		this.distinctId = distinctId;
		this.url = String.format("%sevent/", this.config.baseUrl);
		this.superProperties = getSuperProperties();
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
		//
		this.userOperations = new ArrayList<JSONObject>();
		this.helper = new SubscriberInternalHelper();
		//
		this.__warningsList = new ArrayList<String>();
	}

	/**
	 * @return Headers as JSON object
	 */
	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
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

	public JSONObject getEvent() {
		return new JSONObject()
				.put("$schema", "2")
				.put("$insert_id", UUID.randomUUID().toString())
				.put("$time", Instant.now().getEpochSecond() * 1000)
				.put("env", this.config.apiKey)
				.put("distinct_id", this.distinctId)
				.put("$user_operations", this.userOperations)
				.put("properties", this.superProperties);
	}

	public JSONObject asJson() {
		return new JSONObject()
				.put("distinct_id", this.distinctId)
				.put("$user_operations", this.userOperations)
				.put("warnings", this.__warningsList);
	}

	JSONObject validateEventSize(JSONObject eventDict) throws UnsupportedEncodingException, InputValueException {
		int apparentSize = Utils.getApparentIdentityEventSize(eventDict);
		if (apparentSize > Constants.IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("User Event size too big - %d Bytes, must not cross %s", apparentSize,
					Constants.IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new InputValueException(errMsg);
		}
		return new JSONObject().put("event", eventDict).put("apparent_size", apparentSize);
	}

	List<String> validateBody(boolean isPartOfBulk) {
		this.__warningsList = new ArrayList<String>();
		if (!this.info.isEmpty()) {
			String msg = String.format("[distinct_id: %s] %s", this.distinctId, String.join("\n", this.info));
			this.__warningsList.add(msg);
			// print on console as well
			System.out.println(String.format("WARNING: %s", msg));
		}
		if (!this.errors.isEmpty()) {
			String msg = String.format("[distinct_id: %s] %s", this.distinctId, String.join("\n", this.errors));
			this.__warningsList.add(msg);
			String errMsg = String.format("ERROR: %s", msg);
			if (isPartOfBulk) {
				// print on console in case of bulk-api
				System.out.println(errMsg);
			} else {
				// raise error in case of single api
				// throw new InputValueException(errMsg); // Removed exception throwing, let backend handle it
				System.out.println(errMsg);
			}
		}
		//
		return this.__warningsList;
	}

	public JSONObject save() {
		JSONObject response = new JSONObject();
		try {
			validateBody(false);
			JSONObject headers = getHeaders();
			JSONObject eventTemp = getEvent();
			// # --- validate event size
			JSONObject ev = validateEventSize(eventTemp);
			//
			JSONObject validatedEvent = ev.getJSONObject("event");
			// int apparentSize = ev.getInt("apparent_size");
			//
			// Signature and Authorization Header
			JSONObject sigResult = Signature.getRequestSignature(this.url, HttpMethod.POST, validatedEvent.toString(),
					headers, this.config.apiSecret);
			String contentText = sigResult.getString("contentTxt");
			headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			// --- Make HTTP POST request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, this.url,
					headers, contentText);
			int statusCode = resp.statusCode;
			String responseText = resp.responseText;
			//
			if (statusCode >= 200 && statusCode < 300) {
				response.put("success", true).put("status", "success").put("status_code", statusCode).put("message",
						responseText);
			} else {
				response.put("success", false).put("status", "fail").put("status_code", statusCode).put("message",
						responseText);
			}
		} catch (SuprsendException | IOException e) {
			response.put("success", false).put("status", "fail").put("status_code", 500).put("message", e.toString());
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
			this.userOperations.add(event);
		}
	}

	// =========================================================== Append
	public void append(JSONObject arg1) {
		String caller = "append";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			if (arg1.get(key) instanceof String) {
				this.helper.appendKV(key, arg1.getString(key), arg1, caller);
			} else if (arg1.get(key) instanceof JSONObject) {
				this.helper.appendKV(key, arg1.getJSONObject(key), arg1, caller);
			} else {
				this.helper.appendKV(key, arg1.get(key), arg1, caller);
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

	public void append(String arg1, Object arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	// =========================================================== Set
	public void set(JSONObject arg1) {
		String caller = "set";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.setKV(key, arg1.get(key), arg1, caller);
		}
		collectEvent();
	}

	// TODO: should use a JSON serializable type instead of Object
	public void set(String arg1, Object arg2) {
		String caller = "set";
		this.helper.setKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	// =========================================================== SetOnce
	public void setOnce(JSONObject arg1) {
		String caller = "set_once";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.setOnceKV(key, arg1.get(key), arg1, caller);
		}
		collectEvent();
	}

	public void setOnce(String arg1, Object arg2) {
		String caller = "set_once";
		this.helper.setOnceKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	// =========================================================== Increment
	public void increment(JSONObject arg1) {
		String caller = "increment";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.incrementKV(key, arg1.get(key), arg1, caller);
		}
		collectEvent();
	}

	public void increment(String arg1, Object arg2) {
		String caller = "increment";
		this.helper.incrementKV(arg1, arg2, new JSONObject(), caller);
		collectEvent();
	}

	// =========================================================== Remove
	public void remove(JSONObject arg1) {
		String caller = "remove";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			if (arg1.get(key) instanceof String) {
				this.helper.removeKV(key, arg1.getString(key), arg1, caller);
			} else if (arg1.get(key) instanceof JSONObject) {
				this.helper.removeKV(key, arg1.getJSONObject(key), arg1, caller);
			} else {
				this.helper.removeKV(key, arg1.get(key), arg1, caller);
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

	public void remove(String arg1, Object arg2) {
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

	// =========================================== Preferred language

	public void setPreferredLanguage(String langCode) {
		String caller = "set_preferred_language";
		this.helper.setPreferredLanguage(langCode, caller);
		collectEvent();
	}

	// =========================================== Timezone

	public void setTimezone(String timezone) {
		String caller = "set_timezone";
		this.helper.setTimezone(timezone, caller);
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
	public void addSlack(JSONObject value) {
		String caller = "add_slack";
		this.helper.addSlack(value, caller);
		collectEvent();
	}

	public void removeSlack(JSONObject value) {
		String caller = "remove_slack";
		this.helper.removeSlack(value, caller);
		collectEvent();
	}

	// =========================================================== MS Teams
	public void addMSTeams(JSONObject value) {
		String caller = "add_ms_teams";
		this.helper.addMSTeams(value, caller);
		collectEvent();
	}

	public void removeMSTeams(JSONObject value) {
		String caller = "remove_ms_teams";
		this.helper.removeMSTeams(value, caller);
		collectEvent();
	}

}
