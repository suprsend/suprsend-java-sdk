# suprsend-java-sdk
This package can be included in a java project to easily integrate
with `Suprsend` platform.

We're working towards creating SDK in other languages as well.

### Suprsend SDKs available in following languages

* java (`suprsend-java-sdk`)

### Installation
`suprsend-java-sdk` is available as a JAR with following name - suprsend-java-sdk-0.0.1-SNAPSHOT-jar-with-dependencies.jar. You can install the same
as a maven dependency using following command
`mvn install:install-file -Dfile=suprsend-java-sdk-0.0.1-SNAPSHOT-jar-with-dependencies.jar  -DgroupId=suprsend-java-sdk -DartifactId=suprsend-java-sdk -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar`
Once above command is executed add following to pom.xml:
```
<dependencies>
	<dependency>
		<groupId>suprsend-java-sdk</groupId>
		<artifactId>suprsend-java-sdk</artifactId>
		<version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

We are working towards making the dependency available via Maven central. It will be live shortly.

### Usage
Following example shows the example to use SDK to send whatsapp message

```
package test;

import org.json.JSONArray;
import org.json.JSONObject;

import suprsend.Suprsend;

public class TestSuprsendSDK {
	
	private static Suprsend suprsend;

	private static JSONObject getParams() {
		JSONObject params = new JSONObject();
		params.put("isUAT", true);
		params.put("authEnabled", true);
		params.put("includeSignatureParam", true);
		return params;
	}
	
	private static JSONObject getBody() {
		JSONArray whatsapp = new JSONArray();
		JSONArray users = new JSONArray();
		JSONArray answers = new JSONArray();
		JSONObject user = new JSONObject();
		JSONObject answer = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject body = new JSONObject();
		
		whatsapp.put("+91__mobile_number__");
		
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
		body.put("template", "template_name");
		body.put("notification_category", "transactional");
		body.put("users", users);
		body.put("data", data);
		return body;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		JSONObject params = getParams();
		JSONObject body = getBody();
		suprsend = new Suprsend("__env_key__", "__env_secret__", null, false, params);
		JSONObject response = suprsend.triggerWorkflow(body);
		System.out.println(response);
	}

}
```