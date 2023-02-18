package suprsend;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

public class SubscriberListsApi {
    private static final Logger logger = Logger.getLogger(SubscriberListsApi.class.getName());
    private JSONObject nonErrorDefaultResponse;
    private Suprsend config;

    SubscriberListsApi(Suprsend config) {
        this.config = config;
        nonErrorDefaultResponse = new JSONObject();
        nonErrorDefaultResponse.put("success", true);
    }

    private String validateListId(Object listId) throws SuprsendException {
        if (!(listId instanceof String)) {
            throw new SuprsendException("list_id must be a string");
        }
        String tempListId = ((String) listId).trim();
        if (tempListId.isEmpty()) {
            throw new SuprsendException("missing list_id");
        }
        return tempListId;
    }

    //TODO - what should be the return type?
    public String create(JSONObject payload) throws SuprsendException, IOException {
        if (payload == null || payload.length() == 0) {
            throw new SuprsendException("missing payload");
        }
        if (!payload.has("list_id")) {
            throw new SuprsendException("missing list_id is payload");
        }
        String listId = validateListId(payload.get("list_id"));
        listId = Utils.urlEncode(listId);
        payload.put("list_id", listId);
        JSONObject headers = Utils.getMergedHeaders(config);

        JSONObject sigResult = Signature.getRequestSignature(getSubscriberListUrl(config), HttpMethod.POST, payload.toString(), headers, this.config.workspaceSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));

        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, getSubscriberListUrl(config), headers,
                contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.responseText);
        }
        return resp.responseText;
    }

    private int cleanedLimit(int limit) {
        // limit must be 0 < x <= 1000
        if (limit > 0 && limit <= 1000) {
            return limit;
        } else {
            return 20;
        }
    }

    private int cleanedOffset(int offset) {
        // offset must be >=0
        if (offset >= 0) {
            return offset;
        } else {
            return 0;
        }
    }

    public String getAll() throws IOException, SuprsendException {
        return getAll(20, 0);
    }

    public String getAll(int limit) throws IOException, SuprsendException {
        return getAll(limit, 0);
    }

    public String getAll(int limit, int offset) throws IOException, SuprsendException {
        int mLimit = cleanedLimit(limit);
        int mOffset = cleanedOffset(offset);
        String url = getSubscriberListUrl(config) + "?limit=" + mLimit + "&offset=" + mOffset;
        JSONObject headers = Utils.getMergedHeaders(config);
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, headers, this.config.workspaceSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
                contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.responseText);
        }
        return resp.responseText;
    }

    private String subscriberListDetailUrl(String listId) throws UnsupportedEncodingException {
        String mListId = listId.trim();
        mListId = Utils.urlEncode(mListId);
        return getSubscriberListUrl(config) + mListId + "/";
    }

    public String get(String listID) throws SuprsendException, IOException {
        String mListId = validateListId(listID);
        String url = subscriberListDetailUrl(mListId);
        JSONObject headers = Utils.getMergedHeaders(config);

        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.GET, headers, this.config.workspaceSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
        SuprsendResponse suprsendResponse = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.GET, url, headers,
                contentText);
        if(suprsendResponse.statusCode>=400){
            throw new  SuprsendException(suprsendResponse.responseText);
        }
        return suprsendResponse.responseText;
    }

    public String add(String listId, List<String> distinctIds) throws SuprsendException, IOException {
        String mListId = validateListId(listId);
        if(distinctIds.size()==0){
            //TODO - what should be the return type?
//            JSONObject response = new JSONObject();
//            response.put("success",true);
            return "ok";
        }
        String url = String.format("%ssubscriber/add/",subscriberListDetailUrl(mListId));
        JSONObject payload = new JSONObject();
        payload.put("distinct_ids",distinctIds);
        JSONObject headers = Utils.getMergedHeaders(config);
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST,payload.toString(), headers, this.config.workspaceSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, url, headers,
                contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.responseText);
        }
        return resp.responseText;
    }

    public String remove(String listId,List<String> distinctIds) throws SuprsendException, IOException {
        String mListId = validateListId(listId);
        if(distinctIds.size() == 0){
            //TODO - return type
//            JSONObject response = new JSONObject();
//            response.put("success",true);
            return "ok";
        }
        String url = String.format("%ssubscriber/remove/", subscriberListDetailUrl(mListId));
        JSONObject payload = new JSONObject();
        payload.put("distinct_ids",distinctIds);
        JSONObject headers = Utils.getMergedHeaders(config);
        JSONObject sigResult = Signature.getRequestSignature(url, HttpMethod.POST,payload.toString(), headers, this.config.workspaceSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
        SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, url, headers,
                contentText);
        if (resp.statusCode >= 400) {
            throw new SuprsendException(resp.responseText);
        }
        return resp.responseText;
    }

    public JSONObject broadcast(SubscriberListBroadcast subscriberListBroadcast) throws IOException, SuprsendException {
        JSONObject broadcastBody = subscriberListBroadcast.getFinalJson();
        JSONObject headers = Utils.getMergedHeaders(config);
        JSONObject sigResult = Signature.getRequestSignature(getBroadcastUrl(config), HttpMethod.POST,broadcastBody.toString(), headers, this.config.workspaceSecret);
        String contentText = sigResult.getString("contentTxt");
        headers.put("Authorization",
                String.format("%s:%s", this.config.workspaceKey, sigResult.getString("signature")));
        SuprsendResponse suprsendResponse = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, getBroadcastUrl(config), headers,
                contentText);
        JSONObject response = new JSONObject();
        if(suprsendResponse.statusCode >=400){
            response.put("success",true);
            response.put("status","success");
            response.put("status_code",suprsendResponse.statusCode);
            response.put("message",suprsendResponse.responseText);
            return response;
        }else{
            response.put("success",false);
            response.put("status","fail");
            response.put("status_code",suprsendResponse.statusCode);
            response.put("message",suprsendResponse.responseText);
            return response;
        }
    }

    private String getBroadcastUrl(Suprsend config) {
        return String.format("%s%s/broadcast/", config.baseUrl, config.workspaceKey);
    }

    private String getSubscriberListUrl(Suprsend config) {
        return String.format("%sv1/subscriber_list/", config.baseUrl);
    }
}