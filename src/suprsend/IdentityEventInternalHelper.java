package suprsend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class IdentityEventInternalHelper {
	public static final String IDENT_KEY_EMAIL = "$email";
	public static final String IDENT_KEY_SMS = "$sms";
	public static final String IDENT_KEY_ANDROIDPUSH = "$androidpush";
	public static final String IDENT_KEY_IOSPUSH = "$iospush";
	public static final String IDENT_KEY_WHATSAPP = "$whatsapp";
	public static final String IDENT_KEY_WEBPUSH = "$webpush";
	public static final String KEY_PUSHVENDOR = "$pushvendor";
	
	List<String> identKeysAll, otherReservedKeys, superPropertyKeys, allReservedKeys;

	String distinctID, worspaceKey;
	JSONObject dictAppend, dictRemove;
	int appendCount, removeCount, unsetCount;
	List<String> listUnset, errors, info;
	
	public IdentityEventInternalHelper(String distinctID, String worspaceKey) {
		initializeConstants();
		this.distinctID = distinctID;
		this.worspaceKey = worspaceKey;
		this.dictAppend = new JSONObject();
		this.appendCount = 0;

		this.dictRemove = new JSONObject();
		this.removeCount = 0;

		this.listUnset = new ArrayList<String>();
		this.unsetCount = 0;

		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
	}

	private void reset() {
		this.dictAppend.keySet().clear();
		this.dictRemove.keySet().clear();
		this.listUnset.clear();

		this.appendCount = 0;
		this.removeCount = 0;
		this.unsetCount = 0;

		this.errors.clear();
		this.info.clear();
	}
	
	private void initializeConstants() {
		this.identKeysAll = Arrays.asList(IDENT_KEY_EMAIL, IDENT_KEY_SMS, IDENT_KEY_ANDROIDPUSH, 
				IDENT_KEY_IOSPUSH, IDENT_KEY_WHATSAPP, IDENT_KEY_WEBPUSH);
		
		this.otherReservedKeys = Arrays.asList("$messenger", "$inbox", KEY_PUSHVENDOR,
				"$device_id", "$insert_id", "$time", "$set", "$set_once", "$add", "$append",
				"$remove", "$unset", "$identify", "$anon_id", "$identified_id", "$notification_delivered",
				"$notification_dismiss", "$notification_clicked");
		
		this.superPropertyKeys = Arrays.asList("$app_version_string", "$app_build_number",
				"$brand", "$carrier", "$manufacturer", "$model", "$os", "$ss_sdk_version",
				"$insert_id", "$time");
		
		this.allReservedKeys = new ArrayList<String>();
		
		this.allReservedKeys.addAll(this.identKeysAll);
		this.allReservedKeys.addAll(this.otherReservedKeys);
		this.allReservedKeys.addAll(this.superPropertyKeys);
		
	}
	
	public JSONObject getIdentityEvent() {
		JSONObject retValue = new JSONObject();
		JSONObject event = fromEvent();
		retValue.put("errors", this.errors);
		retValue.put("info", this.info);
		retValue.put("event", event);
		retValue.put("append", this.appendCount);
		retValue.put("remove", this.removeCount);
		retValue.put("unset", this.unsetCount);

		reset();

		return retValue;
	}

	private JSONObject fromEvent() {
		UUID uuid = UUID.randomUUID();

		if (this.dictAppend != null || this.dictRemove != null || this.listUnset != null) {
			JSONObject event = new JSONObject();

			event.put("$insert_id", uuid.toString());
			event.put("$time", Instant.now().getEpochSecond() * 1000);
			event.put("env", this.worspaceKey);
			event.put("distinct_id", this.distinctID);
			
			if (this.dictAppend != null) {
				event.put("$append", this.dictAppend);
				this.appendCount = this.appendCount + 1;
				event.put("$remove", this.dictRemove);
				this.removeCount = this.removeCount + 1;
				event.put("$unset", this.listUnset);
				this.unsetCount = this.unsetCount + 1;
			}

			return event;
		}

		return null;
	}
	
	private JSONObject validateKeyBasic(String key, String caller) {
		JSONObject response = new JSONObject();
		if (key!=null && key instanceof String) {
			response.put("key", key);
			response.put("status", true);
		}
		else {
			if (key == null) {
				this.info.add(String.format("[%s] skipping key: empty string", caller));
			}
			else {
				this.info.add(String.format("[%s] skipping key: %s. key must be a string", caller, key));
			}
			response.put("key", key);
			response.put("status", false);
		}
		return response;
	}
	
	private Boolean validateKeyPrefix(String key, String caller) {
		if (this.allReservedKeys.contains(key)) {
			String prefixThreeChars = key.substring(0, 3).toLowerCase();
			if (prefixThreeChars.startsWith("$") || prefixThreeChars == "ss_") {
				this.info.add(String.format("[%s] skipping key: %s, key starting with [$, ss_] are reserved", caller, key));
				return false;
			}
		}
		return true;
	}
	
	private Boolean isIdentitykey(String key) {
		if (this.identKeysAll.contains(key)) {
			return true;
		}
		return false;
	}
	
	public void appendKV(String key, String value, JSONObject kwargs, String caller) {
		JSONObject validatedResponse = validateKeyBasic(key, caller);
		String validatedKey = validatedResponse.get("key").toString();
		Boolean isKeyValid = (Boolean)validatedResponse.get("status");
		if (isKeyValid) {
			if (isIdentitykey(validatedKey)) {
				addIdentity(validatedKey, value, kwargs, caller);
			}
			else {
			    Boolean isKValid = validateKeyPrefix(validatedKey, caller);
			    if(isKValid) {
			    	this.dictAppend.put(validatedKey, value);
			    }
			}
		}
	}
	
	public void appendKV(String key, JSONObject value, JSONObject kwargs, String caller) {
		JSONObject validatedResponse = validateKeyBasic(key, caller);
		String validatedKey = validatedResponse.getString("key");
		Boolean isKeyValid = (Boolean)validatedResponse.get("status");
		if (isKeyValid) {
			if (isIdentitykey(validatedKey)) {
				addIdentity(validatedKey, value, kwargs, caller);
			}
			else {
			    Boolean isKValid = validateKeyPrefix(validatedKey, caller);
			    if(isKValid) {
			    	this.dictAppend.put(validatedKey, value);
			    }
			}
		}
	}
	
	
	public void removeKV(String key, String value, JSONObject kwargs, String caller) {
		JSONObject validatedResponse = validateKeyBasic(key, caller);
		String validatedKey = validatedResponse.getString("key");
		Boolean isKeyValid = (Boolean)validatedResponse.get("status");
		if (isKeyValid) {
			if (isIdentitykey(validatedKey)) {
				removeIdentity(validatedKey, value, kwargs, caller);
			}
			else {
			    Boolean isKValid = validateKeyPrefix(validatedKey, caller);
			    if(isKValid) {
			    	this.dictRemove.put(validatedKey, value);
			    }
			}
		}
	}
	
	public void removeKV(String key, JSONObject value, JSONObject kwargs, String caller) {
		JSONObject validatedResponse = validateKeyBasic(key, caller);
		String validatedKey = validatedResponse.getString("key");
		Boolean isKeyValid = (Boolean)validatedResponse.get("status");
		if (isKeyValid) {
			if (isIdentitykey(validatedKey)) {
				removeIdentity(validatedKey, value, kwargs, caller);
			}
			else {
			    Boolean isKValid = validateKeyPrefix(validatedKey, caller);
			    if(isKValid) {
			    	this.dictRemove.put(validatedKey, value);
			    }
			}
		}
	}
	
	public void unsetK(String key, String caller) {
		JSONObject validatedResponse = validateKeyBasic(key, caller);
		String validatedKey = validatedResponse.getString("key");
		Boolean isKeyValid = (Boolean)validatedResponse.get("status");
		if (isKeyValid) {
			this.listUnset.add(validatedKey);
		}
	}
	
	
	private void addIdentity(String key, String value, JSONObject kwargs, String caller) {
		if (key == IDENT_KEY_EMAIL) {
			addEmail(value, caller);
		}
		else if (key == IDENT_KEY_SMS) {
			addSMS(value, caller);
		}
		else if (key == IDENT_KEY_WHATSAPP) {
			addWhatsapp(value, caller);
		}
		else if (key == IDENT_KEY_ANDROIDPUSH) {
			addAndroidPush(value, kwargs.getString(KEY_PUSHVENDOR), caller);
			if (this.dictAppend.get(KEY_PUSHVENDOR) != null) {
				kwargs.put(KEY_PUSHVENDOR, this.dictAppend.get(KEY_PUSHVENDOR));
			}
		}
		else if (key ==	IDENT_KEY_IOSPUSH) {
			addIosPush(value, kwargs.getString(KEY_PUSHVENDOR), caller);
			if (this.dictAppend.get(KEY_PUSHVENDOR) != null) {
				kwargs.put(KEY_PUSHVENDOR, this.dictAppend.get(KEY_PUSHVENDOR));
			}
		}
	}
	
	private void addIdentity(String key, JSONObject value, JSONObject kwargs, String caller) {
		if (key == IDENT_KEY_WEBPUSH) {
			addWebPush(value, kwargs.getString(KEY_PUSHVENDOR), caller);
			if (this.dictAppend.get(KEY_PUSHVENDOR) != null) {
				kwargs.put(KEY_PUSHVENDOR, this.dictAppend.get(KEY_PUSHVENDOR));
			}
		}
	}
	
	private void removeIdentity(String key, String value, JSONObject kwargs, String caller) {
		if (key == IDENT_KEY_EMAIL) {
			removeEmail(value, caller);
		}
		else if (key == IDENT_KEY_SMS) {
			removeSMS(value, caller);
		}
		else if (key == IDENT_KEY_WHATSAPP) {
			removeWhatsapp(value, caller);
		}
		else if (key == IDENT_KEY_ANDROIDPUSH) {
			removeAndroidPush(value, kwargs.getString(KEY_PUSHVENDOR), caller);
			if (this.dictRemove.get(KEY_PUSHVENDOR) != null) {
				kwargs.put(KEY_PUSHVENDOR, this.dictRemove.get(KEY_PUSHVENDOR));
			}
		}
		else if (key ==	IDENT_KEY_IOSPUSH) {
			removeIosPush(value, kwargs.getString(KEY_PUSHVENDOR), caller);
			if (this.dictRemove.get(KEY_PUSHVENDOR) != null) {
				kwargs.put(KEY_PUSHVENDOR, this.dictRemove.get(KEY_PUSHVENDOR));
			}
		}
	}
	
	private void removeIdentity(String key, JSONObject value, JSONObject kwargs, String caller) {
		if (key == IDENT_KEY_WEBPUSH) {
			removeWebPush(value, kwargs.getString(KEY_PUSHVENDOR), caller);
			if (this.dictRemove.get(KEY_PUSHVENDOR) != null) {
				kwargs.put(KEY_PUSHVENDOR, this.dictRemove.get(KEY_PUSHVENDOR));
			}
		}
	}
	
	private JSONObject checkIdentValString(String value, String caller) {
		JSONObject response = new JSONObject();
		String msg = "value must a string with proper value";
		if (value != null && value instanceof String) {
			response.put("value", value);
			response.put("status", true);
		}
		else {
			this.errors.add(String.format("[%s] %s", caller, msg));
			response.put("value", value);
			response.put("status", false);
		}
		return response;
	}
	
	private JSONObject validateEmail(String email, String caller) {
		JSONObject response = new JSONObject();
		JSONObject identValStringResponse = checkIdentValString(email, caller);
		String validatedEmail = identValStringResponse.get("value").toString();
		Boolean isValid = (Boolean)identValStringResponse.get("status");
		if (isValid == false) {
			response.put("email", validatedEmail);
			response.put("status", false);
			return response;
		}
		String msg = "value in email format required. e.g. user@example.com";
		int minLength = 6;
		int maxLength = 127;
		
		String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" 
		        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
		Boolean isValidEmail = Pattern.compile(emailRegex).matcher(validatedEmail).matches();
		
		if (isValidEmail == false) {
			this.errors.add(String.format("[%s] invalid value %s. %s", caller, validatedEmail, msg));
			response.put("email", validatedEmail);
			response.put("status", false);
			return response;
		}
		
		if (validatedEmail.length() < minLength || validatedEmail.length() > maxLength) {
			this.errors.add(String.format("[%s] invalid value %s. must be 6 <= len(email) <= 127", caller, validatedEmail, msg));
			response.put("email", validatedEmail);
			response.put("status", false);
			return response;
		}
		response.put("email", validatedEmail);
		response.put("status", true);
		return response;
	}
	
	public void addEmail(String value, String caller) {
		JSONObject validatedEmailResponse = validateEmail(value, caller);
		String validatedEmail = validatedEmailResponse.get("email").toString();
		Boolean isValid = (Boolean)validatedEmailResponse.get("status");
		if (isValid == true) {
			this.dictAppend.put(IDENT_KEY_EMAIL, validatedEmail); 
		}
	}
	
	
	public void removeEmail(String value, String caller) {
		JSONObject validatedEmailResponse = validateEmail(value, caller);
		String validatedEmail = validatedEmailResponse.get("email").toString();
		Boolean isValid = (Boolean)validatedEmailResponse.get("status");
		if (isValid == true) {
			this.dictRemove.put(IDENT_KEY_EMAIL, validatedEmail); 
		}
	}
	
	private JSONObject validateMobileNo(String mobileNo, String caller) {
		JSONObject response = new JSONObject();
		JSONObject identValStringResponse = checkIdentValString(mobileNo, caller);
		String validatedMobileNo = identValStringResponse.get("value").toString();
		Boolean isValid = (Boolean)identValStringResponse.get("status");
		if (isValid == false) {
			response.put("mobileNo", validatedMobileNo);
			response.put("status", false);
			return response;
		}
		String msg = "number must start with + and must contain country code. e.g. +41446681800";
		int minLength = 8;
		
		String mobileRegex = "^\\+[0-9\\s]+";
		Boolean isValidMobileNo = Pattern.compile(mobileRegex).matcher(validatedMobileNo).matches();
		if (isValidMobileNo == false) {
			this.errors.add(String.format("[%s] invalid value %s. %s", caller, validatedMobileNo, msg));
			response.put("mobile", isValidMobileNo);
			response.put("status", false);
			return response;
		}
		
		if (validatedMobileNo.length() < minLength) {
			this.errors.add(String.format("[%s] invalid value %s. len(mobile_no) must be >= 8", caller, validatedMobileNo, msg));
			response.put("mobile", validatedMobileNo);
			response.put("status", false);
			return response;
		}
		response.put("mobile", validatedMobileNo);
		response.put("status", true);
		return response;
	}
	
	public void addSMS(String value, String caller) {
		JSONObject validatedMobileResponse = validateMobileNo(value, caller);
		String validatedMobileNo = validatedMobileResponse.get("mobile").toString();
		Boolean isValid = (Boolean)validatedMobileResponse.get("status");
		if (isValid == true) {
			this.dictAppend.put(IDENT_KEY_SMS, validatedMobileNo); 
		}
	}
	
	public void removeSMS(String value, String caller) {
		JSONObject validatedMobileResponse = validateMobileNo(value, caller);
		String validatedMobileNo = validatedMobileResponse.get("mobile").toString();
		Boolean isValid = (Boolean)validatedMobileResponse.get("status");
		if (isValid == true) {
			this.dictRemove.put(IDENT_KEY_SMS, validatedMobileNo); 
		}
	}
	
	public void addWhatsapp(String value, String caller) {
		JSONObject validatedMobileResponse = validateMobileNo(value, caller);
		String validatedMobileNo = validatedMobileResponse.get("mobile").toString();
		Boolean isValid = (Boolean)validatedMobileResponse.get("status");
		if (isValid == true) {
			this.dictAppend.put(IDENT_KEY_WHATSAPP, validatedMobileNo); 
		}
	}
	
	public void removeWhatsapp(String value, String caller) {
		JSONObject validatedMobileResponse = validateMobileNo(value, caller);
		String validatedMobileNo = validatedMobileResponse.get("mobile").toString();
		Boolean isValid = (Boolean)validatedMobileResponse.get("status");
		if (isValid == true) {
			this.dictRemove.put(IDENT_KEY_WHATSAPP, validatedMobileNo); 
		}
	}
	
	private JSONObject checkAndroidPushValue(String value, String provider, String caller) {
		JSONObject response = new JSONObject();
		JSONObject identValStringResponse = checkIdentValString(value, caller);
		
		List<String> providers = Arrays.asList("fcm", "xiaomi", "oppo");
		
		String validatedValue = identValStringResponse.get("value").toString();
		Boolean isValid = (Boolean)identValStringResponse.get("status");
		if (isValid == false) {
			response.put("value", validatedValue);
			response.put("provider", provider);
			response.put("status", false);
			return response;
		}
		
		if (provider == null) {
			provider = "fcm";
		}
		
		if (providers.contains(provider) == false) {
			this.errors.add(String.format("[%s] unsupported androidpush provider %s", caller, provider));
			response.put("value", validatedValue);
			response.put("provider", provider);
			response.put("status", false);
			return response;
		}
		response.put("value", validatedValue);
		response.put("provider", provider);
		response.put("status", true);
		
		return response;
	}
	
	public void addAndroidPush(String value, String provider, String caller) {
		JSONObject validatedAndroidPushResponse = checkAndroidPushValue(value, provider, caller);
		String validatedValue = validatedAndroidPushResponse.get("value").toString();
		String validatedProvider = validatedAndroidPushResponse.get("provider").toString();
		Boolean isValid = (Boolean)validatedAndroidPushResponse.get("status");
		if (isValid == true) {
			this.dictAppend.put(IDENT_KEY_ANDROIDPUSH, validatedValue);
			this.dictAppend.put(KEY_PUSHVENDOR, validatedProvider);
		}
	}
	
	public void removeAndroidPush(String value, String provider, String caller) {
		JSONObject validatedAndroidPushResponse = checkAndroidPushValue(value, provider, caller);
		String validatedValue = validatedAndroidPushResponse.get("value").toString();
		String validatedProvider = validatedAndroidPushResponse.get("provider").toString();
		Boolean isValid = (Boolean)validatedAndroidPushResponse.get("status");
		if (isValid == true) {
			this.dictRemove.put(IDENT_KEY_ANDROIDPUSH, validatedValue);
			this.dictRemove.put(KEY_PUSHVENDOR, validatedProvider);
		}
	}
	
	private JSONObject checkIosPushValue(String value, String provider, String caller) {
		JSONObject response = new JSONObject();
		JSONObject identValStringResponse = checkIdentValString(value, caller);
		
		List<String> providers = Arrays.asList("apns");
		
		String validatedValue = identValStringResponse.get("value").toString();
		Boolean isValid = (Boolean)identValStringResponse.get("status");
		
		if (isValid == false) {
			response.put("value", validatedValue);
			response.put("provider", provider);
			response.put("status", false);
			return response;
		}
		
		if (provider == null) {
			provider = "apns";
		}
		
		if (providers.contains(provider) == false) {
			this.errors.add(String.format("[%s] unsupported iospush provider %s", caller, provider));
			response.put("value", validatedValue);
			response.put("provider", provider);
			response.put("status", false);
			return response;
		}
		response.put("value", validatedValue);
		response.put("provider", provider);
		response.put("status", true);
		
		return response;
	}

	public void addIosPush(String value, String provider, String caller) {
		JSONObject validatedIosPushResponse = checkIosPushValue(value, provider, caller);
		String validatedValue = validatedIosPushResponse.get("value").toString();
		String validatedProvider = validatedIosPushResponse.get("provider").toString();
		Boolean isValid = (Boolean)validatedIosPushResponse.get("status");
		if (isValid == true) {
			this.dictAppend.put(IDENT_KEY_IOSPUSH, validatedValue);
			this.dictAppend.put(KEY_PUSHVENDOR, validatedProvider);
		}
	}
	
	public void removeIosPush(String value, String provider, String caller) {
		JSONObject validatedIosPushResponse = checkIosPushValue(value, provider, caller);
		String validatedValue = validatedIosPushResponse.get("value").toString();
		String validatedProvider = validatedIosPushResponse.get("provider").toString();
		Boolean isValid = (Boolean)validatedIosPushResponse.get("status");
		if (isValid == true) {
			this.dictRemove.put(IDENT_KEY_IOSPUSH, validatedValue);
			this.dictRemove.put(KEY_PUSHVENDOR, validatedProvider);
		}
	}

	private JSONObject checkWebPushdict(JSONObject value, String provider, String caller) {
		JSONObject response = new JSONObject();
		if (provider == null) {
			provider = "vapid";
		}
		
		List<String> providers = Arrays.asList("vapid");
		
		if (providers.contains(provider) == false) {
			this.errors.add(String.format("[%s] unsupported webpush provider %s", caller, provider));
			response.put("value", value);
			response.put("provider", provider);
			response.put("status", false);
			return response;
		}
		response.put("value", value);
		response.put("provider", provider);
		response.put("status", true);
		return response;
	}

	public void addWebPush(JSONObject value, String provider, String caller) {
		JSONObject validatedWebPushResponse = checkWebPushdict(value, provider, caller);
		String validatedValue = validatedWebPushResponse.get("value").toString();
		String validatedProvider = validatedWebPushResponse.get("provider").toString();
		Boolean isValid = (Boolean)validatedWebPushResponse.get("status");
		if (isValid == true) {
			this.dictAppend.put(IDENT_KEY_WEBPUSH, validatedValue);
			this.dictAppend.put(KEY_PUSHVENDOR, validatedProvider);
		}
	}
	
	public void removeWebPush(JSONObject value, String provider, String caller) {
		JSONObject validatedWebPushResponse = checkWebPushdict(value, provider, caller);
		String validatedValue = validatedWebPushResponse.get("value").toString();
		String validatedProvider = validatedWebPushResponse.get("provider").toString();
		Boolean isValid = (Boolean)validatedWebPushResponse.get("status");
		if (isValid == true) {
			this.dictRemove.put(IDENT_KEY_WEBPUSH, validatedValue);
			this.dictRemove.put(KEY_PUSHVENDOR, validatedProvider);
		}
	}

}
