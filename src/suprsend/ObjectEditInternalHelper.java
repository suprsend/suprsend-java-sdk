package suprsend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

class ObjectEditInternalHelper {
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

	public static final List<String> OTHER_RESERVED_KEYS = Arrays.asList("$messenger", "$inbox", KEY_ID_PROVIDER,
			"$device_id", "$insert_id", "$time", "$set", "$set_once", "$add", "$append", "$remove", "$unset",
			"$identify", "$anon_id", "$identified_id", KEY_PREFERRED_LANGUAGE, KEY_TIMEZONE, "$notification_delivered",
			"$notification_dismiss", "$notification_clicked");

	public static final List<String> SUPER_PROPERTY_KEYS = Arrays.asList("$app_version_string", "$app_build_number",
			"$brand", "$carrier", "$manufacturer", "$model", "$os", "$ss_sdk_version", "$insert_id", "$time");

	public static final List<String> ALL_RESERVED_KEYS = getAllReservedKeys();

	private static final List<String> getAllReservedKeys() {
		ArrayList<String> allReservedKeys = new ArrayList<String>();
		allReservedKeys.addAll(IDENT_KEYS_ALL);
		allReservedKeys.addAll(OTHER_RESERVED_KEYS);
		allReservedKeys.addAll(SUPER_PROPERTY_KEYS);
		return allReservedKeys;
	}

	// --------------
	private JSONObject dictSet, dictSetOnce, dictIncrement, dictAppend, dictRemove;
	private List<String> listUnset, errors, info;

	ObjectEditInternalHelper() {
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
		JSONObject retValue = new JSONObject().put("errors", this.errors).put("info", this.info).put("operation", operation);
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

	private boolean validateKeyPrefix(String key, String caller) {
		if (ALL_RESERVED_KEYS.contains(key) == false) {
			String kLower = key.toLowerCase();
			if (kLower.startsWith("$") || (kLower.length() >= 3 && "ss_".equals(kLower.substring(0, 3)))) {
				this.info.add(
						String.format("[%s] skipping key: %s, key starting with [$, ss_] are reserved", caller, key));
				return false;
			}
		}
		return true;
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
			boolean isKValid = validateKeyPrefix(key, caller);
			if (isKValid) {
				this.dictAppend.put(key, value);
			}
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
			boolean isKValid = validateKeyPrefix(key, caller);
			if (isKValid) {
				this.dictAppend.put(key, value);
			}
		}
	}

