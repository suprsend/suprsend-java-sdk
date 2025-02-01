package test;

import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONObject;

import suprsend.ObjectEdit;
import suprsend.Suprsend;

public class TestObjectsApi {

	public static void main(String[] args) throws Exception {
		// crud operations
		testUpsert();
		testList();
		testGet();
		testEdit();
		testDelete();
		testBulkDelete();
		testGetSubscriptions();
		testCreateSubscriptions();
		testDeleteSubscriptions();
		testGetObjectsSubscribedTo();
		// Call the relevant function to test after adding valid values.
		testEditHelper();
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
		testUnsetKey();
		testUnsetKeyMulti();
		testObjectPropertySet();
		testObjectPropertySetOnce();
		testObjectPropertyIncrement();
		testObjectPropertyAppend();
		testObjectPropertyRemove();
	}

	public static void testUpsert() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		JSONObject payload = new JSONObject().put("name", "John Doe");
		JSONObject object = suprClient.objects.upsert(objectType, objectId, payload);
		System.out.println(object);
	}

	public static void testList() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
			}
		};
		JSONObject response = suprClient.objects.list(objectType, opts);
		System.out.println(response);
	}

	public static void testGet() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		JSONObject object = suprClient.objects.get(objectType, objectId);
		System.out.println(object);
	}

	public static void testEdit() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		JSONObject payload = new JSONObject().put("operations",
				new JSONObject[] { new JSONObject().put("$set", new JSONObject().put("name", "John Doe")) });
		JSONObject object = suprClient.objects.edit(objectType, objectId, payload);
		System.out.println(object);
	}

	public static void testDelete() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		JSONObject response = suprClient.objects.delete(objectType, objectId);
		System.out.println(response);
	}

	public static void testBulkDelete() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		JSONObject payload = new JSONObject().put("object_ids", Arrays.asList("__objectid__"));
		JSONObject response = suprClient.objects.bulkDelete(objectType, payload);
		System.out.println(response);
	}

	public static void testGetSubscriptions() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
			}
		};
		JSONObject response = suprClient.objects.getSubscriptions(objectType, objectId, opts);
		System.out.println(response);
	}

	public static void testCreateSubscriptions() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		JSONObject payload = new JSONObject().put("recipients",
				Arrays.asList("__recipient_id_1__", new JSONObject().put("distinct_id", "__recipient_id_2__"),
						new JSONObject().put("object_type", "__objecttype__").put("id", "__objectid_2_")));
		JSONObject response = suprClient.objects.createSubscriptions(objectType, objectId, payload);
		System.out.println(response);
	}

	public static void testDeleteSubscriptions() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		JSONObject payload = new JSONObject().put("recipients",
				Arrays.asList("__recipient_id_1__", new JSONObject().put("distinct_id", "__recipient_id_2__"),
						new JSONObject().put("object_type", "__objecttype__").put("id", "__objectid_2_")));
		JSONObject response = suprClient.objects.deleteSubscriptions(objectType, objectId, payload);
		System.out.println(response);
	}

	public static void testGetObjectsSubscribedTo() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		HashMap<String, Object> opts = new HashMap<String, Object>() {
			{
				put("limit", 10);
			}
		};
		JSONObject response = suprClient.objects.getObjectsSubscribedTo(objectType, objectId, opts);
		System.out.println(response);
	}

	public static void testEditHelper() throws Exception {
		// SDK instance
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		// Add properties
		object.addEmail("example@example.com");
		object.addSms("+919999999999");
		object.addWhatsapp("+919999999999");
		// Call edit api
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testAddWebpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid1__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__").put("expirationTime", "").put("keys",
				new JSONObject().put("p256dh", "__p256dh__").put("auth", "__auth_key__"));
		//
		object.addWebpush(webpush, "vapid");
		//
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testRemoveWebpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__").put("expirationTime", "").put("keys",
				new JSONObject().put("p256dh", "__p256dh__").put("auth", "__auth_key__"));
		//
		object.removeWebpush(webpush, "vapid");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testAddAndroidpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.addAndroidpush("__androidpush_token__", "fcm");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testRemoveAndroidpush() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.removeAndroidpush("__androidpush_token__");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testAddSlack() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		JSONObject slackIdent = new JSONObject().put("access_token", "xoxb-asdadasda")
				// .put("user_id", "u88998989")
				// .put("email", "user@example.com")
				.put("channel_id", "CXXXXXXX")
				.put("incoming_webhook", new JSONObject().put("url", "https://hooks.slack.com/T0XXX/U0XXX/XXXXX"));

		object.addSlack(slackIdent);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testRemoveSlack() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		JSONObject slackIdent = new JSONObject().put("access_token", "xoxb-asdadasda")
				// .put("user_id", "u88998989")
				// .put("email", "user@example.com")
				.put("channel_id", "CXXXXXXX")
		// .put("incoming_webhook", new JSONObject().put("url", "https://google.com"))
		;
		object.removeSlack(slackIdent);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testAddMSTeams() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		JSONObject teamsIdent = new JSONObject().put("tenant_id", "XXXXXXX")
				.put("service_url", "https://smba.trafficmanager.net/XXXXXXXXXX").put("conversation_id", "XXXXXXXXXXXX")
		// .put("user_id", "XXXXXXXXXXXX")
		// .put("incoming_webhook", new JSONObject().put("url",
		// "https://XXXXX.webhook.office.com/webhookb2/XXXXXXXXXX@XXXXXXXXXX/IncomingWebhook/XXXXXXXXXX/XXXXXXXXXX"))
		;

		object.addMSTeams(teamsIdent);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testRemoveMSTeams() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		JSONObject teamsIdent = new JSONObject().put("tenant_id", "XXXXXXX")
				.put("service_url", "https://smba.trafficmanager.net/XXXXXXXXXX").put("conversation_id", "XXXXXXXXXXXX")
		// .put("user_id", "XXXXXXXXXXXX")
		// .put("incoming_webhook", new JSONObject().put("url",
		// "https://XXXXX.webhook.office.com/webhookb2/XXXXXXXXXX@XXXXXXXXXX/IncomingWebhook/XXXXXXXXXX/XXXXXXXXXX"))
		;
		object.removeMSTeams(teamsIdent);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testPreferredLanguage() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.setPreferredLanguage("es");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testTimezone() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.setTimezone("America/Los_Angeles");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testRemove() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.removeSms("+919999999999");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testAddHelperFunctions() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.addEmail("example@example.com");
		object.addSms("+919999999999");
		object.addWhatsapp("+919999999999");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testRemoveHelperFunctions() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.removeEmail("example@example.com");
		object.removeSms("+919999999999");
		object.removeWhatsapp("+919999999999");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testUnsetKey() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.unset("$email");
		object.unset("$sms");
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testUnsetKeyMulti() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.unset(Arrays.asList(new String[] { "$sms", "$email" }));
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testObjectPropertySet() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.set("prop1", "val1");
		JSONObject objectsProperties = new JSONObject().put("prop2", "val2").put("prop3", "val3").put("some", 1)
				.put("key", 1.0);
		object.set(objectsProperties);
		object.set("prop4", 100);
		object.set("prop5", new Integer[] { 1, 2 });
		object.set("prop6", 10.02);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testObjectPropertySetOnce() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.setOnce("prop1", "val1");
		JSONObject objectsProperties = new JSONObject().put("prop2", "val2").put("prop3", 1);
		object.setOnce(objectsProperties);
		object.setOnce("prop4", 100);
		object.setOnce("prop5", 2.00);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testObjectPropertyIncrement() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.increment("prop1", "1");
		JSONObject objectsProperties = new JSONObject().put("prop2", "2").put("prop3", 3);
		object.increment(objectsProperties);
		object.increment("prop4", 1);
		object.increment("prop5", 2.0);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testObjectPropertyAppend() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.append("prop1", "1");
		JSONObject objectsProperties = new JSONObject().put("prop_append", "val_append").put("prop_append2", "23");
		object.append(objectsProperties);
		object.append("prop4", 1.0);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}

	public static void testObjectPropertyRemove() throws Exception {
		Suprsend suprClient = TestHelper.getClientInstance();
		//
		String objectType = "__objecttype__";
		String objectId = "__objectid__";
		ObjectEdit object = suprClient.objects.getInstance(objectType, objectId);
		//
		object.remove("prop1", "1");
		JSONObject objectsProperties = new JSONObject().put("prop_append", "val_append").put("prop_append2", "23");
		object.remove(objectsProperties);
		object.remove("prop4", 1.0);
		JSONObject res = suprClient.objects.edit(object);
		System.out.println(res);
	}
}
