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
        listVersioning();
        deleteList();
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

    private static void listVersioning() throws Exception {
        logger.log(Level.INFO, "List Versioning Methods:");
        Suprsend suprClient = TestHelper.getClientInstance();
        JSONObject res = new JSONObject();
        String listId = "L00001";
        try {
            res = suprClient.subscriberLists.startSync(listId);
            System.out.println("start sync resp: ");
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
        String versionId = res.getString("version_id");
        // get version
        try {
            res = suprClient.subscriberLists.getVersion(listId, versionId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
        //
        String distinctId = "id-14980";
        ArrayList<String> distinctIds =  new ArrayList<>(Arrays.asList(distinctId));
        // add to version
        try {
            res = suprClient.subscriberLists.addToVersion(listId, versionId, distinctIds);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
        // remove from version
        try {
            res = suprClient.subscriberLists.removeFromVersion(listId, versionId, distinctIds);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
        // finish sync
        try {
            res = suprClient.subscriberLists.finishSync(listId, versionId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void deleteList() throws Exception {
        logger.log(Level.INFO, "List Delete:");
        Suprsend suprClient = TestHelper.getClientInstance();
        JSONObject res = new JSONObject();
        String listId = "L00001";
        try {
            res = suprClient.subscriberLists.startSync(listId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
        String versionId = res.getString("version_id");
        // delete version (draft list)
        try {
            res = suprClient.subscriberLists.deleteVersion(listId, versionId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
        // delete active list
        try {
            res = suprClient.subscriberLists.delete(listId);
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
