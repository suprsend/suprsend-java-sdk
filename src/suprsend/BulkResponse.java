package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BulkResponse {

	public String status;
	public List<JSONObject> failedRecords = new ArrayList<JSONObject>();
	public int total = 0;
	public int success = 0;
	public int failure = 0;
	public List<String> warnings = new ArrayList<String>();

	@Override
	public String toString() {
		return String.format("BulkResponse{status: '%s' | total: %d | success: %d | failure: %d | warnings: %d}",
				this.status, this.total, this.success, this.failure, this.warnings.size());
	}

	void mergeChunkResponse(JSONObject chResponse) {
		if (chResponse == null)
			return;
		// possible status: success/partial/fail
		String newStatus = chResponse.optString("status");
		if (this.status == null || this.status.isEmpty()) {
			this.status = newStatus;
		} else {
			if (this.status == "success") {
				if (newStatus == "fail") {
					this.status = "partial";
				}
			} else if (this.status == "fail") {
				if (newStatus == "success") {
					this.status = "partial";
				}
			}
		}
		this.total += chResponse.optInt("total", 0);
		this.success += chResponse.optInt("success", 0);
		this.failure += chResponse.optInt("failure", 0);
		JSONArray failedRecs = chResponse.optJSONArray("failed_records");
		for (Iterator<Object> iterator = failedRecs.iterator(); iterator.hasNext();) {
			this.failedRecords.add((JSONObject) iterator.next());
		}
	}

	static JSONObject emptyChunkSuccessResponse() {
		return new JSONObject()
				.put("status", "success")
				.put("status_code", 200)
				.put("total", 0)
				.put("success", 0)
				.put("failure", 0)
				.put("failed_records", new ArrayList<>());
	}

	static JSONObject invalidRecordsChunkResponse(List<JSONObject> invalidRecords) {
		return new JSONObject()
				.put("status", "fail")
				.put("status_code", 500)
				.put("total", invalidRecords.size())
				.put("success", 0)
				.put("failure", invalidRecords.size())
				.put("failed_records", invalidRecords);
	}

}
