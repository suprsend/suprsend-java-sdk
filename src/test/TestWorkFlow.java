package test;

import org.json.JSONArray;
import org.json.JSONObject;
import suprsend.BulkResponse;
import suprsend.BulkWorkflows;
import suprsend.Suprsend;
import suprsend.SuprsendException;
import suprsend.Workflow;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TestWorkFlow {

    public static void main(String[] args) throws Exception {
        testWorkFlow();
        testWorkflowWithIdempotencyKey();
        testWorkFlowBulk();
    }


    private static void testWorkFlow() throws SuprsendException, UnsupportedEncodingException {
        Suprsend suprsend = TestHelper.getInstance();
        JSONObject body = getWorkFlowBody();
        Workflow wf = new Workflow(body);
        JSONObject response = suprsend.triggerWorkflow(wf);
        System.out.println(response);
    }


    public static void testWorkflowWithIdempotencyKey() throws SuprsendException, UnsupportedEncodingException {
        Suprsend suprsend = TestHelper.getInstance();
        JSONObject body = getWorkFlowBody();
        String idempotencyKey = "__uniq_id_like_uuid__";
        Workflow wf = new Workflow(body, idempotencyKey);
        JSONObject response = suprsend.triggerWorkflow(wf);
        System.out.println(response);
    }

    private static void testWorkFlowBulk() throws SuprsendException, UnsupportedEncodingException {
        BulkWorkflows bulkWorkFlows = TestHelper.getInstance().bulkWorkflowsFactory.getInstance();
        ArrayList<Workflow> workFlowsList = new ArrayList<Workflow>();
        for (int i = 0; i < 3; i++) {
            workFlowsList.add(getWorkFlow());
        }
        bulkWorkFlows.append(workFlowsList);
        BulkResponse response = bulkWorkFlows.trigger();
        System.out.println(response);
    }

    private static Workflow getWorkFlow() {
        JSONObject body = getWorkFlowBody();
        return new Workflow(body);
    }

    private static JSONObject getWorkFlowBody() {
        JSONObject body = new JSONObject()
                .put("name", "Booking Confirmed")
                .put("template", "template-booking")
                .put("notification_category", "transactional")
                .put("users", new JSONArray()
                        .put(new JSONObject()
                                .put("distinct_id", "__distinct_id__")
                                .put("$whatsapp", new JSONArray().put("+91__mobile_no__"))))
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
