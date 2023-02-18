package test;


import org.json.JSONObject;
import suprsend.SubscriberListBroadcast;
import suprsend.SubscriberListsApi;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TestSubscriberList {
    private static final Logger logger = Logger.getLogger(TestSubscriberList.class.getName());

    //TODO - i am not able to crete list with contains space
    private static final String LIST_KEY_NAME = "automation_list";
    private static final String IDEMPOTENCY_KEY = "automation_idempotency_key";
    private static final String BRAND_ID = "automation_brand_id";
    private static final String DISTINCT_ID = "id-14980";
    private static final String TEMPLATE = "hello";
    private static final String NOTIFICATION_CATEGORY = "promotional";

    public static void main(String[] args) throws Exception {
        createList();
        addItem();
        getAll();
        get();
        getAllLimit();
        remove();
        broadcast();
    }

    private static void createList() throws Exception {
        logger.log(Level.INFO, "List Create");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        JSONObject payload = new JSONObject();
        payload.put("list_id", LIST_KEY_NAME);
        subscriberListsApi.create(payload);
    }

    private static void addItem() throws Exception {
        logger.log(Level.INFO, "List Add");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        ArrayList<String> distinctIds = new ArrayList<>();
        distinctIds.add(DISTINCT_ID);
        subscriberListsApi.add(LIST_KEY_NAME, distinctIds);
    }

    private static void get() throws Exception {
        logger.log(Level.INFO, "List Get");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        //TODO - List is returning ok
        subscriberListsApi.get(LIST_KEY_NAME);
    }

    private static void getAll() throws Exception {
        logger.log(Level.INFO, "List Get All");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        subscriberListsApi.getAll();
    }

    private static void getAllLimit() throws Exception {
        logger.log(Level.INFO, "List Get All Limit");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        subscriberListsApi.getAll(10);
    }

    private static void remove() throws Exception {
        logger.log(Level.INFO, "List Remove");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        ArrayList<String> distinctIds = new ArrayList<>();
        distinctIds.add(DISTINCT_ID);
        subscriberListsApi.remove(LIST_KEY_NAME,distinctIds);
    }

    private static void broadcast() throws Exception {
        logger.log(Level.INFO, "Broadcast");
        SubscriberListsApi subscriberListsApi = TestHelper.getInstance().subscriberListsApi;
        JSONObject body = new JSONObject();
        JSONObject data = new JSONObject();
        body.put("list_id",LIST_KEY_NAME);
        body.put("template",TEMPLATE);
        body.put("notification_category",NOTIFICATION_CATEGORY);
        body.put("data",data);
        SubscriberListBroadcast subscriberListBroadcast = new SubscriberListBroadcast(body,IDEMPOTENCY_KEY,BRAND_ID);
        subscriberListsApi.broadcast(subscriberListBroadcast);
    }
}
