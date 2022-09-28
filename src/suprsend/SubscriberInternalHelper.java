package suprsend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.json.JSONObject;

class SubscriberInternalHelper {
	// -------------- Constants
	public static final String IDENT_KEY_EMAIL = "$email";
	public static final String IDENT_KEY_SMS = "$sms";
	public static final String IDENT_KEY_ANDROIDPUSH = "$androidpush";
	public static final String IDENT_KEY_IOSPUSH = "$iospush";
	public static final String IDENT_KEY_WHATSAPP = "$whatsapp";
	public static final String IDENT_KEY_WEBPUSH = "$webpush";
	public static final String IDENT_KEY_SLACK = "$slack";

	public static final List<String> IDENT_KEYS_ALL = Arrays.asList(IDENT_KEY_EMAIL, IDENT_KEY_SMS,
			IDENT_KEY_ANDROIDPUSH, IDENT_KEY_IOSPUSH, IDENT_KEY_WHATSAPP, IDENT_KEY_WEBPUSH, IDENT_KEY_SLACK);

	public static final String KEY_PUSHVENDOR = "$pushvendor";

	public static final List<String> OTHER_RESERVED_KEYS = Arrays.asList("$messenger", "$inbox", KEY_PUSHVENDOR,
			"$device_id", "$insert_id", "$time", "$set", "$set_once", "$add", "$append", "$remove", "$unset",
			"$identify", "$anon_id", "$identified_id", "$notification_delivered", "$notification_dismiss",
			"$notification_clicked");

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

	private static final String EMAIL_REGEX = "^\\S+@\\S+\\.\\S+$";
	private static final String MOBILE_REGEX = "^\\+[0-9\\s]+";

	// --------------
	private String distinctID, workspaceKey;
	private JSONObject dictAppend, dictRemove;
	private int appendCount, removeCount, unsetCount;
	private List<String> listUnset, errors, info;

	SubscriberInternalHelper(String distinctID, String workspaceKey) {
		this.distinctID = distinctID;
		this.workspaceKey = workspaceKey;
		//
		this.dictAppend = new JSONObject();
		this.appendCount = 0;
		//
		this.dictRemove = new JSONObject();
		this.removeCount = 0;
		//
		this.listUnset = new ArrayList<String>();
		this.unsetCount = 0;
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
	}

	private void reset() {
		this.dictAppend = new JSONObject();
		this.dictRemove = new JSONObject();
		this.listUnset = new ArrayList<String>();
		//
		this.appendCount = 0;
		this.removeCount = 0;
		this.unsetCount = 0;
		//
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
	}

	protected JSONObject getIdentityEvent() {
		JSONObject event = formEvent();
		JSONObject retValue = new JSONObject()
				.put("errors", this.errors)
				.put("info", this.info)
				.put("event", event)
				.put("append", this.appendCount)
				.put("remove", this.removeCount)
				.put("unset", this.unsetCount);
		reset();
		return retValue;
	}

