package test;

import org.json.JSONObject;
import suprsend.SubscriberListBroadcast;
import suprsend.Suprsend;
import suprsend.SuprsendException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestSubscriberList {
    private static final Logger logger = Logger.getLogger(TestSubscriberList.class.getName());

    public static void main(String[] args) throws Exception {
        getAll();
        get();
        createList();
        addToList();
        removeFromList();
        broadcast();
    }

    private static void getAll() throws Exception {
        logger.log(Level.INFO, "List Get All");
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        try {
            JSONObject res = suprClient.subscriberLists.getAll();
            // JSONObject res = suprClient.subscriberLists.getAll(10);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void get() throws Exception {
        logger.log(Level.INFO, "List Get");
        Suprsend suprClient = TestHelper.getClientInstance();
        //
        try {
            String listId = "L00001";
            JSONObject res = suprClient.subscriberLists.get(listId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void createList() throws Exception {
        logger.log(Level.INFO, "List Create");
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        JSONObject payload = new JSONObject().put("list_id", "L00001")
                                            .put("list_name", "L-00001")
                                            .put("list_description", "L-00001 main list");
        try {
            JSONObject res = suprClient.subscriberLists.create(payload);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void addToList() throws Exception {
        logger.log(Level.INFO, "List Add");
        Suprsend suprClient = TestHelper.getClientInstance();
        //
        String distinctId = "id-14980";
        ArrayList<String> distinctIds = new ArrayList<>(Arrays.asList(distinctId));
        try {
            String listId = "L00001";
            JSONObject res = suprClient.subscriberLists.add(listId, distinctIds);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void removeFromList() throws Exception {
        logger.log(Level.INFO, "List Remove");
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        String distinctId = "id-14980";
        ArrayList<String> distinctIds =  new ArrayList<>(Arrays.asList(distinctId));
        try {
            String listId = "L00001";
            JSONObject res = suprClient.subscriberLists.remove(listId, distinctIds);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void broadcast() throws Exception {
        logger.log(Level.INFO, "Broadcast");
        Suprsend suprClient = TestHelper.getClientInstance();
        // payload
        String listId = "L00001";
        String templateSlug = "hello";
        String notifCategory = "promotional";
        JSONObject body = new JSONObject().put("list_id",listId)
                                .put("template",templateSlug)
                                .put("notification_category",notifCategory)
                                .put("data", new JSONObject());
        String idempKey = "__idempotency_key__";
        String brandId = "__brand_id__";
        // 
        SubscriberListBroadcast broadcastIns = new SubscriberListBroadcast(body);
        // SubscriberListBroadcast broadcastIns = new SubscriberListBroadcast(body, idempKey);
        // SubscriberListBroadcast broadcastIns = new SubscriberListBroadcast(body, idempKey, brandId);
        JSONObject res = suprClient.subscriberLists.broadcast(broadcastIns);
        System.out.println(res);
    }
}
