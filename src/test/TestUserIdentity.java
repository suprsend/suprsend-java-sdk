package test;

import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testAndroidPush();
	}
	
	public static void testSave() throws Exception {
//		JSONObject params = new JSONObject();
//		params.put("isUAT", true);
//		params.put("authEnabled", true);
//		params.put("includeSignatureParam", true);
		
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		JSONObject obj = new JSONObject();
		obj.put("$email", "gaurang@suprsend.com");
		obj.put("$sms", "+917738300561");
		obj.put("$whatsapp", "+917738300561");
		user.append(obj);		
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAppendWebPush() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "BEKF1ra0VL4BnoqTPVx-EB7FG6bbSASUtmfitbS7VSe9qBgzhvdPycAezY7VBZCBJHxhHG6pLFDTLOQu_SsBg7M");
		keys.put("auth", "00vw7S4Jjg0OVEgsDFpZeg");
		webpush.put("endpoint", "https://fcm.googleapis.com/fcm/send/fQKx5H8zKxU:APA91bHcfbdCZm2UYISg5bmcMUvm0XOvyws9kSW3X8w2JJXL3r8JERQyS5DX96UrdHiQFwqfCWHQgNQrG_pWRWVFJrGYGIq6SUhDI-e7xhVRRFTo061ztECFjeKl5yDSrZEVa5-nuM2j");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		
		JSONObject obj = new JSONObject();
		obj.put("$webpush", webpush);
		obj.put("$pushvendor", "vapid");
		
		user.append(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAndroidPush() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		
		JSONObject obj = new JSONObject();
		obj.put("$androidpush", "AAAA2NFjQVo:APA91bGRAYgXNaBTVbeGYarv248Ybmb1-yewlvCrmANSd0eHngIdglH0_gwtLANACU5AQmdgQaKBJqEbLINFC8A8lanRxCUrJp7WL4JOUXqhqW2HWURUKvfE1axamdiGRbpYL-OekafM");
		obj.put("$pushvendor", "fcm");		
		user.append(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemove() throws Exception {
		String distinctID = "gaurang";
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		JSONObject obj = new JSONObject();
		obj.put("$sms", "+917738300561");
		user.remove(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddHelperFunctions() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		user.addEmail("parabgp1412@gmail.com");
		user.addSMS("+917021479255");
		user.addWhatsapp("+917021479255");
		JSONObject webpush = new JSONObject();
		webpush.put("token", "abc");
		user.addWebPush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddWebPush() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "BEKF1ra0VL4BnoqTPVx-EB7FG6bbSASUtmfitbS7VSe9qBgzhvdPycAezY7VBZCBJHxhHG6pLFDTLOQu_SsBg7M");
		keys.put("auth", "00vw7S4Jjg0OVEgsDFpZeg");
		webpush.put("endpoint", "https://fcm.googleapis.com/fcm/send/fQKx5H8zKxU:APA91bHcfbdCZm2UYISg5bmcMUvm0XOvyws9kSW3X8w2JJXL3r8JERQyS5DX96UrdHiQFwqfCWHQgNQrG_pWRWVFJrGYGIq6SUhDI-e7xhVRRFTo061ztECFjeKl5yDSrZEVa5-nuM2j");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		user.addWebPush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveWebPush() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "BEKF1ra0VL4BnoqTPVx-EB7FG6bbSASUtmfitbS7VSe9qBgzhvdPycAezY7VBZCBJHxhHG6pLFDTLOQu_SsBg7M");
		keys.put("auth", "00vw7S4Jjg0OVEgsDFpZeg");
		webpush.put("endpoint", "https://fcm.googleapis.com/fcm/send/fQKx5H8zKxU:APA91bHcfbdCZm2UYISg5bmcMUvm0XOvyws9kSW3X8w2JJXL3r8JERQyS5DX96UrdHiQFwqfCWHQgNQrG_pWRWVFJrGYGIq6SUhDI-e7xhVRRFTo061ztECFjeKl5yDSrZEVa5-nuM2j");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);
		user.removeWebPush(webpush, "vapid");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddAndroidPush() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		user.addAndroidPush("AAAA2NFjQVo:APA91bGRAYgXNaBTVbeGYarv248Ybmb1-yewlvCrmANSd0eHngIdglH0_gwtLANACU5AQmdgQaKBJqEbLINFC8A8lanRxCUrJp7WL4JOUXqhqW2HWURUKvfE1axamdiGRbpYL-OekafM", "fcm");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveAndroidPush() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("gaurang");
		user.removeAndroidPush("AAAA2NFjQVo:APA91bGRAYgXNaBTVbeGYarv248Ybmb1-yewlvCrmANSd0eHngIdglH0_gwtLANACU5AQmdgQaKBJqEbLINFC8A8lanRxCUrJp7WL4JOUXqhqW2HWURUKvfE1axamdiGRbpYL-OekafM", "fcm");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveHelperFunctions() throws Exception {
		Suprsend suprsendClient = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh", "https://collector-staging.suprsend.workers.dev/");
		UserIdentity user = suprsendClient.user.newUserIdentity("nikitanavral1404@gmail.com");
		user.removeEmail("parabgp1412@gmail.com");
		user.removeSMS("+917021479255");
		user.removeWhatsapp("+917021479255");
		JSONObject res = user.save();
		System.out.println(res);
	}

}
