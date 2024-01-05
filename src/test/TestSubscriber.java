package test;

import org.json.JSONObject;
import suprsend.BulkResponse;
import suprsend.BulkSubscribers;
import suprsend.Subscriber;
import suprsend.Suprsend;
import suprsend.SuprsendException;

import java.util.Arrays;

public class TestSubscriber {

	public static void main(String[] args) throws Exception {
		// Call the relevant function to test after adding valid values.
		testSave();
		testAddWebpush();
		testRemoveWebpush();
		testAddAndroidpush();
		testRemoveAndroidpush();
		testAddSlack();
		testRemoveSlack();
		testAddMSTeams();
		testRemoveMSTeams();
		testPreferredLanguage();
		testRemove();
		testAddHelperFunctions();
		testRemoveHelperFunctions();
		testUserPropertySet();
		testUserPropertySetOnce();
		testUserPropertyIncrement();
		testUserPropertyAppend();
		testUserPropertyRemove();
		testUnsetKey();
		testUnsetKeyMulti();
		testBulkSubscriber();
	}

	public static void testSave() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		// Subscriber Instance
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		// Add properties
		user.addEmail("example@example.com");
		user.addSms("+919999999999");
		user.addWhatsapp("+919999999999");
		// Save
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddWebpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__")
				.put("expirationTime", "")
				.put("keys", new JSONObject()
						.put("p256dh", "__p256dh__")
						.put("auth", "__auth_key__"));
		//
		user.addWebpush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveWebpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__").put("expirationTime", "").put("keys",
				new JSONObject().put("p256dh", "__p256dh__").put("auth", "__auth_key__"));
		//
		user.removeWebpush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddAndroidpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.addAndroidpush("__androidpush_token__", "fcm");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveAndroidpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.removeAndroidpush("__androidpush_token__");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddSlack() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		JSONObject slackIdent = new JSONObject()
				.put("access_token", "xoxb-asdadasda")
				.put("user_id", "u88998989")
				.put("email", "user@example.com")
				.put("channel_id", "CXXXXXXX")
				.put("incoming_webhook", new JSONObject().put("url", "https://hooks.slack.com/T0XXX/U0XXX/XXXXX"))
				;

		user.addSlack(slackIdent);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveSlack() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		JSONObject slackIdent = new JSONObject()
				.put("access_token", "xoxb-asdadasda")
				// .put("user_id", "u88998989")
				// .put("email", "user@example.com")
				.put("channel_id", "CXXXXXXX")
				// .put("incoming_webhook", new JSONObject().put("url", "https://google.com"))
		;
		user.removeSlack(slackIdent);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddMSTeams() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		JSONObject teamsIdent = new JSONObject()
				.put("tenant_id", "XXXXXXX")
				.put("service_url", "https://smba.trafficmanager.net/XXXXXXXXXX")
				.put("conversation_id", "XXXXXXXXXXXX")
				// .put("user_id", "XXXXXXXXXXXX")
				// .put("incoming_webhook", new JSONObject().put("url", "https://XXXXX.webhook.office.com/webhookb2/XXXXXXXXXX@XXXXXXXXXX/IncomingWebhook/XXXXXXXXXX/XXXXXXXXXX"))
				;

		user.addMSTeams(teamsIdent);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveMSTeams() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		JSONObject teamsIdent = new JSONObject()
				.put("tenant_id", "XXXXXXX")
				.put("service_url", "https://smba.trafficmanager.net/XXXXXXXXXX")
				.put("conversation_id", "XXXXXXXXXXXX")
				// .put("user_id", "XXXXXXXXXXXX")
				// .put("incoming_webhook", new JSONObject().put("url", "https://XXXXX.webhook.office.com/webhookb2/XXXXXXXXXX@XXXXXXXXXX/IncomingWebhook/XXXXXXXXXX/XXXXXXXXXX"))
				;
		;
		user.removeMSTeams(teamsIdent);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testPreferredLanguage() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.setPreferredLanguage("es");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemove() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.removeSms("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddHelperFunctions() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.addEmail("example@example.com");
		user.addSms("+919999999999");
		user.addWhatsapp("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveHelperFunctions() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.removeEmail("example@example.com");
		user.removeSms("+919999999999");
		user.removeWhatsapp("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUnsetKey() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.unset("$email");
		user.unset("$sms");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUnsetKeyMulti() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.unset(Arrays.asList(new String[] { "$sms", "$email" }));
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testBulkSubscriber() throws Exception {
		BulkSubscribers bulkIns = TestHelper.getClientInstance().bulkUsers.newInstance();
		for (int i = 0; i < 3; i++) {
			String distinctId = String.format("__distinct_id__%d", i);
			bulkIns.append(getSubscriber(distinctId));
		}
		//
		// Subscriber s1 = getSubscriber("__distinct_id__1");
		// Subscriber s2 = getSubscriber("__distinct_id__2");
		// Subscriber s3 = getSubscriber("__distinct_id__3");
		// bulkIns.append(s1, s2, s3);
		//
		BulkResponse response = bulkIns.save();
		System.out.println(response);
	}

	public static void testUserPropertySet() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.set("prop1", "val1");
		JSONObject userProperties = new JSONObject()
				.put("prop2", "val2")
				.put("prop3", "val3")
				.put("some", 1)
				.put("key", 1.0)
				;
		user.set(userProperties);
		user.set("prop4", 100);
		user.set("prop5", new Integer[] {1,2});
		user.set("prop6", 10.02);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUserPropertySetOnce() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.setOnce("prop1", "val1");
		JSONObject userProperties = new JSONObject()
				.put("prop2", "val2")
				.put("prop3", 1)
				;
		user.setOnce(userProperties);
		user.setOnce("prop4", 100);
		user.setOnce("prop5", 2.00);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUserPropertyIncrement() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.increment("prop1", "1");
		JSONObject userProperties = new JSONObject()
				.put("prop2", "2")
				.put("prop3", 3)
				;
		user.increment(userProperties);
		user.increment("prop4", 1);
		user.increment("prop5", 2.0);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUserPropertyAppend() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.append("prop1", "1");
		JSONObject userProperties = new JSONObject()
				.put("prop_append", "val_append")
				.put("prop_append2", "23")
				;
		user.append(userProperties);
		user.append("prop4", 1.0);
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUserPropertyRemove() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		Subscriber user = suprClient.user.getInstance(distinctId);
		//
		user.remove("prop1", "1");
		JSONObject userProperties = new JSONObject()
				.put("prop_append", "val_append")
				.put("prop_append2", "23")
				;
		user.remove(userProperties);
		user.remove("prop4", 1.0);
		JSONObject res = user.save();
		System.out.println(res);
	}

	private static Subscriber getSubscriber(String distinctId) throws SuprsendException {
		Suprsend suprClient = TestHelper.getClientInstance();
		// 
		Subscriber user = suprClient.user.getInstance(distinctId);
		user.removeSms("+919999999999");
		return user;
	}
}
