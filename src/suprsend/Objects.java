package suprsend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class Objects {
	private static final Logger logger = Logger.getLogger(Subscriber.class.getName());

	private Suprsend config;
	private String objectType, objectId, url;
	//
	private List<String> errors, info;
	private List<JSONObject> operations;

	private ObjectInternalHelper helper;

	Objects(Suprsend config, String objectType, String objectId) {
		this.config = config;
		this.objectType = objectType;
		this.objectId = objectId;
		this.url = String.format("%sv1/object/%s/%s/", this.config.baseUrl, this.objectType, this.objectId);
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
		//
		this.operations = new ArrayList<JSONObject>();
		this.helper = new ObjectInternalHelper();
	}

	/**
	 * @return Headers as JSON object
	 */
	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	public List<String> getWarnings() {
		return this.info;
	}

	public List<String> getErrors() {
		return this.errors;
	}

	void validateBody() throws InputValueException {
		if (!this.info.isEmpty()) {
			String msg = String.format("[Object: %s/%s] %s", this.objectType, this.objectId, String.join("\n", this.info));
			System.out.println(String.format("WARNING: %s", msg));
		}
		if (!this.errors.isEmpty()) {
			String msg = String.format("[Object: %s/%s] %s", this.objectType, this.objectId, String.join("\n", this.info));
			System.out.println(String.format("ERROR: %s", msg));
		}
	}

	public JSONObject save() {
		JSONObject response = new JSONObject();
		try {
			validateBody();
			JSONObject headers = getHeaders();
			//
			//
			// Signature and Authorization Header
			JSONObject payload = new JSONObject().put("operations", this.operations);
			JSONObject sigResult = Signature.getRequestSignature(this.url, HttpMethod.PATCH, payload.toString(),
					headers, this.config.apiSecret);
			String contentText = sigResult.getString("contentTxt");
			headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			// --- Make HTTP PATCH request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.PATCH, this.url,
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

	private void collectPayload() {
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
		JSONObject payload = response.optJSONObject("payload");
		if (payload != null && !payload.isEmpty()) {
			this.operations.add(payload);
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
		collectPayload();
	}

	public void append(String arg1, String arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	public void append(String arg1, JSONObject arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	public void append(String arg1, Object arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	// =========================================================== Set
	public void set(JSONObject arg1) {
		String caller = "set";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.setKV(key, arg1.get(key), arg1, caller);
		}
		collectPayload();
	}

	// TODO: should use a JSON serializable type instead of Object
	public void set(String arg1, Object arg2) {
		String caller = "set";
		this.helper.setKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	// =========================================================== SetOnce
	public void setOnce(JSONObject arg1) {
		String caller = "set_once";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.setOnceKV(key, arg1.get(key), arg1, caller);
		}
		collectPayload();
	}

	public void setOnce(String arg1, Object arg2) {
		String caller = "set_once";
		this.helper.setOnceKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	// =========================================================== Increment
	public void increment(JSONObject arg1) {
		String caller = "increment";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.incrementKV(key, arg1.get(key), arg1, caller);
		}
		collectPayload();
	}

	public void increment(String arg1, Object arg2) {
		String caller = "increment";
		this.helper.incrementKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
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
		collectPayload();
	}

	public void remove(String arg1, String arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	public void remove(String arg1, JSONObject arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	public void remove(String arg1, Object arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectPayload();
	}

	// =========================================================== Unset

	public void unset(String key) {
		String caller = "unset";
		this.helper.unsetK(key, caller);
		collectPayload();
	}

	public void unset(List<String> key) {
		String caller = "unset";
		for (String k : key) {
			this.helper.unsetK(k, caller);
		}
		collectPayload();
	}

	// =========================================== Preferred language

	public void setPreferredLanguage(String langCode) {
		String caller = "set_preferred_language";
		this.helper.setPreferredLanguage(langCode, caller);
		collectPayload();
	}

	// =========================================== Timezone

	public void setTimezone(String timezone) {
		String caller = "set_timezone";
		this.helper.setTimezone(timezone, caller);
		collectPayload();
	}

	// =========================================================== Email

	public void addEmail(String value) {
		String caller = "add_email";
		this.helper.addEmail(value, caller);
		collectPayload();
	}

	public void removeEmail(String value) {
		String caller = "remove_email";
		this.helper.removeEmail(value, caller);
		collectPayload();
	}

	// =========================================================== SMS

	public void addSms(String value) {
		String caller = "add_sms";
		this.helper.addSms(value, caller);
		collectPayload();
	}

	public void removeSms(String value) {
		String caller = "remove_sms";
		this.helper.removeSms(value, caller);
		collectPayload();
	}

	// =========================================================== Whatsapp

	public void addWhatsapp(String value) {
		String caller = "add_whatsapp";
		this.helper.addWhatsapp(value, caller);
		collectPayload();
	}

	public void removeWhatsapp(String value) {
		String caller = "remove_whatsapp";
		this.helper.removeWhatsapp(value, caller);
		collectPayload();
	}

	// =========================================================== Androidpush

	public void addAndroidpush(String value) {
		String caller = "add_androidpush";
		this.helper.addAndroidpush(value, null, caller);
		collectPayload();
	}

	public void addAndroidpush(String value, String provider) {
		String caller = "add_androidpush";
		this.helper.addAndroidpush(value, provider, caller);
		collectPayload();
	}

	public void removeAndroidpush(String value) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidpush(value, null, caller);
		collectPayload();
	}

	public void removeAndroidpush(String value, String provider) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidpush(value, provider, caller);
		collectPayload();
	}

	// =========================================================== Iospush

	public void addIospush(String value, String provider) {
		String caller = "add_iospush";
		this.helper.addIospush(value, provider, caller);
		collectPayload();
	}

	public void removeIospush(String value, String provider) {
		String caller = "remove_iospush";
		this.helper.removeIospush(value, provider, caller);
		collectPayload();
	}

	// =========================================================== Webpush
	public void addWebpush(JSONObject value) {
		String caller = "add_webpush";
		this.helper.addWebpush(value, null, caller);
		collectPayload();
	}

	public void addWebpush(JSONObject value, String provider) {
		String caller = "add_webpush";
		this.helper.addWebpush(value, provider, caller);
		collectPayload();
	}

	public void removeWebpush(JSONObject value) {
		String caller = "remove_webpush";
		this.helper.removeWebpush(value, null, caller);
		collectPayload();
	}

	public void removeWebpush(JSONObject value, String provider) {
		String caller = "remove_webpush";
		this.helper.removeWebpush(value, provider, caller);
		collectPayload();
	}

	// =========================================================== Slack
	public void addSlack(JSONObject value) {
		String caller = "add_slack";
		this.helper.addSlack(value, caller);
		collectPayload();
	}

	public void removeSlack(JSONObject value) {
		String caller = "remove_slack";
		this.helper.removeSlack(value, caller);
		collectPayload();
	}

	// =========================================================== MS Teams
	public void addMSTeams(JSONObject value) {
		String caller = "add_ms_teams";
		this.helper.addMSTeams(value, caller);
		collectPayload();
	}

	public void removeMSTeams(JSONObject value) {
		String caller = "remove_ms_teams";
		this.helper.removeMSTeams(value, caller);
		collectPayload();
	}

}
