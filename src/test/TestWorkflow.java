package test;

import org.json.JSONArray;
import org.json.JSONObject;
import suprsend.BulkResponse;
import suprsend.BulkWorkflowTrigger;
import suprsend.Suprsend;
import suprsend.SuprsendException;
import suprsend.WorkflowTriggerRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TestWorkflow {

	public static void main(String[] args) throws Exception {
		testWorkflowTrigger();
		testWorkflowTriggerWithIdempotencyKey();
		testWorkflowBulkTrigger();
	}

	private static void testWorkflowTrigger() throws SuprsendException, UnsupportedEncodingException {
		Suprsend suprClient = TestHelper.getClientInstance();
		// payload
		JSONObject body = getWorkflowBody();
		WorkflowTriggerRequest wf = new WorkflowTriggerRequest(body);
		//
		JSONObject resp = suprClient.workflows.trigger(wf);
		System.out.println(resp);
	}

	public static void testWorkflowTriggerWithIdempotencyKey() throws SuprsendException, IOException {
		Suprsend suprClient = TestHelper.getClientInstance();
		// payload
		JSONObject body = getWorkflowBody();
		String idempotencyKey = "__uniq_id_like_uuid__";
		String tenantId = "default";
		WorkflowTriggerRequest wf = new WorkflowTriggerRequest(body, idempotencyKey, tenantId);
		// String filePath = "https://lightning.network/lightning-network-paper.pdf";
		// String filePath = "~/Downloads/gfs-sosp2003.pdf";
		// wf.addAttachment(filePath, "MyFile.pdf", true);
		//
		JSONObject resp = suprClient.workflows.trigger(wf);
		System.out.println(resp);
	}

	private static void testWorkflowBulkTrigger() throws SuprsendException {
		Suprsend suprClient = TestHelper.getClientInstance();
		// payload
		BulkWorkflowTrigger bulkIns = suprClient.workflows.bulkTriggerInstance();
		for (int i = 0; i < 3; i++) {
			WorkflowTriggerRequest wf = getWorkflow();
			bulkIns.append(wf);
		}
		BulkResponse resp = bulkIns.trigger();
		System.out.println(resp);
	}

	private static WorkflowTriggerRequest getWorkflow() throws SuprsendException {
		JSONObject body = getWorkflowBody();
		WorkflowTriggerRequest wf = new WorkflowTriggerRequest(body);
		// String filePath = "https://lightning.network/lightning-network-paper.pdf";
		// String filePath = "~/Downloads/gfs-sosp2003.pdf";
		// wf.addAttachment(filePath, "MyFile.pdf", true);
		// wf.addAttachment(filePath, "MyFile2.pdf", true);
		return wf;
	}

	private static JSONObject getWorkflowBody() {
		JSONObject body = new JSONObject()
				.put("workflow", "__workflow_slug__")
				.put("actor", new JSONObject()
								.put("distinct_id", "__actor_id__")
								.put("$email", "actor@example.com")
								.put("name", "Actor Name")
					)
				.put("recipients", new JSONArray()
						.put(new JSONObject()
								.put("distinct_id", "__recipient_id_1__")
								// .put("is_transient", true)
								// .put("$channels", Arrays.asList("email"))
								.put("$email", Arrays.asList("recp1@example.com"))
								.put("name", "Recipient 1 Name")
								// .put("$slack", new JSONObject()
								// 		.put("incoming_webhook", new JSONObject()
								// 				.put("url", "https://hooks.slack.com/services/T0XXXXX/B0XXXXXX/XXXXXXX")))
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
						.put("answers", new JSONArray()
								.put(new JSONObject().put("answer", "Finance")
										.put("question", "Answer my question"))
								)
						);

		return body;
	}
}
