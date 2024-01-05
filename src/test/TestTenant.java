package test;

import java.util.logging.Logger;

import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.SuprsendException;

public class TestTenant {
    private static final Logger logger = Logger.getLogger(TestSubscriberList.class.getName());

    public static void main(String[] args) throws Exception {
        list();
        get();
        upsert();
        delete();
    }

    private static void list() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        try {
            JSONObject res = suprClient.tenants.list();
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void get() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        //
        try {
            String tenantId = "t00001";
            JSONObject res = suprClient.tenants.get(tenantId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void upsert() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        // 
        String tenantId = " t00001";
        JSONObject payload = new JSONObject()
            .put("tenant_name", "Tenant 1")
            .put("logo", "https://google.com")
            .put("primary_color", "#2a062c")
            .put("secondary_color", "#0000ff")
            .put("tertiary_color", "#00ffff")
            .put("social_links", new JSONObject()
                                        .put("website", "https://www.company.com")
                                        .put("youtube", "https://www.company.com")
                                        .put("twitter", "https://www.company.com")
                                        .put("facebook", "https://www.company.com")
                                        .put("linkedin", "https://www.company.com")
                                        .put("instagram", "https://www.company.com")
                                        .put("discord", "https://www.company.com")
                                        .put("medium", "")
                                        .put("telegram", "")
                )
            .put("properties", new JSONObject()
                                        .put("address", "my company address")
                                        .put("k1", "v1")
                                        .put("k2", 1)
                                        .put("k3", new JSONObject().put("nested_k", "v"))
                )
            ;
        try {
            JSONObject res = suprClient.tenants.upsert(tenantId, payload);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }

    private static void delete() throws Exception {
        Suprsend suprClient = TestHelper.getClientInstance();
        //
        try {
            String tenantId = "t00001";
            JSONObject res = suprClient.tenants.delete(tenantId);
            System.out.println(res);
        } catch (SuprsendException e) {
            System.out.println(e);
        }
    }
}
