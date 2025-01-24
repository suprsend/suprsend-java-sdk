package test;

import org.json.JSONObject;

import suprsend.Suprsend;

public class TestUserApi {

	public static void main(String[] args) throws Exception {
		testGet();
		testDelete();
	}

	public static void testGet() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		String distinctId = "101";
		// response
		JSONObject res = suprClient.users.get(distinctId);
		System.out.println(res);
	}

	public static void testDelete() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		String distinctId = "101";
		// response
		JSONObject res = suprClient.users.delete(distinctId);
		System.out.println(res);
	}
}
