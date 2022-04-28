package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

public class BatchResponse {
	String status;
	JSONArray failureRecords;
	int total, success, failure;
	
	public BatchResponse() {
		this.total = this.success = this.failure = 0;
		this.failureRecords = new JSONArray();
	}
	
	public void mergeChunkResponse(JSONObject chunkResponse) {
		if (chunkResponse.length() > 0) {
			if (this.status.isEmpty()) {
				this.status = chunkResponse.getString("status");
			}
			else {
				if (this.status == "success") {
					if (chunkResponse.getString("status") == "fail") {
						this.status = "partial";
					}
				}
				else if (this.status == "fail") {
					if (chunkResponse.getString("status") == "success") {
						this.status = "partial";
					}
				}
			}
			if (chunkResponse.has("total")) {
				this.total += chunkResponse.getInt("total");
			}
			else {
				this.total += 0;
			}
			if (chunkResponse.has("success")) {
				this.success += chunkResponse.getInt("success");
			}
			else {
				this.success += 0;
			}
			if (chunkResponse.has("failure")) {
				this.failure += chunkResponse.getInt("failure");
			}
			else {
				this.failure += 0;
			}
			JSONArray failureRecords = chunkResponse.getJSONArray("failed_records");
			this.failureRecords.put(failureRecords);
		}
	}
}
