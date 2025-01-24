package suprsend;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class ObjectEdit {
	private static final Logger logger = Logger.getLogger(ObjectEdit.class.getName());

	private Suprsend config;
	private String objectType, objectId;
	//
	private List<String> errors, info;
	private List<JSONObject> operations;

	private ObjectEditInternalHelper helper;

	ObjectEdit(Suprsend config, String objectType, String objectId) {
		this.config = config;
		this.objectType = objectType;
		this.objectId = objectId;
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
		//
		this.operations = new ArrayList<JSONObject>();
		this.helper = new ObjectEditInternalHelper();
	}

	String getObjectType() {
		return this.objectType;
	}

	String getObjectId() {
		return this.objectId;
	}

	public List<String> getWarnings() {
		return this.info;
	}

	public List<String> getErrors() {
		return this.errors;
	}

	public JSONObject getPayload() {
		return new JSONObject().put("operations", this.operations);
	}

	void validateBody() {
		if (!this.info.isEmpty()) {
			String msg = String.format("[Object: %s/%s] %s", this.objectType, this.objectId,
					String.join("\n", this.info));
			System.out.println(String.format("WARNING: %s", msg));
		}
		if (!this.errors.isEmpty()) {
			String msg = String.format("[Object: %s/%s] %s", this.objectType, this.objectId,
					String.join("\n", this.errors));
			// throw new InputValueException(String.format("ERROR: %s", msg));
			System.out.println(String.format("ERROR: %s", msg));
		}
	}

	private void collectOperation() {
		JSONObject response = this.helper.getOperationResult();

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
		JSONObject ops = response.optJSONObject("operation");
		if (ops != null && !ops.isEmpty()) {
			this.operations.add(ops);
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
		collectOperation();
	}

	public void append(String arg1, String arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	public void append(String arg1, JSONObject arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	public void append(String arg1, Object arg2) {
		String caller = "append";
		this.helper.appendKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	// =========================================================== Set
	public void set(JSONObject arg1) {
		String caller = "set";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.setKV(key, arg1.get(key), arg1, caller);
		}
		collectOperation();
	}

	// TODO: should use a JSON serializable type instead of Object
	public void set(String arg1, Object arg2) {
		String caller = "set";
		this.helper.setKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	// =========================================================== SetOnce
	public void setOnce(JSONObject arg1) {
		String caller = "set_once";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.setOnceKV(key, arg1.get(key), arg1, caller);
		}
		collectOperation();
	}

	public void setOnce(String arg1, Object arg2) {
		String caller = "set_once";
		this.helper.setOnceKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	// =========================================================== Increment
	public void increment(JSONObject arg1) {
		String caller = "increment";
		String[] keys = JSONObject.getNames(arg1);
		for (String key : keys) {
			this.helper.incrementKV(key, arg1.get(key), arg1, caller);
		}
		collectOperation();
	}

	public void increment(String arg1, Object arg2) {
		String caller = "increment";
		this.helper.incrementKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
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
		collectOperation();
	}

	public void remove(String arg1, String arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	public void remove(String arg1, JSONObject arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	public void remove(String arg1, Object arg2) {
		String caller = "remove";
		this.helper.removeKV(arg1, arg2, new JSONObject(), caller);
		collectOperation();
	}

	// =========================================================== Unset

	public void unset(String key) {
		String caller = "unset";
		this.helper.unsetK(key, caller);
		collectOperation();
	}

	public void unset(List<String> key) {
		String caller = "unset";
		for (String k : key) {
			this.helper.unsetK(k, caller);
		}
		collectOperation();
	}

	// =========================================== Preferred language

	public void setPreferredLanguage(String langCode) {
		String caller = "set_preferred_language";
		this.helper.setPreferredLanguage(langCode, caller);
		collectOperation();
	}

	// =========================================== Timezone

	public void setTimezone(String timezone) {
		String caller = "set_timezone";
		this.helper.setTimezone(timezone, caller);
		collectOperation();
	}

	// =========================================================== Email

	public void addEmail(String value) {
		String caller = "add_email";
		this.helper.addEmail(value, caller);
		collectOperation();
	}

	public void removeEmail(String value) {
		String caller = "remove_email";
		this.helper.removeEmail(value, caller);
		collectOperation();
	}

	// =========================================================== SMS

	public void addSms(String value) {
		String caller = "add_sms";
		this.helper.addSms(value, caller);
		collectOperation();
	}

	public void removeSms(String value) {
		String caller = "remove_sms";
		this.helper.removeSms(value, caller);
		collectOperation();
	}

	// =========================================================== Whatsapp

	public void addWhatsapp(String value) {
		String caller = "add_whatsapp";
		this.helper.addWhatsapp(value, caller);
		collectOperation();
	}

	public void removeWhatsapp(String value) {
		String caller = "remove_whatsapp";
		this.helper.removeWhatsapp(value, caller);
		collectOperation();
	}

	// =========================================================== Androidpush

	public void addAndroidpush(String value) {
		String caller = "add_androidpush";
		this.helper.addAndroidpush(value, null, caller);
		collectOperation();
	}

	public void addAndroidpush(String value, String provider) {
		String caller = "add_androidpush";
		this.helper.addAndroidpush(value, provider, caller);
		collectOperation();
	}

	public void removeAndroidpush(String value) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidpush(value, null, caller);
		collectOperation();
	}

	public void removeAndroidpush(String value, String provider) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidpush(value, provider, caller);
		collectOperation();
	}

	// =========================================================== Iospush
	public void addIospush(String value) {
		String caller = "add_iospush";
		this.helper.addIospush(value, null, caller);
		collectOperation();
	}

	public void addIospush(String value, String provider) {
		String caller = "add_iospush";
		this.helper.addIospush(value, provider, caller);
		collectOperation();
	}

	public void removeIospush(String value) {
		String caller = "remove_iospush";
		this.helper.removeIospush(value, null, caller);
		collectOperation();
	}

	public void removeIospush(String value, String provider) {
		String caller = "remove_iospush";
		this.helper.removeIospush(value, provider, caller);
		collectOperation();
	}

	// =========================================================== Webpush
	public void addWebpush(JSONObject value) {
		String caller = "add_webpush";
		this.helper.addWebpush(value, null, caller);
		collectOperation();
	}

	public void addWebpush(JSONObject value, String provider) {
		String caller = "add_webpush";
		this.helper.addWebpush(value, provider, caller);
		collectOperation();
	}

	public void removeWebpush(JSONObject value) {
		String caller = "remove_webpush";
		this.helper.removeWebpush(value, null, caller);
		collectOperation();
	}

	public void removeWebpush(JSONObject value, String provider) {
		String caller = "remove_webpush";
		this.helper.removeWebpush(value, provider, caller);
		collectOperation();
	}

	// =========================================================== Slack
	public void addSlack(JSONObject value) {
		String caller = "add_slack";
		this.helper.addSlack(value, caller);
		collectOperation();
	}

	public void removeSlack(JSONObject value) {
		String caller = "remove_slack";
		this.helper.removeSlack(value, caller);
		collectOperation();
	}

	// =========================================================== MS Teams
	public void addMSTeams(JSONObject value) {
		String caller = "add_ms_teams";
		this.helper.addMSTeams(value, caller);
		collectOperation();
	}

	public void removeMSTeams(JSONObject value) {
		String caller = "remove_ms_teams";
		this.helper.removeMSTeams(value, caller);
		collectOperation();
	}

}
