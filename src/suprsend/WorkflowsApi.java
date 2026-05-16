package suprsend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.json.JSONObject;

public class WorkflowsApi {
    private static final Logger logger = Logger.getLogger(WorkflowsApi.class.getName());
    
    private Suprsend config;

    WorkflowsApi(Suprsend config) {
        this.config = config;
    }

    public JSONObject trigger(WorkflowTriggerRequest workflow) throws SuprsendException, UnsupportedEncodingException {
		JSONObject o = workflow.getFinalJson(config, false);
		JSONObject workflowBody = o.getJSONObject("event");
		// int apparentSize = o.getInt("apparent_size");
        JSONObject headers = this.config.getHeaders();
		JSONObject response = new JSONObject();
        String url = String.format("%strigger/", this.config.baseUrl);
		try {
			// Signature and Authorization Header
			JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST, workflowBody.toString(),
					headers, this.config.apiSecret);
			String contentText = sigResult.getString("contentTxt");
			headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			// --- Make HTTP POST request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, url,
					headers, contentText, this.config.httpClient);
			int statusCode = resp.statusCode;
			String responseText = resp.responseText;
			//
			if (statusCode >= 200 && statusCode < 300) {
				response.put("success", true).put("status", "success").put("status_code", statusCode)
						.put("message", (resp.jsonResponse != null ? resp.jsonResponse.optString("message_id") : responseText))
						.put("raw_response", resp.jsonResponse);
			} else {
				response.put("success", false).put("status", "fail").put("status_code", statusCode)
						.put("message", resp.errMsg)
						.put("raw_response", resp.jsonResponse);
			}
	
		} catch (SuprsendException | IOException e) {
			response.put("success", false).put("status", "fail").put("status_code", 500)
					.put("message", e.toString())
					.put("raw_response", JSONObject.NULL);
		}
		return response;
	}

    public BulkWorkflowTrigger bulkTriggerInstance() {
        return new BulkWorkflowTrigger(config);
    }
}
