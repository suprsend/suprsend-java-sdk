package suprsend;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class BatchChunk {
	Suprsend config;
	List<JSONObject> chunk;
	String url;
	int runningSize, runningLength;
	JSONObject response;
	
	public BatchChunk(Suprsend config) {
		this.config = config;
		this.chunk = new ArrayList<JSONObject>();
		this.url = getUrl();
		this.runningSize = 0;
		this.runningLength = 0;
		this.response = new JSONObject();
	}
	
	/**
	 * Workflow backend URL
	 * @return
	 * 		Formatted workflow backend URL
	 */
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
		headers.put("Content-Type", "application/json");
		headers.put("User-Agent", userAgent);
		headers.put("Date", dateTimeFormat.format(zonedCurrentDateTime));
		return headers;
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
	
	private void addBodyToChunk(JSONObject body, int bodySize) {
		this.runningSize += bodySize;
		this.chunk.add(body);
		this.runningLength += 1;
	}
	
	private boolean checkLimitReached() {
		if (this.runningLength >= Constants.MAX_RECORDS_IN_BATCH || this.runningSize >= Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean tryToAddInfoChunk(JSONObject body) throws SuprsendException {
		int apparentBodySize;
		if (body.length() <= 0) {
			return true;
		}
		if (checkLimitReached()) {
			return false;
		}
		apparentBodySize = Utils.getApparentBodySize(body);
		if (apparentBodySize > Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES) {
			throw new SuprsendException(String.format("workflow body (discounting attachment "
					+ "if any) too big - %s Bytes, must not cross %s", apparentBodySize, Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES));
		}
		if (this.runningSize + apparentBodySize > Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES) {
			return false;
		}
		if(Constants.ALLOW_ATTACHMENTS_IN_BATCH == false) {
			body.getJSONObject("data").remove("$attachments");
		}
		addBodyToChunk(body, apparentBodySize);
		return true;
	}
	
	public void trigger() {
		String contentText;
		JSONObject signatureResult;
		JSONObject headers = getHeaders();
		JSONArray failureRecords;
		Boolean authEnabled = (Boolean)this.config.authEnabled;
		HttpURLConnection httpClient;
		try {
			httpClient = (HttpURLConnection) new URL(this.url).openConnection();
			httpClient.setRequestMethod("POST");
			setMandatoryHeaders(httpClient, headers);
			if (authEnabled) {
				Signature signature = new Signature();
				signatureResult = signature.getRequestSignature(this.url, "POST", this.chunk, headers, this.config.envSecret);
				contentText = signatureResult.get("contentTxt").toString();
				httpClient.setRequestProperty("Authorization", String.format("%s:%s", this.config.envKey, signatureResult.get("signature").toString()));
			}
			else {
				contentText = this.chunk.toString();
			}
			httpClient.setDoOutput(true);
			try(OutputStream stream = httpClient.getOutputStream()){
				byte[] input = contentText.getBytes(StandardCharsets.UTF_8);
				stream.write(input, 0, input.length);
			}
			int statusCode = httpClient.getResponseCode();
			if (statusCode == 202) {
				this.response.put("status", "success");
				this.response.put("status_code", statusCode);
				this.response.put("total", this.chunk.size());
				this.response.put("success", this.chunk.size());
				this.response.put("failure", 0);
				this.response.put("failed_records", new JSONArray());
			}
			else {
				String responseText = httpClient.getResponseMessage();
				failureRecords = new JSONArray();
				for (int i=0; i < this.chunk.size(); i++) {
					JSONObject record = new JSONObject();
					record.put("record", this.chunk.get(i));
					record.put("error", responseText);
					record.put("code", statusCode);
					failureRecords.put(record);
				}
				
				this.response.put("status", "fail");
				this.response.put("status_code", statusCode);
				this.response.put("total", this.chunk.size());
				this.response.put("success", 0);
				this.response.put("failure", this.chunk.size());
				this.response.put("failed_records", failureRecords);
			}
		} catch (Exception e) {
			String errorText = e.toString();
			failureRecords = new JSONArray();
			for (int i=0; i < this.chunk.size(); i++) {
				JSONObject record = new JSONObject();
				record.put("record", this.chunk.get(i));
				record.put("error", errorText);
				record.put("code", 500);
				failureRecords.put(record);
			}
			
			this.response.put("status", "fail");
			this.response.put("status_code", 500);
			this.response.put("total", this.chunk.size());
			this.response.put("success", 0);
			this.response.put("failure", this.chunk.size());
			this.response.put("failed_records", failureRecords);
		} 
	}
}
