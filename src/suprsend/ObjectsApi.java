package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.JSONObject;

public class ObjectsApi {
	private static final Logger logger = Logger.getLogger(ObjectsApi.class.getName());

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
		objectType = validateObjectType(objectType);
		String encodedParams = Utils.buildQueryParams(opts);
		String url = String.format("%s%s/%s", this.listUrl, Utils.urlEncode(objectType), (encodedParams == "" ? "" : String.format("?%s", encodedParams)));
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

	private String validateObjectType(String objectType) throws SuprsendException {
		if (objectType == null || objectType.trim().isEmpty()) {
			throw new SuprsendException("missing object_type");
		} else {
			return objectType.trim();
		}
	}

	private String validateObjectId(String objectId) throws SuprsendException {
		if (objectId == null || objectId.trim().isEmpty()) {
			throw new SuprsendException("missing object id");
		} else {
			return objectId.trim();
		}
	}

	private String detailUrl(String objectType, String objectId) throws UnsupportedEncodingException {
		return String.format("%s%s/%s/", this.listUrl, Utils.urlEncode(objectType), Utils.urlEncode(objectId));
	}

	public JSONObject get(String objectType, String objectId) throws IOException, SuprsendException {
		objectType = validateObjectType(objectType);
		objectId = validateObjectId(objectId);
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
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
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

	public JSONObject edit(ObjectEdit editInstance) throws IOException, SuprsendException {
		if (editInstance == null) {
			throw new SuprsendException("instance is required");
		}
        editInstance.validateBody();
		String url = detailUrl(editInstance.getObjectType(), editInstance.getObjectId());
		JSONObject payload = editInstance.getPayload();
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

	public JSONObject edit(String objectType, String objectId, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
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
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
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

	public JSONObject bulkDelete(String objectType, JSONObject payload) throws IOException, SuprsendException {
		objectType = validateObjectType(objectType);
        String url = String.format("%s%s/", this.bulkUrl, Utils.urlEncode(objectType));
		if (payload == null) {
			payload = new JSONObject();
		}
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
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
		String encodedParams = Utils.buildQueryParams(opts);
		String detailUrl = this.detailUrl(objectType, objectId);
		String url = String.format("%ssubscription/%s", detailUrl, (encodedParams == "" ? "" : String.format("?%s", encodedParams)));
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
		objectType = validateObjectType(objectType);
		objectId = validateObjectId(objectId);
		String detailUrl = this.detailUrl(objectType, objectId);
		String url = String.format("%ssubscription/", detailUrl);
		if (payload == null) {
			payload = new JSONObject();
		}
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
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
        String detailUrl = detailUrl(objectType, objectId);
		String url = String.format("%ssubscription/", detailUrl);
		if (payload == null) {
			payload = new JSONObject();
		}
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

	public JSONObject getObjectsSubscribedTo(String objectType, String objectId, HashMap<String, Object> opts) throws IOException, SuprsendException {
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
		String encodedParams = Utils.buildQueryParams(opts);
        String detailUrl = detailUrl(objectType, objectId);
		String url = String.format("%ssubscribed_to/object/%s", detailUrl, (encodedParams == "" ? "" : String.format("?%s", encodedParams)));
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

	public ObjectEdit getInstance(String objectType, String objectId) throws IOException, SuprsendException {
		objectType = validateObjectType(objectType);
        objectId = validateObjectId(objectId);
		return new ObjectEdit(this.config, objectType, objectId);
	}
}
