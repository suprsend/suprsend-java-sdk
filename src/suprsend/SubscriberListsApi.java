package suprsend;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class SubscriberListsApi {
	private static final Logger logger = Logger.getLogger(SubscriberListsApi.class.getName());

	private final Suprsend config;
	private String subscriberListUrl;
	private String broadcastUrl;
	private JSONObject nonErrorDefaultResponse;

	SubscriberListsApi(Suprsend config) {
		this.config = config;
		this.subscriberListUrl = String.format("%sv1/subscriber_list/", this.config.baseUrl);
		this.broadcastUrl = String.format("%s%s/broadcast/", this.config.baseUrl, this.config.apiKey);
		this.nonErrorDefaultResponse = new JSONObject().put("success", true);
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	private String validateListId(String listId) throws SuprsendException {
		if (listId == null || listId.trim().isEmpty()) {
			throw new SuprsendException("missing list_id");
		} else {
			return listId.trim();
		}
	}

	public JSONObject create(JSONObject payload) throws SuprsendException, IOException {
		if (payload == null || payload.length() == 0) {
			throw new SuprsendException("missing payload");
		}
		String listId = validateListId(payload.optString("list_id"));
		payload.put("list_id", listId);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(this.subscriberListUrl, HttpMethod.POST,
				payload.toString(), headers, this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST,
				this.subscriberListUrl, headers, contentText);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	private int cleanLimit(int limit) {
		// limit must be 0 < x <= 1000
		if (limit > 0 && limit <= 1000) {
			return limit;
		}
		return 20;
	}

	private int cleanOffset(int offset) {
		// offset must be >=0
		if (offset >= 0) {
			return offset;
		}
		return 0;
	}

	public JSONObject getAll() throws IOException, SuprsendException {
		return getAll(20, 0);
	}

	public JSONObject getAll(int limit) throws IOException, SuprsendException {
		return getAll(limit, 0);
	}

	public JSONObject getAll(int limit, int offset) throws IOException, SuprsendException {
		HashMap<String, Object> queryParamsMap = new HashMap<String, Object>() {
			{
				put("limit", cleanLimit(limit));
				put("offset", cleanOffset(offset));
			}
		};
		String encodedParams = Utils.buildQueryParams(queryParamsMap);
		String url = String.format("%s?%s", this.subscriberListUrl, encodedParams);
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

	private String subscriberListDetailUrl(String listId) throws UnsupportedEncodingException {
		return String.format("%s%s/", this.subscriberListUrl, Utils.urlEncode(listId));
	}

	public JSONObject get(String listId) throws SuprsendException, IOException {
		listId = validateListId(listId);
		String url = subscriberListDetailUrl(listId);
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

	public JSONObject add(String listId, List<String> distinctIds) throws SuprsendException, IOException {
		listId = validateListId(listId);
		if (distinctIds == null || distinctIds.size() == 0) {
			return this.nonErrorDefaultResponse;
		}
		String url = String.format("%ssubscriber/add/", subscriberListDetailUrl(listId));
		JSONObject payload = new JSONObject().put("distinct_ids", distinctIds);
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

	public JSONObject remove(String listId, List<String> distinctIds) throws SuprsendException, IOException {
		listId = validateListId(listId);
		if (distinctIds == null || distinctIds.size() == 0) {
			return this.nonErrorDefaultResponse;
		}
		String url = String.format("%ssubscriber/remove/", subscriberListDetailUrl(listId));
		JSONObject payload = new JSONObject().put("distinct_ids", distinctIds);
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

	public JSONObject delete(String listId) throws SuprsendException, IOException {
		listId = validateListId(listId);
		//
		String url = String.format("%sdelete/", subscriberListDetailUrl(listId));
		JSONObject payload = new JSONObject();
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

	public JSONObject broadcast(SubscriberListBroadcast broadcastInstance) throws IOException, SuprsendException {
		if (broadcastInstance == null) {
			throw new SuprsendException("argument must be an instance of suprsend.SubscriberListBroadcast");
		}
		// {"event": broadcast_body, "apparent_size": apparentSize}
		JSONObject validatedJson = broadcastInstance.getFinalJson();
		JSONObject broadcastBody = validatedJson.getJSONObject("event");
		//
		JSONObject response = new JSONObject();
		try {
			JSONObject headers = getHeaders();
			// Signature and Authorization-header
			JSONObject sigResult = Signature.getRequestSignature(this.broadcastUrl, HttpMethod.POST,
					broadcastBody.toString(), headers, this.config.apiSecret);
			String contentText = sigResult.getString("contentTxt");
			headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			//
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST,
					this.broadcastUrl, headers, contentText);
			int statusCode = resp.statusCode;
			String responseText = resp.responseText;
			//
			if (statusCode >= 200 && statusCode < 300) {
				response.put("success", true).put("status", "success").put("status_code", statusCode).put("message",
						responseText);
			} else {
				response.put("success", false).put("status", "fail").put("status_code", statusCode).put("message",
						responseText);
			}
		} catch (SuprsendException | IOException e) {
			response.put("success", false).put("status", "fail").put("status_code", 500).put("message", e.toString());
		}
		return response;
	}

	public JSONObject startSync(String listId) throws SuprsendException, IOException {
		listId = validateListId(listId);
		//
		String url = String.format("%sstart_sync/", subscriberListDetailUrl(listId));
		JSONObject payload = new JSONObject();
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

	private String validateVersionId(String versionId) throws SuprsendException {
		if (versionId == null || versionId.trim().isEmpty()) {
			throw new SuprsendException("missing version_id");
		} else {
			return versionId.trim();
		}
	}

	private String subscriberListUrlWithVersion(String listId, String versionId) throws UnsupportedEncodingException {
		return String.format("%s%s/version/%s/", this.subscriberListUrl, Utils.urlEncode(listId),
				Utils.urlEncode(versionId));
	}

	public JSONObject getVersion(String listId, String versionId) throws SuprsendException, IOException {
		listId = validateListId(listId);
		versionId = validateVersionId(versionId);
		String url = subscriberListUrlWithVersion(listId, versionId);
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

	public JSONObject addToVersion(String listId, String versionId, List<String> distinctIds)
			throws SuprsendException, IOException {
		listId = validateListId(listId);
		versionId = validateVersionId(versionId);
		if (distinctIds == null || distinctIds.size() == 0) {
			return this.nonErrorDefaultResponse;
		}
		String url = String.format("%ssubscriber/add/", subscriberListUrlWithVersion(listId, versionId));
		JSONObject payload = new JSONObject().put("distinct_ids", distinctIds);
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

	public JSONObject removeFromVersion(String listId, String versionId, List<String> distinctIds)
			throws SuprsendException, IOException {
		listId = validateListId(listId);
		versionId = validateVersionId(versionId);
		if (distinctIds == null || distinctIds.size() == 0) {
			return this.nonErrorDefaultResponse;
		}
		String url = String.format("%ssubscriber/remove/", subscriberListUrlWithVersion(listId, versionId));
		JSONObject payload = new JSONObject().put("distinct_ids", distinctIds);
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

	public JSONObject finishSync(String listId, String versionId) throws SuprsendException, IOException {
		listId = validateListId(listId);
		versionId = validateVersionId(versionId);
		//
		String url = String.format("%sfinish_sync/", subscriberListUrlWithVersion(listId, versionId));
		JSONObject payload = new JSONObject();
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

	public JSONObject deleteVersion(String listId, String versionId) throws SuprsendException, IOException {
		listId = validateListId(listId);
		versionId = validateVersionId(versionId);
		//
		String url = String.format("%sdelete/", subscriberListUrlWithVersion(listId, versionId));
		JSONObject payload = new JSONObject();
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
}
