package test;

import org.json.JSONObject;
import suprsend.BulkResponse;
import suprsend.BulkSubscribers;
import suprsend.Subscriber;
import suprsend.Suprsend;
import suprsend.SuprsendException;

import java.util.ArrayList;
import java.util.Arrays;

public class TestSubscriber {

	public static void main(String[] args) throws Exception {
		// Call the relevant function to test after adding valid values.
		testSave();
		testAddWebpush();
		testRemoveWebpush();
		testAddAndroidpush();
		testRemoveAndroidpush();
		testRemove();
		testAddHelperFunctions();
		testRemoveHelperFunctions();
		testUnsetKey();
		testUnsetKeyMulti();
		testBulkSubscriber();
	}

	public static void testSave() throws Exception {
		// SDK instance
		Suprsend suprsendClient = TestHelper.getInstance();
		// Subscriber Instance
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		// Add properties
		user.addEmail("example@example.com");
		user.addSms("+919999999999");
		user.addWhatsapp("+919999999999");
		// Save
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddWebpush() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
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
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		// Webpush token json (VAPID)
		JSONObject webpush = new JSONObject().put("endpoint", "__end_point__").put("expirationTime", "").put("keys",
				new JSONObject().put("p256dh", "__p256dh__").put("auth", "__auth_key__"));
		//
		user.removeWebpush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddAndroidpush() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.addAndroidpush("__android_push_key__", "fcm");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveAndroidpush() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.removeAndroidpush("__android_push_key__");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemove() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.removeSms("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testAddHelperFunctions() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.addEmail("example@example.com");
		user.addSms("+919999999999");
		user.addWhatsapp("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testRemoveHelperFunctions() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.removeEmail("example@example.com");
		user.removeSms("+919999999999");
		user.removeWhatsapp("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUnsetKey() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.unset("$email");
		user.unset("$sms");
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testUnsetKeyMulti() throws Exception {
		Suprsend suprsendClient = TestHelper.getInstance();
		//
		String distinctID = "__distinct_id__";
		Subscriber user = suprsendClient.user.getInstance(distinctID);
		//
		user.unset(Arrays.asList(new String[] { "$sms", "$email" }));
		JSONObject res = user.save();
		System.out.println(res);
	}

	public static void testBulkSubscriber() throws Exception {
		BulkSubscribers bulkSubscribers = TestHelper.getInstance().bulkSubscribersFactory.getInstance();
		ArrayList<Subscriber> subscriberList = new ArrayList<Subscriber>();
		for (int i = 0; i < 3; i++) {
			subscriberList.add(getSubscriber());
		}
		bulkSubscribers.append(subscriberList);
		BulkResponse response = bulkSubscribers.save();
		System.out.println(response);
	}

	private static Subscriber getSubscriber() throws SuprsendException {
		Subscriber user = TestHelper.getInstance().user.getInstance("123");
		user.removeSms("+919999999999");
		return user;
	}
}
