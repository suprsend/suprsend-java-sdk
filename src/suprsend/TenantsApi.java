package suprsend;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Logger;

public class TenantsApi {
    private static final Logger logger = Logger.getLogger(TenantsApi.class.getName());

	private Suprsend config;
	private String listUrl;

	TenantsApi(Suprsend config) {
		this.config = config;
		this.listUrl = String.format("%sv1/tenant/", this.config.baseUrl);
	}

    private JSONObject getHeaders() {
		return new JSONObject()
		        .put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent)
                .put("Date", Utils.getCurrentDateTimeHeader());
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

    public JSONObject list() throws IOException, SuprsendException {
        return list(20, 0);
    }

    public JSONObject list(int limit) throws IOException, SuprsendException {
        return list(limit, 0);
    }

    public JSONObject list(int limit, int offset) throws IOException, SuprsendException {
        HashMap<String, Object> queryParamsMap = new HashMap<String, Object>(){{
            put("limit", cleanLimit(limit));
            put("offset", cleanOffset(offset));
        }};
        String encodedParams = Utils.buildQueryParams(queryParamsMap);
        String url = String.format("%s?%s", this.listUrl, encodedParams);
        // 
        JSONObject headers = getHeaders();
        // Signature and Authorization-header
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization", 
                String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
        // 
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
                contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.errMsg, resp.statusCode);
        }
        return resp.jsonResponse;
    }

    private String validateTenantId(String tenantId) throws SuprsendException {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new SuprsendException("missing tenantId");
		} else {
            return tenantId.trim();
        }
    }

    private String detailUrl(String tenantId) throws UnsupportedEncodingException {
        return String.format("%s%s/", this.listUrl, Utils.urlEncode(tenantId));
    }

    public JSONObject get(String tenantId) throws IOException, SuprsendException {
        tenantId = validateTenantId(tenantId);
        String url = detailUrl(tenantId);
        // 
        JSONObject headers = getHeaders();
        // Signature and Authorization-header
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, "", headers, this.config.apiSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
        // 
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
                contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.errMsg, resp.statusCode);
        }
        return resp.jsonResponse;
    }

    public JSONObject upsert(String tenantId, JSONObject tenantPayload) throws IOException, SuprsendException {
        tenantId = validateTenantId(tenantId);
        String url = detailUrl(tenantId);
        if (tenantPayload == null) {
            tenantPayload = new JSONObject();
        }
        // 
        JSONObject headers = getHeaders();
        // Signature and Authorization-header
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST, 
                tenantPayload.toString(), headers, this.config.apiSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
        // 
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, 
                url, headers, contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.errMsg, resp.statusCode);
        }
        return resp.jsonResponse;
    }

    public JSONObject delete(String tenantId) throws IOException, SuprsendException {
        tenantId = validateTenantId(tenantId);
        String url = detailUrl(tenantId);
        // 
        JSONObject headers = getHeaders();
        // Signature and Authorization-header
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.DELETE, "", headers, this.config.apiSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
        // 
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.DELETE, 
                url, headers, contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.errMsg, resp.statusCode);
        }
        // if no error, that means successfully deleted
        return new JSONObject()
            .put("success", true)
            .put("status_code", resp.statusCode);
    }
}
