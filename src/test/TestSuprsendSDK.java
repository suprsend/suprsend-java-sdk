package test;

import org.json.JSONArray;
import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.Workflow;


public class TestSuprsendSDK {

	private static Suprsend suprsend;

	private static JSONObject getBody() {
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

	public static void main(String[] args) throws Exception {
		suprsend = new Suprsend("workspace_key", "workspace_secret");
		JSONObject body = getBody();
		Workflow wf = new Workflow(body);
		JSONObject response = suprsend.triggerWorkflow(wf);
		System.out.println(response);
	}

	public static void testWithIdempotencyKey(String[] args) throws Exception {
		suprsend = new Suprsend("workspace_key", "workspace_secret");
		JSONObject body = getBody();
		String idempotencyKey = "__uniq_id_like_uuid__";
		// 
		Workflow wf = new Workflow(body, idempotencyKey);
		JSONObject response = suprsend.triggerWorkflow(wf);
		System.out.println(response);
	}

}
