package suprsend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

class UserEditInternalHelper {
	// -------------- Constants
	public static final String IDENT_KEY_EMAIL = "$email";
	public static final String IDENT_KEY_SMS = "$sms";
	public static final String IDENT_KEY_ANDROIDPUSH = "$androidpush";
	public static final String IDENT_KEY_IOSPUSH = "$iospush";
	public static final String IDENT_KEY_WHATSAPP = "$whatsapp";
	public static final String IDENT_KEY_WEBPUSH = "$webpush";
	public static final String IDENT_KEY_SLACK = "$slack";
	public static final String IDENT_KEY_MS_TEAMS = "$ms_teams";

	public static final List<String> IDENT_KEYS_ALL = Arrays.asList(IDENT_KEY_EMAIL, IDENT_KEY_SMS,
			IDENT_KEY_ANDROIDPUSH, IDENT_KEY_IOSPUSH, IDENT_KEY_WHATSAPP, IDENT_KEY_WEBPUSH, IDENT_KEY_SLACK,
			IDENT_KEY_MS_TEAMS);

	public static final String KEY_ID_PROVIDER = "$id_provider";
	public static final String KEY_PREFERRED_LANGUAGE = "$preferred_language";
	public static final String KEY_TIMEZONE = "$timezone";

	// --------------
	private JSONObject dictSet, dictSetOnce, dictIncrement, dictAppend, dictRemove;
	private List<String> listUnset, errors, info;

	UserEditInternalHelper() {
		this.dictSet = new JSONObject();
		this.dictSetOnce = new JSONObject();
		this.dictIncrement = new JSONObject();
		this.dictAppend = new JSONObject();
		this.dictRemove = new JSONObject();
		this.listUnset = new ArrayList<String>();
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
	}

	private void reset() {
		this.dictSet = new JSONObject();
		this.dictSetOnce = new JSONObject();
		this.dictIncrement = new JSONObject();
		this.dictAppend = new JSONObject();
		this.dictRemove = new JSONObject();
		this.listUnset = new ArrayList<String>();
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
	}

	JSONObject getOperationResult() {
		JSONObject operation = formOperation();
		JSONObject retValue = new JSONObject().put("errors", this.errors).put("info", this.info).put("operation",
				operation);
		reset();
		return retValue;
	}

	private JSONObject formOperation() {
		JSONObject ops = new JSONObject();
		if (this.dictSet.length() > 0) {
			ops.put("$set", this.dictSet);
		}
		if (this.dictSetOnce.length() > 0) {
			ops.put("$set_once", this.dictSetOnce);
		}
		if (this.dictIncrement.length() > 0) {
			ops.put("$add", this.dictIncrement);
		}
		if (this.dictAppend.length() > 0) {
			ops.put("$append", this.dictAppend);
		}
		if (this.dictRemove.length() > 0) {
			ops.put("$remove", this.dictRemove);
		}
		if (this.listUnset.size() > 0) {
			ops.put("$unset", this.listUnset);
		}
		return ops;
	}

	private JSONObject validateKeyBasic(String key, String caller) {
		boolean isError = false;
		if (key == null || key.trim().isEmpty()) {
			this.info.add(String.format("[%s] skipping key: empty string", caller));
			isError = true;
		} else {
			key = key.trim();
		}
		return new JSONObject().put("key", key).put("is_valid", !isError);
	}

	private boolean isIdentitykey(String key) {
		return IDENT_KEYS_ALL.contains(key);
	}

	void appendKV(String key, String value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		if (isIdentitykey(key)) {
			addIdentity(key, value, kwargs, caller);
		} else {
			this.dictAppend.put(key, value);
		}
	}

	void appendKV(String key, JSONObject value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		if (isIdentitykey(key)) {
			addIdentity(key, value, kwargs, caller);
		} else {
			this.dictAppend.put(key, value);
		}
	}

