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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

public class EventCollector {
	String url;
	Suprsend config;
	public static List<String> RESERVED_EVENT_NAMES = Arrays.asList("$identify", "$notification_delivered",
			"$notification_dismiss", "$notification_clicked", "$app_launched", "$user_login", 
			"$user_logout");
	
	public EventCollector(Suprsend config) {
		this.config = config;
		this.url = getUrl();
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
		JSONObject headers = new JSONObject();
		ZoneId zone = ZoneId.of("UTC");
		LocalDateTime currentDateTime = LocalDateTime.now(zone);
		ZonedDateTime zonedCurrentDateTime = currentDateTime.atZone(zone);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss z");
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
	
	private void checkEventPrefix(String eventName) throws SuprsendException {
		if (EventCollector.RESERVED_EVENT_NAMES.contains(eventName) == false) {
			String prefixThreeChars = eventName.substring(0, 3).toLowerCase();
			if (prefixThreeChars.startsWith("$") || prefixThreeChars == "ss_") {
				throw new SuprsendException("eventName starting with [$,ss_] are reserved");
			}
		}
	}
	
	private String validateEventName(String eventName) throws SuprsendException {
		String strippedEventName = eventName.strip();
		checkEventPrefix(strippedEventName);
		return eventName;
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
	
	/**
	 * Validate data against the event JSON schema 
	 * @return
	 * 		Validated data
	 * @throws SuprsendException
	 */
	public JSONObject validateEventSchema(JSONObject data) throws SuprsendException {
		JSONObject jsonSchema;
		if(data.get("properties") == null) {
			data.put("properties", new JSONObject());
		}
		RequestSchema schema = new RequestSchema();
		jsonSchema = schema.getSchema("event");
		Schema schemaValidator = SchemaLoader.load(jsonSchema);
		try {
			schemaValidator.validate(data);
		} catch(ValidationException e) {
			throw new ValidationException(schemaValidator, e.getMessage());
		}
		return data;
	}
	
	public JSONObject collect(String distinctID, String eventName, JSONObject properties) throws SuprsendException {
		JSONObject event, superProps;
		UUID uuid = UUID.randomUUID();
		
		eventName = validateEventName(eventName);

		superProps = getSuperProperties();
		superProps.keys().forEachRemaining(key -> {
			properties.append(key, superProps.getString(key));
		});
		
		event = new JSONObject();
		event.put("$insert_id", uuid.toString());
		event.put("$time", Instant.now().getEpochSecond() * 1000);
		event.put("event", eventName);
		event.put("env", this.config.envKey);
		event.put("distinct_id", distinctID);
		event.put("properties", properties);
		
		JSONObject validatedEvent = validateEventSchema(event);
		
		return send(validatedEvent);
	}
	
	public JSONObject send(JSONObject event) {
		JSONObject headers, signatureResult, response;
		HttpURLConnection httpClient;
		String contentText;
		
		response = new JSONObject();
		try {
			headers = getHeaders();
			httpClient = (HttpURLConnection) new URL(this.url).openConnection();
			httpClient.setRequestMethod("POST");
			setMandatoryHeaders(httpClient, headers);
			if(this.config.authEnabled) {
				Signature signature = new Signature();
				signatureResult = signature.getRequestSignature(this.url, "POST", event, headers, this.config.envSecret);
				contentText = signatureResult.get("contentTxt").toString();
				httpClient.setRequestProperty("Authorization", String.format("%s:%s", this.config.envKey, signatureResult.get("signature").toString()));
			}
			else {
				contentText = event.toString();
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
		}
		catch (SuprsendException | IOException e) {
			response.put("success", false);
			response.put("status", "fail");
			response.put("status_code", 500);
			response.put("message", e.toString());
		}
		
		return response;
	}
}
