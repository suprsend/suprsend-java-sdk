package suprsend;

import org.json.JSONObject;

import java.util.ArrayList;

public class BulkResponse {

    public String status;
    public int total = 0;
    public int success = 0;
    public int failure = 0;
    public ArrayList<String> warnings = new ArrayList<String>();

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

    @Override
    public String toString() {
        return "BulkResponse{" +
                "status='" + status + '\'' +
                ", total=" + total +
                ", success=" + success +
                ", failure=" + failure +
                '}';
    }
}
