package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Logger;

public class BulkWorkflowsChunk {

    private static final Logger logger = Logger.getLogger(BulkWorkflowsChunk.class.getName());
    private Suprsend config;
    private JSONArray __chunk = new JSONArray();
    int __running_size = 0;
    JSONObject response = new JSONObject();

    public BulkWorkflowsChunk(Suprsend config) {
        this.config = config;
    }

    private String getUrl() {
        String urlTemplate = "%s%s/trigger/";
        return String.format(urlTemplate, this.config.baseUrl, this.config.workspaceKey);
    }

    private JSONObject getCommonHeaders() {
        return new JSONObject()
                .put("Content-Type", "application/json; charset=utf-8")
                .put("User-Agent", this.config.userAgent);
    }

    private JSONObject dynamicHeaders() {
        return new JSONObject().put("Date", Utils.getCurrentDateTimeFormatted(Constants.HEADER_DATE_FMT));
    }

    public void addEventToChunk(JSONObject event, int eventSize) {
        __running_size += eventSize;
        __chunk.put(event);
    }

    private JSONObject getMergedHeaders() {
        return Utils.mergeJSONObjects(getCommonHeaders(), dynamicHeaders());
    }

    public boolean checkLimitReached() {
        if (__chunk.length() >= Constants.MAX_RECORDS_IN_CHUNK ||
                __running_size >= Constants.CHUNK_APPARENT_SIZE_IN_BYTES)
            return true;
        else
            return false;
    }

    public boolean tryToAddIntoChunk(JSONObject event, int eventSize) throws SuprsendException {
        if (event == null) {
            return true;
        }
        if (checkLimitReached()) {
            return false;
        }
        if (eventSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
            throw new SuprsendException("workflow body too big - " + eventSize + " Bytes" +
                    "must not cross " + Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
        }
        if (__running_size + eventSize > Constants.CHUNK_APPARENT_SIZE_IN_BYTES) {
            return false;
        }
        if (!Constants.ALLOW_ATTACHMENTS_IN_BULK_API) {
            event.remove("$attachments");
        }
        addEventToChunk(event, eventSize);
        return true;
    }

    public void trigger() {
        JSONObject headers = getMergedHeaders();

        try {
            String contentText;
            // Signature and Authorization Header
            JSONObject sigResult = Signature.getRequestSignature(getUrl(), HttpMethod.POST, __chunk.toString(), headers,
                    this.config.workspaceSecret);
            contentText = sigResult.getString("contentTxt");
            headers.put("Authorization",
                    String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
            // --- Make HTTP POST request
            SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, getUrl(), headers,
                    contentText);
            int statusCode = resp.statusCode;
            String responseText = resp.responseText;
            //
            if (statusCode >= 200 && statusCode < 300) {
                response.put("status", "success")
                        .put("status_code", statusCode)
                        .put("total", __chunk.length())
                        .put("success", __chunk.length())
                        .put("failure", 0)
                        .put("failed_records", new JSONArray());
            } else {

                JSONArray failedRecordsJa = getFailedRecordsJA(statusCode, responseText);
                response.put("status", "fail")
                        .put("status_code", statusCode)
                        .put("total", __chunk.length())
                        .put("success", 0)
                        .put("failure", __chunk.length())
                        .put("failed_records", failedRecordsJa);
            }
        } catch (SuprsendException | IOException e) {
            JSONArray failedRecordsJa = getFailedRecordsJA(500, e.toString());
            response.put("status", "fail")
                    .put("status_code", 500)
                    .put("total", __chunk.length())
                    .put("success", 0)
                    .put("failure", __chunk.length())
                    .put("failed_records", failedRecordsJa);
        }

    }

    private JSONArray getFailedRecordsJA(int statusCode, String responseText) {
        JSONArray failedRecordsJa = new JSONArray();
        for (int ci = 0; ci < __chunk.length(); ci++) {
            JSONObject chJo = __chunk.getJSONObject(ci);
            JSONObject newChJO = new JSONObject();
            newChJO.put("record", chJo);
            newChJO.put("error", responseText);
            newChJO.put("code", statusCode);
            failedRecordsJa.put(newChJO);
        }
        return failedRecordsJa;
    }


}
