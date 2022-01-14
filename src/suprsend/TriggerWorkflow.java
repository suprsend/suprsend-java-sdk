package suprsend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.ValidationException;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

class TriggerWorkflow {
	Suprsend config;
	JSONObject data;
	String url;
	TriggerWorkflow(Suprsend config, JSONObject data) {
		this.config = config;
		this.data = data;
		this.url = getUrl();
	}
	
	private String getUrl() {
		String urlTemplate = "%s%s/trigger/";
		String baseUrl = this.config.baseUrl;
		String envKey = this.config.envKey;
		Boolean includeSignatureParam = this.config.includeSignatureParam;
		Boolean authEnabled = this.config.authEnabled;
		if(includeSignatureParam) {
			if(authEnabled) {
				urlTemplate = urlTemplate + "?verify=true";
			}
			else {
				urlTemplate = urlTemplate + "?verify=false";
			}
		}
		String urlFormatted = String.format(urlTemplate, baseUrl, envKey);
		return urlFormatted;
	}
	
	private JSONObject getHeaders() {
		String userAgent = this.config.userAgent;
		ZoneId zone = ZoneId.of("UTC");
		LocalDateTime currentDateTime = LocalDateTime.now(zone);
		ZonedDateTime zonedCurrentDateTime = currentDateTime.atZone(zone);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss z");
		JSONObject headers = new JSONObject();
		headers.put("Content-Type", "application/json");
		headers.put("User-Agent", userAgent);
		headers.put("Date", dateTimeFormat.format(zonedCurrentDateTime));
		return headers;
	}
	
	private void setMandatoryHeaders(HttpURLConnection httpClient, JSONObject headers) {
		httpClient.setRequestProperty("Content-Type", headers.get("Content-Type").toString());
		httpClient.setRequestProperty("User-Agent", headers.get("User-Agent").toString());
		httpClient.setRequestProperty("Date", headers.get("Date").toString());
	}
	
	public JSONObject executeWorkflow() throws Exception {
		JSONObject signatureResult;
		JSONObject response = new JSONObject();
		JSONObject headers = getHeaders();
		Boolean authEnabled = (Boolean)this.config.authEnabled;
		String contentText;
		HttpURLConnection httpClient = (HttpURLConnection) new URL(this.url).openConnection();
		httpClient.setRequestMethod("POST");
		setMandatoryHeaders(httpClient, headers);
		if (authEnabled) {
			Signature signature = new Signature();
			signatureResult = signature.getRequestSignature(this.url, "POST", this.data, headers, this.config.envSecret);
			contentText = signatureResult.get("contentTxt").toString();
			httpClient.setRequestProperty("Authorization", String.format("%s:%s", this.config.envKey, signatureResult.get("signature").toString()));
		}
		else {
			contentText = this.data.toString();
		}
		httpClient.setDoOutput(true);
		
		try(OutputStream stream = httpClient.getOutputStream()){
			byte[] input = contentText.getBytes(StandardCharsets.UTF_8);
			stream.write(input, 0, input.length);
		}
		int statusCode = httpClient.getResponseCode();
		String responseText = httpClient.getResponseMessage();
		Boolean success = false;
		if(Math.floorDiv(statusCode, 2) == 2) {
			success = true;
		}
		response.put("success", success);
		response.put("status", statusCode);
		response.put("message", responseText);
		return response;
	}
	
	public JSONObject validateData() throws org.everit.json.schema.ValidationException, IOException, ValidationException {
		JSONObject jsonSchema;
		if(this.data.get("data") == null) {
			this.data.put("data", new JSONObject());
		}
		RequestSchema schema = new RequestSchema();
		jsonSchema = schema.getSchema("workflow");
		Schema schemaValidator = SchemaLoader.load(jsonSchema);
		try {
			schemaValidator.validate(this.data);
		} catch(org.everit.json.schema.ValidationException e) {
			throw new org.everit.json.schema.ValidationException(schemaValidator, e.getMessage());
		}
		return this.data;
	}
}
