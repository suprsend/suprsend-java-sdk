package test;

import org.json.JSONArray;
import org.json.JSONObject;
import suprsend.BulkResponse;
import suprsend.BulkWorkflows;
import suprsend.Suprsend;
import suprsend.SuprsendException;
import suprsend.Workflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TestWorkflow {

    public static void main(String[] args) throws Exception {
        testWorkflow();
        testWorkflowWithIdempotencyKey();
        testWorkflowBulk();
    }

    private static void testWorkflow() throws SuprsendException, UnsupportedEncodingException {
        Suprsend suprClient = TestHelper.getClientInstance();
        // payload
        JSONObject body = getWorkflowBody();
        Workflow wf = new Workflow(body);
        // 
        JSONObject resp = suprClient.triggerWorkflow(wf);
        System.out.println(resp);
    }


    public static void testWorkflowWithIdempotencyKey() throws SuprsendException, IOException {
        Suprsend suprClient = TestHelper.getClientInstance();
        // payload
        JSONObject body = getWorkflowBody();
        String idempotencyKey = "__uniq_id_like_uuid__";
        String tenantId = "default";
        Workflow wf = new Workflow(body, idempotencyKey, tenantId);
        // String filePath = "https://lightning.network/lightning-network-paper.pdf";
        // String filePath = "~/Downloads/gfs-sosp2003.pdf"; 
        // wf.addAttachment(filePath, "MyFile.pdf", true);
        // 
        JSONObject resp = suprClient.triggerWorkflow(wf);
        System.out.println(resp);
    }

    private static void testWorkflowBulk() throws SuprsendException {
        Suprsend suprClient = TestHelper.getClientInstance();
        // payload
        BulkWorkflows bulkIns = suprClient.bulkWorkflows.newInstance();
        for (int i = 0; i < 3; i++) {
            bulkIns.append(getWorkflow());
        }
        BulkResponse resp = bulkIns.trigger();
        System.out.println(resp);
    }

    private static Workflow getWorkflow() throws SuprsendException {
        JSONObject body = getWorkflowBody();
        Workflow wf = new Workflow(body);
        // String filePath = "https://lightning.network/lightning-network-paper.pdf";
        // String filePath = "~/Downloads/gfs-sosp2003.pdf"; 
        // wf.addAttachment(filePath, "MyFile.pdf", true);
        // wf.addAttachment(filePath, "MyFile2.pdf", true);
        return wf;
    }

    private static JSONObject getWorkflowBody() {
        JSONObject body = new JSONObject()
                .put("name", "Booking Confirmed")
                .put("template", "template-booking")
                .put("notification_category", "transactional")
                .put("users", new JSONArray()
                        .put(new JSONObject()
                                .put("distinct_id", "__distinct_id__")
                                // .put("is_transient", true)
                                .put("$channels", Arrays.asList("slack"))
                                .put("$slack", new JSONObject().put("incoming_webhook", new JSONObject().put("url", "https://hooks.slack.com/services/T0XXXXX/B0XXXXXX/XXXXXXX")))
                                // .put("$whatsapp", new JSONArray().put("+91__mobile_no__"))
                            ))
                .put("data", new JSONObject()
                        .put("time", "Tue, 17-Aug-2021, 12:30 AM (Asia/Dubai)")
                        .put("price", "23")
                        .put("call_with", "Alex")
                        .put("expert_name", "Mike")
                        .put("time_to_call", "3PM")
                        .put("consumer_name", "Joe")
                        .put("service_title", "Points")
                        .put("videocall_link", "https://dummy")
                        .put("answers", new JSONArray().put(new JSONObject()
                                .put("answer", "Finance")
                                .put("question", "Answer my question"))));

        return body;
    }
}
