package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.JSONObject;

public class UsersApi {
	private static final Logger logger = Logger.getLogger(UsersApi.class.getName());

	private Suprsend config;
	private String listUrl;
	private String bulkUrl;

	UsersApi(Suprsend config) {
		this.config = config;
		this.listUrl = String.format("%sv1/user/", this.config.baseUrl);
		this.bulkUrl = String.format("%sv1/bulk/user/", this.config.baseUrl);
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	public JSONObject list() throws IOException, SuprsendException {
		return list(null);
	}

	public JSONObject list(HashMap<String, Object> opts) throws IOException, SuprsendException {
		String encodedParams = Utils.buildQueryParams(opts);
		String url = String.format("%s%s", this.listUrl,
				(encodedParams == "" ? "" : String.format("?%s", encodedParams)));
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

	private String validateDistinctId(String distinctId) throws SuprsendException {
		if (distinctId == null || distinctId.trim().isEmpty()) {
			throw new SuprsendException("missing distinct_id");
		} else {
			return distinctId.trim();
		}
	}

	private String detailUrl(String distinctId) throws UnsupportedEncodingException {
		return String.format("%s%s/", this.listUrl, Utils.urlEncode(distinctId));
	}

	public JSONObject get(String distinctId) throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		String url = detailUrl(distinctId);
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

	public JSONObject upsert(String distinctId, JSONObject payload) throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		String url = detailUrl(distinctId);
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

	public JSONObject asyncEdit(UserEdit editInstance) throws IOException, SuprsendException {
		if (editInstance == null) {
			throw new SuprsendException("instance is required");
		}
		editInstance.validateBody();
		JSONObject aPayload = editInstance.getAsyncPayload();
		// --- validate payload size
		JSONObject aPl = editInstance.validatPayloadSize(aPayload);
		aPayload = aPl.getJSONObject("payload");
		//
		String url = String.format("%sevent/", this.config.baseUrl);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST, aPayload.toString(), headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, url, headers,
				contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		// if no error, return success response
		return new JSONObject().put("success", true).put("status", "success").put("status_code", resp.statusCode)
				.put("message", resp.responseText);
	}

	public JSONObject edit(UserEdit editInstance) throws IOException, SuprsendException {
		if (editInstance == null) {
			throw new SuprsendException("instance is required");
		}
		editInstance.validateBody();
		String url = detailUrl(editInstance.getDistinctId());
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

	public JSONObject edit(String distinctId, JSONObject payload) throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		if (payload == null) {
			payload = new JSONObject();
		}
		String url = detailUrl(distinctId);
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

	public JSONObject merge(String distinctId, String fromUserId) throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		// {"from_user_id": "id1"}
		JSONObject payload = new JSONObject().put("from_user_id", fromUserId);
		//
		String detailUrl = detailUrl(distinctId);
		String url = String.format("%smerge/", detailUrl);
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

	public JSONObject delete(String distinctId) throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		String url = detailUrl(distinctId);
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

	public JSONObject bulkDelete(JSONObject payload) throws IOException, SuprsendException {
		// payload: {"distinct_ids": ["id1", "id2"]}
		if (payload == null) {
			payload = new JSONObject();
		}
		String url = this.bulkUrl;
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

	public JSONObject getObjectsSubscribedTo(String distinctId, HashMap<String, Object> opts)
			throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		String encodedParams = Utils.buildQueryParams(opts);
		String detailUrl = detailUrl(distinctId);
		String url = String.format("%ssubscribed_to/object/%s", detailUrl,
				(encodedParams == "" ? "" : String.format("?%s", encodedParams)));
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

	public JSONObject getListsSubscribedTo(String distinctId, HashMap<String, Object> opts)
			throws IOException, SuprsendException {
		distinctId = validateDistinctId(distinctId);
		String encodedParams = Utils.buildQueryParams(opts);
		String detailUrl = detailUrl(distinctId);
		String url = String.format("%ssubscribed_to/list/%s", detailUrl,
				(encodedParams == "" ? "" : String.format("?%s", encodedParams)));
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

	public UserEdit getEditInstance(String distinctId) throws SuprsendException {
		distinctId = validateDistinctId(distinctId);
		return new UserEdit(this.config, distinctId);
	}

	public BulkUsersEdit getBulkEditInstance() {
		return new BulkUsersEdit(this.config);
	}

}
