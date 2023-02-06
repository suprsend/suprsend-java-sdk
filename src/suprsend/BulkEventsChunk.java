package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Logger;

class BulkEventsChunk {
    private static final Logger logger = Logger.getLogger(BulkEventsChunk.class.getName());
    private Suprsend config;
    private JSONArray __chunk = new JSONArray();
    int __running_size = 0;
    JSONObject response = new JSONObject();

    public BulkEventsChunk(Suprsend config) {
        this.config = config;
    }

    private String getUrl() {
        String urlTemplate = "%sevent/";
        if (this.config.includeSignatureParam) {
            if (this.config.authEnabled) {
                urlTemplate = urlTemplate + "?verify=true";
            } else {
                urlTemplate = urlTemplate + "?verify=false";
            }
        }
        return String.format(urlTemplate, this.config.baseUrl);
    }

    private JSONObject getCommonHeaders() {
        return new JSONObject()
                .put("Content-Type", "application/json; charset=utf-8")
                .put("User-Agent", this.config.userAgent);
    }

    private JSONObject dynamicHeaders() {
        return new JSONObject().put("Date", Utils.getCurrentDateTimeFormatted(Constants.HEADER_DATE_FMT));
    }

    private JSONObject getMergedHeaders() {
        return Utils.mergeJSONObjects(getCommonHeaders(), dynamicHeaders());
    }

    public void addEventToChunk(JSONObject event, int eventSize) {
        __running_size += eventSize;
        __chunk.put(event);
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
            throw new SuprsendException("Event properties too big - " + eventSize + " Bytes" +
                    "must not cross " + Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
        }
        if (__running_size + eventSize > Constants.CHUNK_APPARENT_SIZE_IN_BYTES) {
            return false;
        }
        if (!Constants.ALLOW_ATTACHMENTS_IN_BULK_API) {
            // TODO - test attachments
            event.getJSONObject("properties").remove("$attachments");
        }
        addEventToChunk(event, eventSize);
        return true;
    }

    public void trigger() {
        JSONObject headers = getMergedHeaders();

        try {
            String contentText;
            if (this.config.authEnabled) {
                // Signature and Authorization Header
                JSONObject sigResult = Signature.getRequestSignature(getUrl(), "POST", __chunk.toString(), headers,
                        this.config.workspaceSecret);
                contentText = sigResult.getString("contentTxt");
                headers.put("Authorization",
                        String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
            } else {
                contentText = __chunk.toString();
            }
            // --- Make HTTP POST request
            SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, "POST", getUrl(), headers,
                    contentText);
            int statusCode = resp.statusCode;
            String responseText = resp.responseText;
            //
            if (statusCode >= 200 && statusCode < 300) {
                response.put("success", true)
                        .put("status", "success")
                        .put("status_code", statusCode)
                        .put("message", responseText);
            } else {
                response.put("success", false)
                        .put("status", "fail")
                        .put("status_code", statusCode)
                        .put("message", responseText);
            }
        } catch (SuprsendException | IOException e) {
            response.put("success", false)
                    .put("status", "fail")
                    .put("status_code", 500)
                    .put("message", e.toString());
        }

    }

}