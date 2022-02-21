package test;

import org.json.JSONArray;
import org.json.JSONObject;

import suprsend.Suprsend;

public class TestSuprsendSDK {
	
	private static Suprsend suprsend;
	
	private static JSONObject getBody() {
		JSONArray whatsapp = new JSONArray();
		JSONArray users = new JSONArray();
		JSONArray answers = new JSONArray();
		JSONObject user = new JSONObject();
		JSONObject answer = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject body = new JSONObject();
		
		whatsapp.put("+91__mobile_no__");
		
		user.put("distinct_id", "__distinct_id__");
		user.put("$whatsapp", whatsapp);
		users.put(user);
		
		answer.put("answer", "Finance");
		answer.put("question", "Answer my question");
		answers.put(answer);
		
		data.put("time", "Tue, 17-Aug-2021, 12:30 AM (Asia/Dubai)");
		data.put("price", "23");
		data.put("call_with", "Alex");
		data.put("expert_name", "Mike");
		data.put("time_to_call", "3PM");
		data.put("consumer_name", "Joe");
		data.put("service_title", "Points");
		data.put("videocall_link", "https://dummy");
		data.put("answers", answers);
		
		body.put("name", "Booking Confirmed");
		body.put("template", "template-booking");
		body.put("notification_category", "transactional");
		body.put("users", users);
		body.put("data", data);
		return body;
	}
	
	public static void main(String[] args) throws Exception {
		JSONObject body = getBody();
		suprsend = new Suprsend("__env_key__", "__env_secret__");
		JSONObject response = suprsend.triggerWorkflow(body);
		System.out.println(response);
	}

}
