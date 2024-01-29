package suprsend;

import org.json.JSONObject;
import org.json.JSONException;

public class SuprsendResponse {
	int statusCode;
	String responseText;
	String contentType;

	// in case of >= 400 status
	String errMsg;

	// in case contentType=application/json
	JSONObject jsonResponse;

	SuprsendResponse(int statusCode, String responseText, String contentType) {
		this.statusCode = statusCode;
		this.responseText = responseText;
		this.contentType = contentType;
	}

	void parseResponse() {
		if (this.contentType != null && this.contentType.contains("application/json")) {
			try {
				this.jsonResponse = new JSONObject(this.responseText);
			} catch (JSONException ex) {
			}
			if (this.statusCode >= 400) {
				if (this.jsonResponse != null) {
					String msgStr = this.jsonResponse.optString("message");
					String dtlStr = this.jsonResponse.optString("detail");
					String finalErrMsg = msgStr.isEmpty() ? dtlStr : msgStr;
					if (finalErrMsg.isEmpty()) {
						finalErrMsg = this.responseText;
					}
					this.errMsg = finalErrMsg;
				} else {
					this.errMsg = this.responseText;
				}
			}
		} else {
			if (this.statusCode >= 400) {
				this.errMsg = this.responseText;
			}
		}
	}
}
