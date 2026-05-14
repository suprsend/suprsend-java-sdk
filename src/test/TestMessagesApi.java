package test;

import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import suprsend.Suprsend;

public class TestMessagesApi {

	public static void main(String[] args) throws Exception {
		testList();
		testListWithFilters();
		testBulkUpdate();
	}

	public static void testList() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		JSONObject res = suprClient.messages.list();
		System.out.println(res);
	}

	public static void testListWithFilters() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
				// put("after", "<cursor_from_meta.after>");
				// put("recipient_id", Arrays.asList("user_101", "user_102"));
				// put("status", Arrays.asList("seen", "clicked"));
				// put("channel", "email");
				// put("workflow_slug", "my-workflow");
				// put("is_campaign", false);
			}
		};
		JSONObject res = suprClient.messages.list(opts);
		System.out.println(res);
	}

	public static void testBulkUpdate() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		JSONArray messages = new JSONArray()
				.put(new JSONObject().put("message_id", "<message_id_1>").put("action", "seen"))
				.put(new JSONObject().put("message_id", "<message_id_2>").put("action", "clicked"));
		JSONObject res = suprClient.messages.bulkUpdate(messages);
		System.out.println(res);
	}
}