	private JSONObject formEvent() {
		if (this.dictAppend.length() > 0 || this.dictRemove.length() > 0 || this.listUnset.size() > 0) {
			JSONObject event = new JSONObject()
					.put("$insert_id", UUID.randomUUID().toString())
					.put("$time", Instant.now().getEpochSecond() * 1000)
					.put("env", this.workspaceKey)
					.put("distinct_id", this.distinctID);

			if (this.dictAppend.length() > 0) {
				event.put("$append", this.dictAppend);
				this.appendCount = this.appendCount + 1;
			}

			if (this.dictRemove.length() > 0) {
				event.put("$remove", this.dictRemove);
				this.removeCount = this.removeCount + 1;
			}

			if (this.listUnset.size() > 0) {
				event.put("$unset", this.listUnset);
				this.unsetCount = this.unsetCount + 1;
			}
			return event;
		}
		return null;
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

	protected void appendKV(String key, String value, JSONObject kwargs, String caller) {
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

	protected void appendKV(String key, JSONObject value, JSONObject kwargs, String caller) {
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

	protected void removeKV(String key, String value, JSONObject kwargs, String caller) {
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

	protected void removeKV(String key, JSONObject value, JSONObject kwargs, String caller) {
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

	protected void unsetK(String key, String caller) {
		JSONObject res = validateKeyBasic(key, caller);
		boolean isKeyValid = res.getBoolean("is_valid");
		if (!isKeyValid) {
			return;
		}
		this.listUnset.add(res.getString("key"));
	}

	private void addIdentity(String key, String value, JSONObject kwargs, String caller) {
		if (IDENT_KEY_EMAIL.equals(key)) {
			addEmail(value, caller);

		} else if (IDENT_KEY_SMS.equals(key)) {
			addSms(value, caller);

		} else if (IDENT_KEY_WHATSAPP.equals(key)) {
			addWhatsapp(value, caller);

		} else if (IDENT_KEY_ANDROIDPUSH.equals(key)) {
			addAndroidpush(value, kwargs.optString(KEY_PUSHVENDOR), caller);
			if (this.dictAppend.optString(KEY_PUSHVENDOR).isEmpty() == false) {
				kwargs.put(KEY_PUSHVENDOR, this.dictAppend.getString(KEY_PUSHVENDOR));
			}

		} else if (IDENT_KEY_IOSPUSH.equals(key)) {
			addIospush(value, kwargs.optString(KEY_PUSHVENDOR), caller);
			if (this.dictAppend.optString(KEY_PUSHVENDOR).isEmpty() == false) {
				kwargs.put(KEY_PUSHVENDOR, this.dictAppend.getString(KEY_PUSHVENDOR));
			}

		}
	}

	private void addIdentity(String key, JSONObject value, JSONObject kwargs, String caller) {
		if (IDENT_KEY_WEBPUSH.equals(key)) {
			addWebpush(value, kwargs.optString(KEY_PUSHVENDOR), caller);
			if (this.dictAppend.optString(KEY_PUSHVENDOR).isEmpty() == false) {
				kwargs.put(KEY_PUSHVENDOR, this.dictAppend.getString(KEY_PUSHVENDOR));
			}
		} else if (IDENT_KEY_SLACK.equals(key)) {
			addSlack(value, caller);
		}
	}

	private void removeIdentity(String key, String value, JSONObject kwargs, String caller) {
		if (IDENT_KEY_EMAIL.equals(key)) {
			removeEmail(value, caller);

		} else if (IDENT_KEY_SMS.equals(key)) {
			removeSms(value, caller);

		} else if (IDENT_KEY_WHATSAPP.equals(key)) {
			removeWhatsapp(value, caller);

		} else if (IDENT_KEY_ANDROIDPUSH.equals(key)) {
			removeAndroidpush(value, kwargs.optString(KEY_PUSHVENDOR), caller);
			if (this.dictRemove.optString(KEY_PUSHVENDOR).isEmpty() == false) {
				kwargs.put(KEY_PUSHVENDOR, this.dictRemove.getString(KEY_PUSHVENDOR));
			}

		} else if (IDENT_KEY_IOSPUSH.equals(key)) {
			removeIospush(value, kwargs.optString(KEY_PUSHVENDOR), caller);
			if (this.dictRemove.optString(KEY_PUSHVENDOR).isEmpty() == false) {
				kwargs.put(KEY_PUSHVENDOR, this.dictRemove.getString(KEY_PUSHVENDOR));
			}
		}
	}

	private void removeIdentity(String key, JSONObject value, JSONObject kwargs, String caller) {
		if (IDENT_KEY_WEBPUSH.equals(key)) {
			removeWebpush(value, kwargs.optString(KEY_PUSHVENDOR), caller);
			if (this.dictRemove.optString(KEY_PUSHVENDOR).isEmpty() == false) {
				kwargs.put(KEY_PUSHVENDOR, this.dictRemove.getString(KEY_PUSHVENDOR));
			}
		} else if (IDENT_KEY_SLACK.equals(key)) {
			removeSlack(value, caller);
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

	private JSONObject validateEmail(String email, String caller) {
		JSONObject res = checkIdentValString(email, caller);
		boolean isError = false;
		if (!res.getBoolean("is_valid")) {
			isError = true;
		} else {
			email = res.getString("value");
			//
			String msg = "value in email format required. e.g. user@example.com";
			int minLength = 6;
			int maxLength = 127;
			//
			boolean isValidEmail = Pattern.compile(EMAIL_REGEX).matcher(email).matches();
			if (!isValidEmail) {
				this.errors.add(String.format("[%s] invalid value %s. %s", caller, email, msg));
				isError = true;
			} else {
				if (email.length() < minLength || email.length() > maxLength) {
					this.errors.add(
							String.format("[%s] invalid value %s. must be 6 <= len(email) <= 127", caller, email, msg));
					isError = true;
				}
			}
		}
		return new JSONObject().put("email", email).put("is_valid", !isError);
	}

	protected void addEmail(String value, String caller) {
		JSONObject res = validateEmail(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_EMAIL, res.getString("email"));
	}

	protected void removeEmail(String value, String caller) {
		JSONObject res = validateEmail(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_EMAIL, res.getString("email"));
	}

	// ------------------------------- Mobile no

	private JSONObject validateMobileNo(String mobileNo, String caller) {
		JSONObject res = checkIdentValString(mobileNo, caller);
		boolean isError = false;
		if (!res.getBoolean("is_valid")) {
			isError = true;
		} else {
			mobileNo = res.getString("value");
			//
			String msg = "number must start with + and must contain country code. e.g. +41446681800";
			int minLength = 8;
			//
			boolean isValidMobileNo = Pattern.compile(MOBILE_REGEX).matcher(mobileNo).matches();
			if (!isValidMobileNo) {
				this.errors.add(String.format("[%s] invalid value %s. %s", caller, mobileNo, msg));
				isError = true;
			} else {
				if (mobileNo.length() < minLength) {
					this.errors.add(
							String.format("[%s] invalid value %s. len(mobile_no) must be >= 8", caller, mobileNo, msg));
					isError = true;
				}
			}
		}
		return new JSONObject().put("mobile", mobileNo).put("is_valid", !isError);
	}

	// ------------------------------- SMS

	protected void addSms(String value, String caller) {
		JSONObject res = validateMobileNo(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_SMS, res.getString("mobile"));
	}

	protected void removeSms(String value, String caller) {
		JSONObject res = validateMobileNo(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_SMS, res.getString("mobile"));
	}

	// ------------------------------- Whatsapp

	protected void addWhatsapp(String value, String caller) {
		JSONObject res = validateMobileNo(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_WHATSAPP, res.getString("mobile"));
	}

	protected void removeWhatsapp(String value, String caller) {
		JSONObject res = validateMobileNo(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_WHATSAPP, res.getString("mobile"));
	}

	// ------------------------------- Androidpush [providers: fcm / xiaomi / oppo]

	private JSONObject checkAndroidpushValue(String value, String provider, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		//
		boolean isError = false;
		if (!res.getBoolean("is_valid")) {
			isError = true;
		} else {
			if (provider == null || provider.trim().isEmpty()) {
				provider = "fcm";
			}
			provider = provider.trim();
			//
			List<String> providers = Arrays.asList("fcm", "xiaomi", "oppo");
			if (providers.contains(provider) == false) {
				this.errors.add(String.format("[%s] unsupported androidpush provider %s", caller, provider));
				isError = true;
			} else {
				value = res.getString("value");
			}
		}
		return new JSONObject().put("value", value).put("provider", provider).put("is_valid", !isError);
	}

	protected void addAndroidpush(String value, String provider, String caller) {
		JSONObject res = checkAndroidpushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_ANDROIDPUSH, res.getString("value"));
		this.dictAppend.put(KEY_PUSHVENDOR, res.getString("provider"));
	}

	protected void removeAndroidpush(String value, String provider, String caller) {
		JSONObject res = checkAndroidpushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_ANDROIDPUSH, res.getString("value"));
		this.dictRemove.put(KEY_PUSHVENDOR, res.getString("provider"));
	}

	// ------------------------ Iospush [providers: apns]

	private JSONObject checkIospushValue(String value, String provider, String caller) {
		JSONObject res = checkIdentValString(value, caller);
		//
		boolean isError = false;
		if (!res.getBoolean("is_valid")) {
			isError = true;
		} else {
			if (provider == null || provider.trim().isEmpty()) {
				provider = "apns";
			}
			provider = provider.trim();
			//
			List<String> providers = Arrays.asList("apns");
			if (providers.contains(provider) == false) {
				this.errors.add(String.format("[%s] unsupported iospush provider %s", caller, provider));
				isError = true;
			} else {
				value = res.getString("value");
			}
		}
		return new JSONObject().put("value", value).put("provider", provider).put("is_valid", !isError);
	}

	protected void addIospush(String value, String provider, String caller) {
		JSONObject res = checkIospushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_IOSPUSH, res.getString("value"));
		this.dictAppend.put(KEY_PUSHVENDOR, res.getString("provider"));
	}

	protected void removeIospush(String value, String provider, String caller) {
		JSONObject res = checkIospushValue(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_IOSPUSH, res.getString("value"));
		this.dictRemove.put(KEY_PUSHVENDOR, res.getString("provider"));
	}

	// ------------------------ Webpush [providers: vapid]

	private JSONObject checkWebpushDict(JSONObject value, String provider, String caller) {
		boolean isError = false;
		if (value == null || value.isEmpty()) {
			this.errors.add(String.format("[%s] value must be a valid json representing webpush-token", caller));
			isError = true;
		} else {
			if (provider == null || provider.trim().isEmpty()) {
				provider = "vapid";
			}
			provider = provider.trim();
			//
			List<String> providers = Arrays.asList("vapid");
			if (providers.contains(provider) == false) {
				this.errors.add(String.format("[%s] unsupported webpush provider %s", caller, provider));
				isError = true;
			}
		}
		JSONObject response = new JSONObject().put("value", value).put("provider", provider).put("is_valid", !isError);
		return response;
	}

	protected void addWebpush(JSONObject value, String provider, String caller) {
		JSONObject res = checkWebpushDict(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_WEBPUSH, res.getJSONObject("value"));
		this.dictAppend.put(KEY_PUSHVENDOR, res.getString("provider"));
	}

	protected void removeWebpush(JSONObject value, String provider, String caller) {
		JSONObject res = checkWebpushDict(value, provider, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_WEBPUSH, res.getJSONObject("value"));
		this.dictRemove.put(KEY_PUSHVENDOR, res.getString("provider"));
	}

	// ------------------------ Slack

	private JSONObject validateSlackUserId(String userId, String caller) {
		boolean isError = false;
		JSONObject res = checkIdentValString(userId, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			isError = true;
		} else {
			userId = userId.toUpperCase();
			if (!(userId.startsWith("U") || userId.startsWith("W"))) {
				this.errors.add(String.format("[%s] invalid value %s. Slack user/member_id starts with a U or W",
						caller, userId));
				isError = true;
				return new JSONObject().put("user_id", userId).put("is_valid", !isError);
			}
		}
		return new JSONObject().put("user_id", userId).put("is_valid", !isError);
	}

	private JSONObject checkSlackDict(JSONObject value, String caller) {
		String msg = "value must be a valid dict/json with one of these keys: [email, user_id]";

		boolean isError = false;
		if (value == null || value.isEmpty()) {
			this.errors.add(String.format("[%s] %s", caller, msg));
			isError = true;
		} else {
			String userId = value.getString("user_id");
			String email = value.getString("email");
			if (userId != null && !userId.trim().isEmpty()) {
				userId = userId.trim();
				JSONObject res = validateSlackUserId(userId, caller);
				boolean isValid = res.getBoolean("is_valid");
				if (!isValid) {
					isError = true;
				} else {
					userId = res.getString("user_id");
					value = new JSONObject().put("user_id", userId);
				}
			} else if (email != null && !email.trim().isEmpty()) {
				email = email.trim();
				JSONObject res = validateEmail(email, caller);
				boolean isValid = res.getBoolean("is_valid");
				if (!isValid) {
					isError = true;
				} else {
					email = res.getString("email");
					value = new JSONObject().put("email", email);
				}
			} else {
				this.errors.add(String.format("[%s] %s", caller, msg));
				isError = true;
			}
		}
		JSONObject response = new JSONObject().put("value", value).put("is_valid", !isError);
		return response;
	}

	protected void addSlack(JSONObject value, String caller) {
		JSONObject res = checkSlackDict(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictAppend.put(IDENT_KEY_SLACK, res.getJSONObject("value"));
	}

	protected void removeSlack(JSONObject value, String caller) {
		JSONObject res = checkSlackDict(value, caller);
		boolean isValid = res.getBoolean("is_valid");
		if (!isValid) {
			return;
		}
		this.dictRemove.put(IDENT_KEY_SLACK, res.getJSONObject("value"));
	}
}