	void appendKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		this.dictAppend.put(key, value);
	}

	void setKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		this.dictSet.put(key, value);
	}

	void setOnceKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		this.dictSetOnce.put(key, value);
	}

	void incrementKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		this.dictIncrement.put(key, value);
	}

	void removeKV(String key, String value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		if (isIdentitykey(key)) {
			removeIdentity(key, value, kwargs, caller);
		} else {
			this.dictRemove.put(key, value);
		}
	}

	void removeKV(String key, JSONObject value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		if (isIdentitykey(key)) {
			removeIdentity(key, value, kwargs, caller);
		} else {
			this.dictRemove.put(key, value);
		}
	}

	void removeKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		this.dictRemove.put(key, value);
	}

	void unsetK(String key, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		this.listUnset.add(res.getString("key"));
	}

	void setPreferredLanguage(String langCode, String caller) {
		this.dictSet.put(KEY_PREFERRED_LANGUAGE, langCode);
	}

	void setTimezone(String timezone, String caller) {
		this.dictSet.put(KEY_TIMEZONE, timezone);
	}

	private void addIdentity(String key, String value, JSONObject kwargs, String caller) {
		String newCaller = String.format("%s:%s", caller, key);
		if (IDENT_KEY_EMAIL.equals(key)) {
			addEmail(value, newCaller);

		} else if (IDENT_KEY_SMS.equals(key)) {
			addSms(value, newCaller);

		} else if (IDENT_KEY_WHATSAPP.equals(key)) {
			addWhatsapp(value, newCaller);

		} else if (IDENT_KEY_ANDROIDPUSH.equals(key)) {
			addAndroidpush(value, kwargs.optString(KEY_ID_PROVIDER), newCaller);

		} else if (IDENT_KEY_IOSPUSH.equals(key)) {
			addIospush(value, kwargs.optString(KEY_ID_PROVIDER), newCaller);

		}
	}

	private void addIdentity(String key, JSONObject value, JSONObject kwargs, String caller) {
		String newCaller = String.format("%s:%s", caller, key);
		if (IDENT_KEY_WEBPUSH.equals(key)) {
			addWebpush(value, kwargs.optString(KEY_ID_PROVIDER), newCaller);

		} else if (IDENT_KEY_SLACK.equals(key)) {
			addSlack(value, newCaller);

		} else if (IDENT_KEY_MS_TEAMS.equals(key)) {
			addMSTeams(value, newCaller);

		}
	}

	private void removeIdentity(String key, String value, JSONObject kwargs, String caller) {
		String newCaller = String.format("%s:%s", caller, key);
		if (IDENT_KEY_EMAIL.equals(key)) {
			removeEmail(value, newCaller);

		} else if (IDENT_KEY_SMS.equals(key)) {
			removeSms(value, newCaller);

		} else if (IDENT_KEY_WHATSAPP.equals(key)) {
			removeWhatsapp(value, newCaller);

		} else if (IDENT_KEY_ANDROIDPUSH.equals(key)) {
			removeAndroidpush(value, kwargs.optString(KEY_ID_PROVIDER), newCaller);

		} else if (IDENT_KEY_IOSPUSH.equals(key)) {
			removeIospush(value, kwargs.optString(KEY_ID_PROVIDER), newCaller);

		}
	}

	private void removeIdentity(String key, JSONObject value, JSONObject kwargs, String caller) {
		String newCaller = String.format("%s:%s", caller, key);
		if (IDENT_KEY_WEBPUSH.equals(key)) {
			removeWebpush(value, kwargs.optString(KEY_ID_PROVIDER), newCaller);

		} else if (IDENT_KEY_SLACK.equals(key)) {
			removeSlack(value, newCaller);

		} else if (IDENT_KEY_MS_TEAMS.equals(key)) {
			removeMSTeams(value, newCaller);

		}
	}

	// ------------------------------- Email

	void addEmail(String value, String caller) {
		this.dictAppend.put(IDENT_KEY_EMAIL, value);
	}

	void removeEmail(String value, String caller) {
		this.dictRemove.put(IDENT_KEY_EMAIL, value);
	}

	// ------------------------------- SMS

	void addSms(String value, String caller) {
		this.dictAppend.put(IDENT_KEY_SMS, value);
	}

	void removeSms(String value, String caller) {
		this.dictRemove.put(IDENT_KEY_SMS, value);
	}

	// ------------------------------- Whatsapp

	void addWhatsapp(String value, String caller) {
		this.dictAppend.put(IDENT_KEY_WHATSAPP, value);
	}

	void removeWhatsapp(String value, String caller) {
		this.dictRemove.put(IDENT_KEY_WHATSAPP, value);
	}

	// ------------------------------- Androidpush

	void addAndroidpush(String value, String provider, String caller) {
		this.dictAppend.put(IDENT_KEY_ANDROIDPUSH, value);
		this.dictAppend.put(KEY_ID_PROVIDER, provider);
	}

	void removeAndroidpush(String value, String provider, String caller) {
		this.dictRemove.put(IDENT_KEY_ANDROIDPUSH, value);
		this.dictRemove.put(KEY_ID_PROVIDER, provider);
	}

	// ------------------------ Iospush

	void addIospush(String value, String provider, String caller) {
		this.dictAppend.put(IDENT_KEY_IOSPUSH, value);
		this.dictAppend.put(KEY_ID_PROVIDER, provider);
	}

	void removeIospush(String value, String provider, String caller) {
		this.dictRemove.put(IDENT_KEY_IOSPUSH, value);
		this.dictRemove.put(KEY_ID_PROVIDER, provider);
	}

	// ------------------------ Webpush

	void addWebpush(JSONObject value, String provider, String caller) {
		this.dictAppend.put(IDENT_KEY_WEBPUSH, value);
		this.dictAppend.put(KEY_ID_PROVIDER, provider);
	}

	void removeWebpush(JSONObject value, String provider, String caller) {
		this.dictRemove.put(IDENT_KEY_WEBPUSH, value);
		this.dictRemove.put(KEY_ID_PROVIDER, provider);
	}

	// ------------------------ Slack

	void addSlack(JSONObject value, String caller) {
		this.dictAppend.put(IDENT_KEY_SLACK, value);
	}

	void removeSlack(JSONObject value, String caller) {
		this.dictRemove.put(IDENT_KEY_SLACK, value);
	}

	// ------------------------ MS Teams

	void addMSTeams(JSONObject value, String caller) {
		this.dictAppend.put(IDENT_KEY_MS_TEAMS, value);
	}

	void removeMSTeams(JSONObject value, String caller) {
		this.dictRemove.put(IDENT_KEY_MS_TEAMS, value);
	}
}
