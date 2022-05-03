package test;

import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {

	public static void main(String[] args) throws Exception {
		// Call the relevant function to test after adding valid values.
	}
	
	public static void testSave() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		JSONObject obj = new JSONObject();
		obj.put("$email", "example@example.com");
		obj.put("$sms", "+919999999999");
		obj.put("$whatsapp", "+919999999999");
		user.append(obj);		
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAppendWebPush() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "__p256dh__");
		keys.put("auth", "__auth_key__");
		webpush.put("endpoint", "__end_point__");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		
		JSONObject obj = new JSONObject();
		obj.put("$webpush", webpush);
		obj.put("$pushvendor", "vapid");
		
		user.append(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveGeneralWebPush() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "__p256dh__");
		keys.put("auth", "__auth_key__");
		webpush.put("endpoint", "__end_point__");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		
		JSONObject obj = new JSONObject();
		obj.put("$webpush", webpush);
		obj.put("$pushvendor", "vapid");
		
		user.remove(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAppendAndroidPush() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		
		JSONObject obj = new JSONObject();
		obj.put("$androidpush", "__android_push_key__");
		obj.put("$pushvendor", "fcm");		
		user.append(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveGeneralAndroidPush() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		
		JSONObject obj = new JSONObject();
		obj.put("$androidpush", "__android_push_key__");
		obj.put("$pushvendor", "fcm");		
		user.remove(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemove() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		JSONObject obj = new JSONObject();
		obj.put("$sms", "+919999999999");
		user.remove(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddHelperFunctions() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		user.addEmail("example@example.com");
		user.addSMS("+919999999999");
		user.addWhatsapp("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddWebPush() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "__p256dh__");
		keys.put("auth", "__auth_key__");
		webpush.put("endpoint", "__end_point__");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		user.addWebPush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveWebPush() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "__p256dh__");
		keys.put("auth", "__auth_key__");
		webpush.put("endpoint", "__end_point__");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		user.removeWebPush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddAndroidPush() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		user.addAndroidPush("__android_push_token__", "fcm");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveAndroidPush() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		user.removeAndroidPush("__android_push_token__", "fcm");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveHelperFunctions() throws Exception {
		String distinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		user.removeEmail("example@example.com");
		user.removeSMS("+919999999999");
		user.removeWhatsapp("+919999999999");
		JSONObject res = user.save();
		System.out.println(res);
	}

}
