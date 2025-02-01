package test;

import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

import suprsend.BulkResponse;
import suprsend.BulkUsersEdit;
import suprsend.Suprsend;
import suprsend.SuprsendException;
import suprsend.UserEdit;

public class TestUsersApi {

	public static void main(String[] args) throws Exception {
		testGet();
		testList();
		testUpsert();
		testAsyncEdit();
		testEdit();
		testEditHelper();
		testMerge();
		testDelete();
		testBulkDelete();
		testGetObjectsSubscribedTo();
		testGetListsSubscribedTo();
		//
		testAddWebpush();
		testRemoveWebpush();
		testAddAndroidpush();
		testRemoveAndroidpush();
		testAddSlack();
		testRemoveSlack();
		testAddMSTeams();
		testRemoveMSTeams();
		testPreferredLanguage();
		testTimezone();
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
		testBulkUsersEdit();
	}

	public static void testGet() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		String distinctId = "101";
		// response
		JSONObject res = suprClient.users.get(distinctId);
		System.out.println(res);
	}

	public static void testList() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
				// put("after", "01JHGC0EYVHBBHV7SS3595MJF8");
			}
		};
		// JSONObject res = suprClient.users.list();
		JSONObject res = suprClient.users.list(opts);
		System.out.println(res);
	}

	public static void testUpsert() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "101";
		JSONObject payload = new JSONObject().put("name", "John Doe").put("$email", "101+test@example.com");
		JSONObject user = suprClient.users.upsert(distinctId, payload);
		System.out.println(user);
	}

	public static void testAsyncEdit() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		String distinctId = "101";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		user.addEmail("101+123@example.com");
		user.set(new JSONObject().put("name", "John Doe"));
		//
		JSONObject res = suprClient.users.asyncEdit(user);
		System.out.println(res);
	}

	public static void testEdit() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "101";
		JSONObject payload = new JSONObject().put("operations",
				new JSONObject[] { new JSONObject().put("$set", new JSONObject().put("name", "John Doe")) });
		JSONObject res = suprClient.users.edit(distinctId, payload);
		System.out.println(res);
	}

	public static void testEditHelper() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "101";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		// Add properties
		user.addEmail("example@example.com");
		user.addSms("+919999999999");
		user.addWhatsapp("+919999999999");
		//
		user.append("arr_key", "v1");
		user.append(new JSONObject().put("arr_key", "v2"));
		user.append("arr_key", 12);
		//
		user.set("key1", "v1");
		user.set(new JSONObject().put("key2", "v2"));
		//
		user.setOnce("once_key1", "v1");
		user.setOnce(new JSONObject().put("once_key1", "v2"));
		//
		user.increment("increment_key", 1);
		user.increment(new JSONObject().put("increment_key", -1));
		//
		user.remove("arr_key", "v2");
		user.remove(new JSONObject().put("arr_key", "v3"));
		//
		user.unset("key4");
		// Call edit api
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testMerge() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		String distinctId = "101";
		String fromUserId = "102";
		// response
		JSONObject res = suprClient.users.merge(distinctId, fromUserId);
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

	public static void testBulkDelete() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		JSONObject payload = new JSONObject().put("distinct_ids", Arrays.asList("101", "102"));
		JSONObject response = suprClient.users.bulkDelete(payload);
		System.out.println(response);
	}

	public static void testGetObjectsSubscribedTo() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "101";
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
				put("after", "");
			}
		};
		JSONObject response = suprClient.users.getObjectsSubscribedTo(distinctId, opts);
		System.out.println(response);
	}

	public static void testGetListsSubscribedTo() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "101";
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
				// put("after", "01HFS04E4J29KHPYRK7HT3YQQ5");
			}
		};
		JSONObject response = suprClient.users.getListsSubscribedTo(distinctId, opts);
		System.out.println(response);
	}

	public static void testAddWebpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__")
				.put("expirationTime", "")
				.put("keys", new JSONObject().put("p256dh", "__p256dh__").put("auth", "__auth_key__"));
		//
		user.addWebpush(webpush, "vapid");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testRemoveWebpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__")
				.put("expirationTime", "")
				.put("keys", new JSONObject().put("p256dh", "__p256dh__").put("auth", "__auth_key__"));
		//
		user.removeWebpush(webpush, "vapid");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testAddAndroidpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.addAndroidpush("__androidpush_token__");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testRemoveAndroidpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.removeAndroidpush("__androidpush_token__");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testAddSlack() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		JSONObject slackIdent = new JSONObject().put("access_token", "xoxb-asdadasda")
				.put("user_id", "u88998989")
				.put("email", "user@example.com")
				.put("channel_id", "CXXXXXXX")
				.put("incoming_webhook", new JSONObject().put("url", "https://hooks.slack.com/T0XXX/U0XXX/XXXXX"));

		user.addSlack(slackIdent);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testRemoveSlack() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		JSONObject slackIdent = new JSONObject().put("access_token", "xoxb-asdadasda")
				// .put("user_id", "u88998989")
				// .put("email", "user@example.com")
				.put("channel_id", "CXXXXXXX")
		// .put("incoming_webhook", new JSONObject().put("url",
		// "https://hooks.slack.com/T0XXX/U0XXX/XXXXX"))
		;
		user.removeSlack(slackIdent);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testAddMSTeams() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		JSONObject teamsIdent = new JSONObject().put("tenant_id", "XXXXXXX")
				.put("service_url", "https://smba.trafficmanager.net/XXXXXXXXXX")
				.put("conversation_id", "XXXXXXXXXXXX")
		// .put("user_id", "XXXXXXXXXXXX")
		// .put("incoming_webhook", new JSONObject().put("url",
		// "https://XXXXX.webhook.office.com/webhookb2/XXXXXXXXXX@XXXXXXXXXX/IncomingWebhook/XXXXXXXXXX/XXXXXXXXXX"))
		;

		user.addMSTeams(teamsIdent);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testRemoveMSTeams() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		JSONObject teamsIdent = new JSONObject().put("tenant_id", "XXXXXXX")
				.put("service_url", "https://smba.trafficmanager.net/XXXXXXXXXX")
				.put("conversation_id", "XXXXXXXXXXXX")
		// .put("user_id", "XXXXXXXXXXXX")
		// .put("incoming_webhook", new JSONObject().put("url",
		// "https://XXXXX.webhook.office.com/webhookb2/XXXXXXXXXX@XXXXXXXXXX/IncomingWebhook/XXXXXXXXXX/XXXXXXXXXX"))
		;
		user.removeMSTeams(teamsIdent);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testPreferredLanguage() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.setPreferredLanguage("es");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testTimezone() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.setTimezone("America/Los_Angeles");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testRemove() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.removeSms("+919999999999");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testAddHelperFunctions() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.addEmail("example@example.com");
		user.addSms("+919999999999");
		user.addWhatsapp("+919999999999");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testRemoveHelperFunctions() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.removeEmail("example@example.com");
		user.removeSms("+919999999999");
		user.removeWhatsapp("+919999999999");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUserPropertySet() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.set("prop1", "val1");
		JSONObject userProperties = new JSONObject().put("prop2", "val2")
				.put("prop3", "val3")
				.put("some", 1)
				.put("key", 1.0);
		user.set(userProperties);
		user.set("prop4", 100);
		user.set("prop5", new Integer[] { 1, 2 });
		user.set("prop6", 10.02);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUserPropertySetOnce() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.setOnce("prop1", "val1");
		JSONObject userProperties = new JSONObject().put("prop2", "val2").put("prop3", 1);
		user.setOnce(userProperties);
		user.setOnce("prop4", 100);
		user.setOnce("prop5", 2.00);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUserPropertyIncrement() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.increment("prop1", "1");
		JSONObject userProperties = new JSONObject().put("prop2", "2").put("prop3", 3);
		user.increment(userProperties);
		user.increment("prop4", 1);
		user.increment("prop5", 2.0);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUserPropertyAppend() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.append("prop1", "1");
		JSONObject userProperties = new JSONObject().put("prop_append", "val_append").put("prop_append2", "23");
		user.append(userProperties);
		user.append("prop4", 1.0);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUserPropertyRemove() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.remove("prop1", "1");
		JSONObject userProperties = new JSONObject().put("prop_append", "val_append").put("prop_append2", "23");
		user.remove(userProperties);
		user.remove("prop4", 1.0);
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUnsetKey() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.unset("$email");
		user.unset("$sms");
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testUnsetKeyMulti() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String distinctId = "__distinct_id__";
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		//
		user.unset(Arrays.asList(new String[] { "$sms", "$email" }));
		JSONObject res = suprClient.users.edit(user);
		System.out.println(res);
	}

	public static void testBulkUsersEdit() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		BulkUsersEdit bulkIns = suprClient.users.getBulkEditInstance();
		for (int i = 0; i < 3; i++) {
			String distinctId = String.format("__distinct_id__%d", i);
			bulkIns.append(getEditInstance(distinctId));
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

	private static UserEdit getEditInstance(String distinctId) throws SuprsendException {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		UserEdit user = suprClient.users.getEditInstance(distinctId);
		user.removeSms("+919999999999");
		return user;
	}
}
