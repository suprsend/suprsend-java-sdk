package test;

import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
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

}
