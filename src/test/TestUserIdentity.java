package test;

import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testRemoveHelperFunctions();
	}
	
	public static void testSave() throws Exception {
		Suprsend suprsendClient = new Suprsend("lap5NefpkeN4gKyi8CiM", "Ya1eDjXYDsg9Bt88dpWw");
		UserIdentity user = suprsendClient.user.newUserIdentity("nikitanavral1404@gmail.com");
		JSONObject obj = new JSONObject();
		obj.put("$email", "gaurang@suprsend.com");
		obj.put("$sms", "+917738300561");
		obj.put("$whatsapp", "+917738300561");
		user.append(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemove() throws Exception {
		String distinctID = "nikitanavral1404@gmail.com";
		Suprsend suprsendClient = new Suprsend("lap5NefpkeN4gKyi8CiM", "Ya1eDjXYDsg9Bt88dpWw");
		UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
		JSONObject obj = new JSONObject();
		obj.put("$whatsapp", "+917738300561");
		user.remove(obj);
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testAddHelperFunctions() throws Exception {
		Suprsend suprsendClient = new Suprsend("lap5NefpkeN4gKyi8CiM", "Ya1eDjXYDsg9Bt88dpWw");
		UserIdentity user = suprsendClient.user.newUserIdentity("nikitanavral1404@gmail.com");
		user.addEmail("parabgp1412@gmail.com");
		user.addSMS("+917021479255");
		user.addWhatsapp("+917021479255");
		JSONObject res = user.save();
		System.out.println(res);
	}
	
	public static void testRemoveHelperFunctions() throws Exception {
		Suprsend suprsendClient = new Suprsend("lap5NefpkeN4gKyi8CiM", "Ya1eDjXYDsg9Bt88dpWw");
		UserIdentity user = suprsendClient.user.newUserIdentity("nikitanavral1404@gmail.com");
		user.removeEmail("parabgp1412@gmail.com");
		user.removeSMS("+917021479255");
		user.removeWhatsapp("+917021479255");
		JSONObject res = user.save();
		System.out.println(res);
	}

}