	void appendKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		boolean isKValid = validateKeyPrefix(key, caller);
		if (isKValid) {
			this.dictAppend.put(key, value);
		}
	}

	void setKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		boolean isKValid = validateKeyPrefix(key, caller);
		if (isKValid) {
			this.dictSet.put(key, value);
		}
	}

	void setOnceKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		boolean isKValid = validateKeyPrefix(key, caller);
		if (isKValid) {
			this.dictSetOnce.put(key, value);
		}
	}

	void incrementKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		boolean isKValid = validateKeyPrefix(key, caller);
		if (isKValid) {
			this.dictIncrement.put(key, value);
		}
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
			boolean isKValid = validateKeyPrefix(key, caller);
			if (isKValid) {
				this.dictRemove.put(key, value);
			}
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
			boolean isKValid = validateKeyPrefix(key, caller);
			if (isKValid) {
				this.dictRemove.put(key, value);
			}
		}
	}

	void removeKV(String key, Object value, JSONObject kwargs, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		key = res.getString("key");
		boolean isKValid = validateKeyPrefix(key, caller);
		if (isKValid) {
			this.dictRemove.put(key, value);
		}
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
		// Check language code is in the list
		if (!LanguageCode.ALL_LANG_CODES.contains(langCode)) {
			this.info.add(String.format("[%s] invalid value %s", caller, langCode));
			return;
		}
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

	// -------------------------------
	private JSONObject checkIdentValString(String value, String caller) {
		String msg = "need a string with proper value";
		boolean isError = false;
		//
		if (value == null || value.trim().isEmpty()) {
			this.errors.add(String.format("[%s] %s", caller, msg));
			isError = true;
		} else {
			value = value.trim();
		}
		return new JSONObject().put("value", value).put("is_valid", !isError);
	}

	// ------------------------------- Email

	void addEmail(String value, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_EMAIL, res.getString("value"));
	}

	void removeEmail(String value, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_EMAIL, res.getString("value"));
	}

	// ------------------------------- SMS

	void addSms(String value, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_SMS, res.getString("value"));
	}

	void removeSms(String value, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_SMS, res.getString("value"));
	}

	// ------------------------------- Whatsapp

	void addWhatsapp(String value, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_WHATSAPP, res.getString("value"));
	}

	void removeWhatsapp(String value, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_WHATSAPP, res.getString("value"));
	}

	// ------------------------------- Androidpush

	private JSONObject checkAndroidpushValue(String value, String provider, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		//
		boolean isError = false;
		if (!res.getBoolean("is_valid")) {
			isError = true;
		} else {
			if (provider == null) {
				provider = "";
			}
			provider = provider.trim().toLowerCase();
		}
		return new JSONObject().put("value", value).put("provider", provider).put("is_valid", !isError);
	}

	void addAndroidpush(String value, String provider, String caller) {
		JSONObject res = checkAndroidpushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_ANDROIDPUSH, res.getString("value"));
		this.dictAppend.put(KEY_ID_PROVIDER, res.getString("provider"));
	}

	void removeAndroidpush(String value, String provider, String caller) {
		JSONObject res = checkAndroidpushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_ANDROIDPUSH, res.getString("value"));
		this.dictRemove.put(KEY_ID_PROVIDER, res.getString("provider"));
	}

	// ------------------------ Iospush

	private JSONObject checkIospushValue(String value, String provider, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		//
		boolean isError = false;
		if (!res.getBoolean("is_valid")) {
			isError = true;
		} else {
			if (provider == null) {
				provider = "";
			}
			provider = provider.trim().toLowerCase();
		}
		return new JSONObject().put("value", value).put("provider", provider).put("is_valid", !isError);
	}

	void addIospush(String value, String provider, String caller) {
		JSONObject res = checkIospushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_IOSPUSH, res.getString("value"));
		this.dictAppend.put(KEY_ID_PROVIDER, res.getString("provider"));
	}

	void removeIospush(String value, String provider, String caller) {
		JSONObject res = checkIospushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_IOSPUSH, res.getString("value"));
		this.dictRemove.put(KEY_ID_PROVIDER, res.getString("provider"));
	}

	// ------------------------ Webpush

	private JSONObject checkWebpushDict(JSONObject value, String provider, String caller) {
		boolean isError = false;
		if (value == null || value.isEmpty()) {
			this.errors.add(String.format("[%s] value must be a valid json representing webpush-token", caller));
			isError = true;
		} else {
			if (provider == null) {
				provider = "";
			}
			provider = provider.trim().toLowerCase();
		}
		JSONObject response = new JSONObject().put("value", value).put("provider", provider).put("is_valid", !isError);
		return response;
	}

	void addWebpush(JSONObject value, String provider, String caller) {
		JSONObject res = checkWebpushDict(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_WEBPUSH, res.getJSONObject("value"));
		this.dictAppend.put(KEY_ID_PROVIDER, res.getString("provider"));
	}

	void removeWebpush(JSONObject value, String provider, String caller) {
		JSONObject res = checkWebpushDict(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_WEBPUSH, res.getJSONObject("value"));
		this.dictRemove.put(KEY_ID_PROVIDER, res.getString("provider"));
	}

	// ------------------------ Slack

	private JSONObject checkSlackDict(JSONObject value, String caller) {
		boolean isError = false;
		if (value == null || value.isEmpty()) {
			this.errors.add(String.format("[%s] value must be a valid dict/json with proper keys", caller));
			isError = true;
		}
		JSONObject response = new JSONObject().put("value", value).put("is_valid", !isError);
		return response;
	}

	void addSlack(JSONObject value, String caller) {
		JSONObject res = checkSlackDict(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_SLACK, res.getJSONObject("value"));
	}

	void removeSlack(JSONObject value, String caller) {
		JSONObject res = checkSlackDict(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_SLACK, res.getJSONObject("value"));
	}

	// ------------------------ MS Teams

	private JSONObject checkMSTeamsDict(JSONObject value, String caller) {
		boolean isError = false;
		if (value == null || value.isEmpty()) {
			this.errors.add(String.format("[%s] value must be a valid dict/json with proper keys", caller));
			isError = true;
		}
		JSONObject response = new JSONObject().put("value", value).put("is_valid", !isError);
		return response;
	}

	void addMSTeams(JSONObject value, String caller) {
		JSONObject res = checkMSTeamsDict(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_MS_TEAMS, res.getJSONObject("value"));
	}

	void removeMSTeams(JSONObject value, String caller) {
		JSONObject res = checkMSTeamsDict(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_MS_TEAMS, res.getJSONObject("value"));
	}
}
