package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.JSONObject;

public class ObjectsApi {
	private static final Logger logger = Logger.getLogger(TenantsApi.class.getName());

	private Suprsend config;
	private String listUrl;
	private String bulkUrl;

	ObjectsApi(Suprsend config) {
		this.config = config;
		this.listUrl = String.format("%sv1/object/", this.config.baseUrl);
		this.bulkUrl = String.format("%sv1/bulk/object/", this.config.baseUrl);
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	public JSONObject list(String objectType) throws IOException, SuprsendException {
		HashMap<String, Object> opts = new HashMap<String, Object>();
		return list(objectType, opts);
	}

	public JSONObject list(String objectType, HashMap<String, Object> opts) throws IOException, SuprsendException {
		String encodedParams = Utils.buildQueryParams(opts);
		String url = String.format("%s%s/?%s", this.listUrl, objectType, encodedParams);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	private String validateObjectEntityId(String entityId) throws SuprsendException {
		if (entityId == null || entityId.trim().isEmpty()) {
			throw new SuprsendException("missing object entityId");
		} else {
			return entityId.trim();
		}
	}

	private String detailUrl(String objectType, String objectId) throws UnsupportedEncodingException {
		return String.format("%s%s/%s/", this.listUrl, Utils.urlEncode(objectType), Utils.urlEncode(objectId));
	}

	public JSONObject get(String objectType, String objectId) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
		objectId = validateObjectEntityId(objectId);
		String url = detailUrl(objectType, objectId);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	public JSONObject upsert(String objectType, String objectId, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
        String url = detailUrl(objectType, objectId);
		if (payload == null) {
			payload = new JSONObject();
		}
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST, payload.toString(), headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	public JSONObject edit(String objectType, String objectId, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
        String url = detailUrl(objectType, objectId);
		if (payload == null) {
			payload = new JSONObject();
		}
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.PATCH, payload.toString(), headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.PATCH, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	public JSONObject delete(String objectType, String objectId) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
        String url = detailUrl(objectType, objectId);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.DELETE, "", headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.DELETE, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		// if no error, that means successfully deleted
		return new JSONObject().put("success", true).put("status_code", resp.statusCode);
	}

	public JSONObject bulkDelete(String objectType, String objectId, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
        String url = String.format("%s%s/%s/", this.bulkUrl, Utils.urlEncode(objectType), Utils.urlEncode(objectId));
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.DELETE, payload.toString(), headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.DELETE, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		// if no error, that means successfully deleted
		return new JSONObject().put("success", true).put("status_code", resp.statusCode);
	}

	public JSONObject getSubscriptions(String objectType, String objectId, HashMap<String, Object> opts) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
        objectType = validateObjectEntityId(objectType);
		objectId = validateObjectEntityId(objectId);
		String encodedParams = Utils.buildQueryParams(opts);
		String detailUrl = this.detailUrl(objectType, objectId);
		String url = String.format("%ssubscription/?%s", detailUrl, encodedParams);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers, contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	public JSONObject createSubscriptions(String objectType, String objectId, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
		objectId = validateObjectEntityId(objectId);
		String detailUrl = this.detailUrl(objectType, objectId);
		String url = String.format("%ssubscription/", detailUrl);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST, payload.toString(), headers, this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, url, headers, contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}
		
	public JSONObject deleteSubscriptions(String objectType, String objectId, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
        String url = detailUrl(objectType, objectId);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.DELETE, payload.toString(), headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.DELETE, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		// if no error, that means successfully deleted
		return new JSONObject().put("success", true).put("status_code", resp.statusCode);
	}

	public Objects getInstance(String objectType, String objectId) throws IOException, SuprsendException {
		objectType = validateObjectEntityId(objectType);
        objectId = validateObjectEntityId(objectId);
		return new Objects(this.config, objectType, objectId);
	}
}
