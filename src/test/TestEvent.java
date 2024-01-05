package test;

import org.json.JSONObject;
import suprsend.BulkEvents;
import suprsend.BulkResponse;
import suprsend.Event;
import suprsend.Suprsend;
import suprsend.SuprsendException;


public class TestEvent {

    public static void main(String[] args) throws Exception {
        sendEvent();
        sendEventWithIdempotencyKey();
        sendEventBulk();
    }

    public static void sendEvent() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        JSONObject response = suprClient.trackEvent(getEvent(null, null));
        System.out.println(response);
    }

    public static void sendEventWithIdempotencyKey() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        String idempotencyKey = "__uniq_id_like_uuid__";
        String tenantId = "default";
        JSONObject response = suprClient.trackEvent(getEvent(idempotencyKey, tenantId));
        System.out.println(response);
    }

    public static void sendEventBulk() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        BulkEvents bulkIns = suprClient.bulkEvents.newInstance();
        for (int i = 0; i < 3; i++) {
            bulkIns.append(getEvent(null, null));
        }
        BulkResponse response = bulkIns.trigger();
        System.out.println(response);
    }

    private static Event getEvent(String idempotencyKey, String tenantId) throws SuprsendException {
        JSONObject eventProps = new JSONObject().put("k1", "v1");
        Event e = new Event("__distinct_id__", "EVENT_NAME", eventProps, 
            idempotencyKey, tenantId);
        // String filePath = "https://lightning.network/lightning-network-paper.pdf";
        // String filePath = "~/Downloads/gfs-sosp2003.pdf"; 
        // e.addAttachment(filePath, "MyFile.pdf", true);
        // e.addAttachment(filePath, "MyFile2.pdf", true);
        return e;
    }

}
