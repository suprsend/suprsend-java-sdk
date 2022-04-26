package suprsend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserIdentity {
	Suprsend config;
	String distinctID, url;
	List<String> errors, info;
	List<JSONObject> events;
	int appendCount, removeCount, unsetCount;
	IdentityEventInternalHelper helper;
	JSONObject suprProperties;
	
	public UserIdentity(Suprsend config, String distinctID) {
		this.config = config;
		this.distinctID = distinctID;
		this.url = getUrl();
		this.suprProperties = getSuperProperties();
		
		this.errors = new ArrayList<String>();
		this.info = new ArrayList<String>();
		this.events = new ArrayList<JSONObject>();
		
		appendCount = removeCount = unsetCount = 0;
		
		helper = new IdentityEventInternalHelper(this.distinctID, this.config.envKey);
	}
	
	private String getUrl() {
		String urlTemplate = "%sevent/";
		if (this.config.includeSignatureParam) {
			if (this.config.authEnabled) {
				urlTemplate = urlTemplate + "?verify=true";
			}
			else {
				urlTemplate = urlTemplate + "?verify=false";
			}
		}
		String urlFormatted = String.format(urlTemplate, this.config.baseUrl);
		return urlFormatted;
	}
	
	/**
	 * Headers required to trigger workflow request
	 * @return
	 * 		Headers as JSON object
	 */
	private JSONObject getHeaders() {
		String userAgent = this.config.userAgent;
		ZoneId zone = ZoneId.of("UTC");
		LocalDateTime currentDateTime = LocalDateTime.now(zone);
		ZonedDateTime zonedCurrentDateTime = currentDateTime.atZone(zone);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss z");
		JSONObject headers = new JSONObject();
		headers.put("Content-Type", "application/json; charset=utf-8");
		headers.put("User-Agent", userAgent);
		headers.put("Date", dateTimeFormat.format(zonedCurrentDateTime));
		return headers;
	}
	
	private JSONObject getSuperProperties() {
		JSONObject response = new JSONObject();
		response.put("$ss_sdk_version", this.config.userAgent);
		return response;
	}
	
	public List<String> getWarnings() {
		return this.info;
	}
	
	public List<String> getErrors() {
		return this.errors;
	}
	
	public void setEvents() {
		for(int i = 0; i < this.events.size(); i++) {
			this.events.get(i).put("properties", this.suprProperties);
		}
		
		if (this.appendCount > 0) {
			JSONObject userIdentifyEvent = new JSONObject();
			JSONObject properties = new JSONObject();
			UUID uuid = UUID.randomUUID();
			
			properties.put("$anon_id", this.distinctID);
			properties.put("$identified_id", this.distinctID);
			properties.put("$ss_sdk_version", this.config.userAgent);
			
			userIdentifyEvent.put("$insert_id", uuid.toString());
			userIdentifyEvent.put("$time", Instant.now().getEpochSecond() * 1000);
			userIdentifyEvent.put("env", this.config.envKey);
			userIdentifyEvent.put("event", "$identify");
			userIdentifyEvent.put("properties", properties);
			
			this.events.add(userIdentifyEvent);
		}
	}
	
	private void validateBody() throws SuprsendException {
		if(this.info.size() > 0) {
			System.out.println(String.format("WARNING: %s", String.join("\n", this.info)));
		}
		
		if(this.errors.size() > 0) {
			throw new SuprsendException(String.format("Error: %s", String.join("\n", this.errors)));
		}
		
		if(this.events.size() <= 0) {
			throw new SuprsendException("ERROR: no user properties have been edited. "
					+ "Use user.append/remove/unset method to update user properties");
		}
	}
	
	/**
	 * Set HTTP headers in HTTP client object
	 * @param httpClient
	 * 		  HTTP client object
	 * @param headers
	 * 		  Headers in JSON format
	 */
	private void setMandatoryHeaders(HttpURLConnection httpClient, JSONObject headers) {
		httpClient.setRequestProperty("Content-Type", headers.get("Content-Type").toString());
		httpClient.setRequestProperty("User-Agent", headers.get("User-Agent").toString());
		httpClient.setRequestProperty("Date", headers.get("Date").toString());
	}
	
	public JSONObject save() {
		JSONObject headers, signatureResult, response;
		HttpURLConnection httpClient;
		String contentText;
		response = new JSONObject();
		try {
			validateBody();
			headers = getHeaders();
			setEvents();
			httpClient = (HttpURLConnection) new URL(this.url).openConnection();
			httpClient.setRequestMethod("POST");
			setMandatoryHeaders(httpClient, headers);
			if(this.config.authEnabled) {
				Signature signature = new Signature();
				signatureResult = signature.getRequestSignature(this.url, "POST", this.events, headers, this.config.envSecret);
				contentText = signatureResult.get("contentTxt").toString();
				httpClient.setRequestProperty("Authorization", String.format("%s:%s", this.config.envKey, signatureResult.get("signature").toString()));
			}
			else {
				contentText = this.events.toString();
			}
			httpClient.setDoOutput(true);
			try(OutputStream stream = httpClient.getOutputStream()){
				byte[] input = contentText.getBytes(StandardCharsets.UTF_8);
				stream.write(input, 0, input.length);
			}
			int statusCode = httpClient.getResponseCode();
			String responseText = httpClient.getResponseMessage();
			if (statusCode == 202) {
				response.put("success", true);
				response.put("status", "success");
				response.put("status_code", statusCode);
				response.put("message", responseText);
			}
			else {
				response.put("success", false);
				response.put("status", "fail");
				response.put("status_code", statusCode);
				response.put("message", responseText);
			}			
		} catch (SuprsendException | IOException e) {
			response.put("success", false);
			response.put("status", "fail");
			response.put("status_code", 500);
			response.put("message", e.toString());
		}
		
		return response;
	}
	
	private void collectEvent() {
		JSONObject response = this.helper.getIdentityEvent();
		JSONArray errors = response.getJSONArray("errors");
		JSONArray info = response.getJSONArray("info");
		JSONObject event = response.getJSONObject("event");
		if (errors.length() > 0) {
			for(int i=0; i < errors.length(); i++) {
				this.errors.add(errors.get(i).toString());
			}
		}
		if (info.length() > 0) {
			for(int i=0; i < info.length(); i++) {
				this.errors.add(info.get(i).toString());
			}
		}
		if (event.length() > 0) {
			this.events.add(event);
			this.appendCount = this.appendCount + response.getInt("append");
			this.removeCount = this.removeCount + response.getInt("remove");
			this.unsetCount = this.unsetCount + response.getInt("unset");
		}
		
	}
	
	public void append(JSONObject arg1) {
		String caller = "append";
		Iterator<String> keys = arg1.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			if (arg1.get(key) instanceof String) {
				this.helper.appendKV(key, arg1.getString(key), new JSONObject(), caller);
			}
			else {
				this.helper.appendKV(key, arg1.getJSONObject(key), new JSONObject(), caller);
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
	
	public void remove(JSONObject arg1) {
		String caller = "remove";
		Iterator<String> keys = arg1.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			if (arg1.get(key) instanceof String) {
				this.helper.removeKV(key, arg1.getString(key), new JSONObject(), caller);
			}
			else {
				this.helper.removeKV(key, arg1.getJSONObject(key), new JSONObject(), caller);
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
	
	public void unset(String key) {
		String caller = "unset";
		this.helper.unsetK(key, caller);
		collectEvent();
	}
	
	public void unset(List<String> key) {
		String caller = "unset";
		for (int i=0; i < key.size(); i++) {
			this.helper.unsetK(key.get(i), caller);
		}
		collectEvent();
	}
	
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
	
	public void addSMS(String value) {
		String caller = "add_sms";
		this.helper.addSMS(value, caller);
		collectEvent();
	}
	
	public void removeSMS(String value) {
		String caller = "remove_sms";
		this.helper.removeSMS(value, caller);
		collectEvent();
	}
	
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
	
	public void addAndroidPush(String value, String provider) {
		String caller = "add_androidpush";
		this.helper.addAndroidPush(value, provider, caller);
		collectEvent();
	}
	
	public void removeAndroidPush(String value, String provider) {
		String caller = "remove_androidpush";
		this.helper.removeAndroidPush(value, provider, caller);
		collectEvent();
	}
	
	public void addIOSPush(String value, String provider) {
		String caller = "add_iospush";
		this.helper.addIosPush(value, provider, caller);
		collectEvent();
	}
	
	public void removeIOSPush(String value, String provider) {
		String caller = "remove_iospush";
		this.helper.removeIosPush(value, provider, caller);
		collectEvent();
	}
	
	public void addWebPush(JSONObject value, String provider) {
		String caller = "add_webpush";
		this.helper.addWebPush(value, provider, caller);
		collectEvent();
	}
	
	public void removeWebPush(JSONObject value, String provider) {
		String caller = "remove_webpush";
		this.helper.removeWebPush(value, provider, caller);
		collectEvent();
	}
}
