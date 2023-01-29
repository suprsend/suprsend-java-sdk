package suprsend;

import org.json.JSONObject;

public class BulkResponse {

    String status;
    int total = 0;
    int success = 0;
    int failure = 0;

    void mergeChunkResponse(JSONObject responseJO) {
        if (responseJO == null)
            return;
        if (responseJO.has("status")) {
            status = responseJO.optString("status");
        } else {
            if (status.equalsIgnoreCase("success")) {
                if (responseJO.optString("status").equalsIgnoreCase("fail")) {
                    status = "partial";
                }
            } else if (status.equalsIgnoreCase("fail")) {
                if (responseJO.optString("status").equalsIgnoreCase("success")) {
                    status = "partial";
                }
            }
        }
        total += responseJO.optInt("total", 0);
        success += responseJO.optInt("success", 0);
        failure += responseJO.optInt("failure", 0);
    }
}
