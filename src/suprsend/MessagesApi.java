package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API for querying and updating notification messages.
 * Access via {@code suprsendClient.messages}.
 */
public class MessagesApi {
	private static final Logger logger = Logger.getLogger(MessagesApi.class.getName());

	// Keys whose values may be lists; emitted with bracket notation: key[]=v1&key[]=v2
	private static final List<String> MULTI_VALUE_KEYS = Arrays.asList("recipient_id", "status", "category");

	private Suprsend config;
	private String listUrl;
	private String bulkUrl;

	MessagesApi(Suprsend config) {
		this.config = config;
		this.listUrl = String.format("%sv1/message", this.config.baseUrl);
		this.bulkUrl = String.format("%sv1/bulk/message", this.config.baseUrl);
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	private String buildListQuery(HashMap<String, Object> opts) throws UnsupportedEncodingException {
		if (opts == null || opts.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> entry : opts.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			if (MULTI_VALUE_KEYS.contains(key) && val instanceof List) {
				for (Object item : (List<?>) val) {
					if (sb.length() > 0)
						sb.append("&");
					sb.append(Utils.urlEncode(key + "[]")).append("=").append(Utils.urlEncode(String.valueOf(item)));
				}
			} else {
				if (sb.length() > 0)
					sb.append("&");
				sb.append(Utils.urlEncode(key)).append("=").append(Utils.urlEncode(String.valueOf(val)));
			}
		}
		return sb.toString();
	}

	/**
	 * Fetch a paginated list of messages. Uses cursor-based pagination; results are
	 * sorted newest first.
	 *
	 * @return JSONObject with {@code results} array and {@code meta} pagination info
	 * @throws IOException
	 * @throws SuprsendException
	 */
	public JSONObject list() throws IOException, SuprsendException {
		return list(null);
	}

	/**
	 * Fetch a paginated list of messages with filters.
	 *
	 * <p>Supported keys in {@code opts}:
	 * <ul>
	 *   <li>{@code limit} (int) — results per page, max 1000, default 20</li>
	 *   <li>{@code after} (String) — cursor for next page ({@code meta.after} from previous response)</li>
	 *   <li>{@code before} (String) — cursor for previous page ({@code meta.before} from previous response)</li>
	 *   <li>{@code recipient_id} (List&lt;String&gt; or String) — filter by recipient distinct_id</li>
	 *   <li>{@code status} (List&lt;String&gt; or String) — filter by status; see valid values in docs</li>
	 *   <li>{@code message_id} (String) — exact match on message_id</li>
	 *   <li>{@code idempotency_key} (String)</li>
	 *   <li>{@code tenant_id} (String)</li>
	 *   <li>{@code workflow_slug} (String)</li>
	 *   <li>{@code channel} (String) — e.g. email, sms, inbox, androidpush</li>
	 *   <li>{@code execution_id} (String) — workflow execution or broadcast ID</li>
	 *   <li>{@code created_at_gte} (String) — RFC3339 timestamp</li>
	 *   <li>{@code created_at_lte} (String) — RFC3339 timestamp</li>
	 *   <li>{@code object_id} (String) — must be paired with object_type</li>
	 *   <li>{@code object_type} (String) — must be paired with object_id</li>
	 *   <li>{@code is_campaign} (boolean) — true = broadcasts only, false = workflows only</li>
	 *   <li>{@code category} (List&lt;String&gt; or String) — filter by notification category</li>
	 * </ul>
	 *
	 * @param opts filter and pagination options
	 * @return JSONObject with {@code results} array and {@code meta} pagination info
	 * @throws IOException
	 * @throws SuprsendException
	 */
	public JSONObject list(HashMap<String, Object> opts) throws IOException, SuprsendException {
		String encodedParams = buildListQuery(opts);
		String url = this.listUrl + (encodedParams.isEmpty() ? "" : "?" + encodedParams);
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
				contentText, this.config.httpClient);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	/**
	 * Update the status of one or more messages in a single call. The overall HTTP
	 * response is 202 Accepted even when individual items fail; check each record's
	 * {@code status_code} for per-item results.
	 *
	 * <p>Each element of {@code messages} must be a JSONObject with:
	 * <ul>
	 *   <li>{@code message_id} (String, required)</li>
	 *   <li>{@code action} (String, required) — seen, clicked, dismissed, read, unread, archived, unarchived</li>
	 * </ul>
	 *
	 * @param messages JSONArray of patch items
	 * @return JSONObject with a {@code records} array of per-item results
	 * @throws IOException
	 * @throws SuprsendException
	 */
	public JSONObject bulkPatch(JSONArray messages) throws IOException, SuprsendException {
		JSONObject payload = new JSONObject().put("messages", messages);
		String url = this.bulkUrl;
		//
		JSONObject headers = getHeaders();
		// Signature and Authorization-header
		JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.PATCH, payload.toString(), headers,
				this.config.apiSecret);
		String contentText = sigResult.getString("contentTxt");
		headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
		//
		SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.PATCH, url, headers,
				contentText, this.config.httpClient);
		if (resp.statusCode >= 400) {
			throw new SuprsendException(resp.errMsg, resp.statusCode);
		}
		return resp.jsonResponse;
	}

	// private String validateMessageId(String messageId) throws SuprsendException {
	// 	if (messageId == null || messageId.trim().isEmpty()) {
	// 		throw new SuprsendException("missing messageId");
	// 	}
	// 	return messageId.trim();
	// }

	// private String contentUrl(String messageId) throws UnsupportedEncodingException {
	// 	return String.format("%s/%s/content", this.listUrl, Utils.urlEncode(messageId));
	// }

	// /**
	//  * Fetch the rendered content of a message (subject, body, title, etc.).
	//  * Throws SuprsendException with status 404 if content has expired or was never saved.
	//  *
	//  * @param messageId message ID (ULID)
	//  * @return JSONObject with {@code notification_id}, {@code channel}, {@code rendered_at},
	//  *         and {@code content} fields
	//  * @throws IOException
	//  * @throws SuprsendException
	//  */
	// public JSONObject getContent(String messageId) throws IOException, SuprsendException {
	// 	messageId = validateMessageId(messageId);
	// 	String url = contentUrl(messageId);
	// 	//
	// 	JSONObject headers = getHeaders();
	// 	// Signature and Authorization-header
	// 	JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
	// 	String contentText = sigResult.getString("contentTxt");
	// 	headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
	// 	//
	// 	SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
	// 			contentText, this.config.httpClient);
	// 	if (resp.statusCode >= 400) {
	// 		throw new SuprsendException(resp.errMsg, resp.statusCode);
	// 	}
	// 	return resp.jsonResponse;
	// }
}
