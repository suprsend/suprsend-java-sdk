package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.json.JSONObject;

public class UsersApi {
	private static final Logger logger = Logger.getLogger(UsersApi.class.getName());

	private Suprsend config;
	private String listUrl;

	UsersApi(Suprsend config) {
		this.config = config;
		this.listUrl = String.format("%sv1/user/", this.config.baseUrl);
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
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
}
