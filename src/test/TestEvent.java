package test;

import suprsend.Suprsend;
import suprsend.Event;
import org.json.JSONObject;


public class TestEvent {
    private static Suprsend suprsend;

    public static void main(String[] args) throws Exception {
		
	}

    public static void sendEvent() throws Exception {
        suprsend = new Suprsend("workspace_key", "workspace_secret");
        JSONObject eventProps = new JSONObject()
            .put("k1", "v1");
        // 
        Event e = new Event("__distinct_id__", "EVENT_NAME", eventProps);
        JSONObject response = suprsend.trackEvent(e);
        System.out.println(response);
    }

    public static void sendEventWithIdempotencyKey() throws Exception {
        suprsend = new Suprsend("workspace_key", "workspace_secret");
        JSONObject eventProps = new JSONObject()
            .put("k1", "v1");
        String idempotencyKey = "__uniq_id_like_uuid__";
        // 
        Event e = new Event("__distinct_id__", "EVENT_NAME", eventProps, idempotencyKey);
        JSONObject response = suprsend.trackEvent(e);
        System.out.println(response);
    }
}
