package test;

import org.json.JSONObject;
import suprsend.BulkEvents;
import suprsend.BulkResponse;
import suprsend.Event;
import suprsend.SuprsendException;

import java.util.ArrayList;


public class TestEvent {

    public static void main(String[] args) throws Exception {
        sendEvent();
        sendEventWithIdempotencyKey();
        sendEventBulk();
    }

    public static void sendEvent() throws Exception {
        JSONObject response = TestHelper.getInstance().trackEvent(getEvent());
        System.out.println(response);
    }

    public static void sendEventBulk() throws Exception {
        BulkEvents bulkEvents = TestHelper.getInstance().bulkEventsFactory.getInstance();
        ArrayList<Event> eventsList = new ArrayList<Event>();
        for (int i = 0; i < 3; i++) {
            eventsList.add(getEvent());
        }
        bulkEvents.append(eventsList);
        BulkResponse response = bulkEvents.trigger();
        System.out.println(response);
    }

    public static void sendEventWithIdempotencyKey() throws Exception {
        String idempotencyKey = "__uniq_id_like_uuid__";
        JSONObject response = TestHelper.getInstance().trackEvent(getEvent(idempotencyKey));
        System.out.println(response);
    }

    private static Event getEvent() throws SuprsendException {
        return getEvent(null);
    }

    private static Event getEvent(String idempotencyKey) throws SuprsendException {
        JSONObject eventProps = new JSONObject().put("k1", "v1");
        return new Event("__distinct_id__", "EVENT_NAME", eventProps, idempotencyKey);
    }
}
