# suprsend-java-sdk
This package can be included in a java project to easily integrate
with `Suprsend` platform.

We're working towards creating SDK in other languages as well.

### Suprsend SDKs available in following languages

* java (`suprsend-java-sdk`)

### Installation
`suprsend-java-sdk` is available as a JAR with following name - suprsend-java-sdk-0.1.0-jar-with-dependencies.jar. 

You can include the jar using following two ways:
1. As a Maven dependency for maven projects
   You can include the jar as maven dependency using following procedure:
   a. Run following command:
   
   `mvn install:install-file -Dfile=suprsend-java-sdk-0.1.0-jar-with-dependencies.jar  -DgroupId=suprsend-java-sdk -DartifactId=suprsend-java-sdk -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar`
   
   b. Once above command is executed add following to pom.xml:
   ```
    <dependencies>
        <dependency>
            <groupId>suprsend-java-sdk</groupId>
            <artifactId>suprsend-java-sdk</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```
   ###### Note: We are working towards making the dependency available via Maven central. It will be live shortly.
   
2. As a jar file for non maven projects:
   a. Right click on your java project.
   b. Click on "Build Path".
   c. Click on "Add External JARs"
   d. Select the jar file you received from local machine.
   e. Click "Apply and Close"

### Initialization
Initialize the Suprsend library using the following:

For initializing SDK, you need workspace key and workspace secret. You will get both the tokens from client dashboard.

```
import suprsend.Suprsend;
Suprsend suprsend = new Suprsend("__env_key__", "__env_secret__");
```

Alternatively we provide following constructors:

i. Constructor which allows you to provide custom base URL:
```
import suprsend.Suprsend;
Suprsend suprsend = new Suprsend("__env_key__", "__env_secret__", "Custom Base URL");
```

ii. Constructor which allows you to view HTTP logs in your console:
```
import suprsend.Suprsend;
Suprsend suprsend = new Suprsend("__env_key__", "__env_secret__", true);
```

### How the call is made to SuprSend?
Once you have the object initialized you can make a call to suprsend backend using following line:

```
JSONObject response = suprsend.triggerWorkflow(body);
```

Response could be one of the following:
```
# If the call succeeds, response will looks like:
{
    "success": True,
    "status": 201,
    "message": "Message received",
}

# In case the call fails. You will receive a response with success=False
{
    "success": False,
    "status": 400,
    "message": "error message",
}
```

Note: The actual processing/execution of workflow happens asynchronously.

### Sample

Sample workflow body
```
{
   "template":"Name of registered template",
   "notification_category":"transactional",
   "data":{
      "event": {
	    "logo": "https://ik.imagekit.io/l0quatz6utm/1639998025344-Screenshot 2021-12-20 at 4.13.24 PM.png",
	    "link1": "https://www.suprsend.com",
	    "solution": "https://www.doubtnut.com",
	    "last_name": "ABC",
	    "first_name": "XYZ",
	    "question_subject": "Arithmetic Progression"
	  }
   },
   "delivery": {
       "smart": False,
       "success": "seen"
   },
   "name":"SMS",
   "users":[
      {
         "$sms":[
            "+91__mobile_no__"
         ],
         "distinct_id":"__distinct_id__"
      }
   ]
}
```

Put the above code in a file named input.json

Code to call Suprsend backend using SDK

```
package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import suprsend.Suprsend;

public class TestSuprsendSDK {
	
	private static Suprsend suprsend;
	
	private JSONObject loadBody(String fileName) throws FileNotFoundException {
		JSONObject jsonObject;
		String relativePath = String.format("%s/src/%s/resources/%s.json", System.getProperty("user.dir"), this.getClass().getPackage().getName(), fileName);
		InputStream schemaStream = new FileInputStream(new File(relativePath));
		jsonObject = new JSONObject(new JSONTokener(schemaStream));
		return jsonObject;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		TestSuprsendSDK sdk = new TestSuprsendSDK();
		JSONObject body = sdk.loadBody("input");
		suprsend = new Suprsend("__env_key__", "__env_secret__");
		JSONObject response = suprsend.triggerWorkflow(body);
		System.out.println(response);
	}

}
```

#### Delivery instruction
All delivery options:
```
delivery = {
    "smart": True/False,
    "success": "seen/interaction/<some-user-defined-success-event>",
    "time_to_live": "<TTL duration>",
    "mandatory_channels": [] # list of mandatory channels e.g ["email"]
}
```
Where
* `smart` (boolean) - whether to optimize for number of notifications sent?
  - Possible values: `True` / `False`
  - Default value: False
  - If False, then notifications are sent on all channels at once.
  - If True, then notifications are sent one-by-one (on regular interval controlled by `time_to_live`)
    on each channel until given `success`-metric is achieved.

* `success` - what is your measurement of success for this notification?
  - Possible values: `seen` / `interaction` / `<some-user-defined-success-event>`
  - Default value: seen
  - If `seen`: If notification on any of the channels is seen by user, consider it a success.
  - If `interaction`: If notification on any of the channels is clicked/interacted by the user, consider it a success.
  - If `<some-user-defined-success-event>`: If certain event is done by user within the event-window (1 day), consider it a success.
    - currently, event-window is not configurable. default set to `1d` (1 day).
      success-event must happen within this event-window since notification was sent.

* `time_to_live` - What's your buffer-window for sending notification.
  - applicable when `smart`=True, otherwise ignored
  - Default value: `1h` (1 hour)
  - notification on each channel will be sent with time-interval of [`time_to_live / (number_of_channels - 1))`] apart.
  - channels are tried in low-to-high notification-cost order based on `Notification Cost` mentioned in Vendor Config.
    If cost is not mentioned, it is considered zero for order-calculation purpose.
  - Process will continue until all channels are exhausted or `success` metric is achieved, whichever occurs first.

* `mandatory_channels` - Channels on which notification has to be sent immediately (irrespective of notification-cost).
  - applicable when `smart`=True, otherwise ignored
  - Default value: [] (empty list)
  - possible channels: `email, sms, whatsapp, androidpush, iospush` etc.


If delivery instruction is not provided, then default value is
```
{
    "smart": False,
    "success": "seen"
}
```

### Set channels in User Profile
If you regularly trigger a workflow for users on some pre-decided channels,
then instead of adding user-channel-details in each workflow request, you can set those channel-details in user
profile once, and after that, in workflow trigger request you only need to pass the distinct_id of the user.
All associated channels in User profile will be automatically picked when executing the workflow.

You can set user channel details viz. email, sms, whatsapp, androidpush etc (using `user.append` method) as shown in the example below.

```
import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {
    public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testSave();
	}

    public static void testSave() throws Exception {
        String distinctID = "__distinct_id__";
        Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
        UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
        JSONObject obj = new JSONObject();
        obj.put("$email", "example@example.com");
		obj.put("$sms", "+919999999999");
		obj.put("$whatsapp", "+919999999999");
        user.append(obj);
        JSONObject response = user.save();
		System.out.println(response);
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
		JSONObject response = user.save();
		System.out.println(response);
	}

    public static void testAppendAndroidPush() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		
		JSONObject obj = new JSONObject();
		obj.put("$androidpush", "__android_push_key__");
		obj.put("$pushvendor", "fcm");		
		user.append(obj);
		JSONObject response = user.save();
		System.out.println(response);
	}
}
```

# Response structure
```
{
    "success": True, # if true, request was accepted.
    "status": "success",
    "status_code": 202, # http status code
    "message": "OK",
}

{
    "success": False, # error will be present in message
    "status": "fail",
    "status_code": 500, # http status code
    "message": "error message",
}
```

Apart from the append method which accepts JSONObject as an input you have 2 other options for append method as follows

```
user.append(String, JSONObject)
user.append(String, String)
```

If you want to remove some channel details, use `user.remove` method.

```
import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {
    public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testRemove();
	}

    public static void testRemove() throws Exception {
        String distinctID = "__distinct_id__";
        Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
        UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
        JSONObject obj = new JSONObject();
        obj.put("$whatsapp", "+919999999999");
        user.remove(obj);
        JSONObject response = user.save();
		System.out.println(response);
    }

    public static void testRemoveWebPush() throws Exception {
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
		JSONObject response = user.save();
		System.out.println(response);
	}

    public static void testRemoveAndroidPush() throws Exception {
		String disctinctID = "__disctint_id__";
		Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
		UserIdentity user = suprsendClient.user.newUserIdentity(disctinctID);
		
		JSONObject obj = new JSONObject();
		obj.put("$androidpush", "__android_push_key__");
		obj.put("$pushvendor", "fcm");		
		user.remove(obj);
		JSONObject response = user.save();
		System.out.println(response);
	}
}
```

Apart from the remove method which accepts JSONObject as an input you have 2 other options for remove method as follows

```
user.remove(String, JSONObject)
user.remove(String, String)
```

There are helper methods available to add/remove channel details.

```
import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.UserIdentity;

public class TestUserIdentity {
    public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		testAddHelperFunctions();
        testRemoveHelperFunctions();
	}

    public static void testAddHelperFunctions() throws Exception {
		String distinctID = "__distinct_id__";
        Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
        UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);

        JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "__p256dh__");
		keys.put("auth", "__auth_key__");
		webpush.put("endpoint", "__end_point__");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);

        user.addEmail("example@example.com");
		user.addSMS("+919999999999");
		user.addWhatsapp("+919999999999");
        user.addAndroidPush("__android_push_token__", "__provider_name__");
        user.addIOSPush("__iospush_token__", "__provider_name__");
        user.addWebPush(webpush, "vapid");        

		JSONObject response = user.save();
		System.out.println(response);
	}
	
	public static void testRemoveHelperFunctions() throws Exception {
		String distinctID = "__distinct_id__";
        Suprsend suprsendClient = new Suprsend("__env_key__", "__env_secret__");
        UserIdentity user = suprsendClient.user.newUserIdentity(distinctID);
        
        JSONObject webpush = new JSONObject();
		JSONObject keys = new JSONObject();
		keys.put("p256dh", "__p256dh__");
		keys.put("auth", "__auth_key__");
		webpush.put("endpoint", "__end_point__");
		webpush.put("expirationTime", "");
		webpush.put("keys", keys);

		user.removeEmail("example@example.com");
		user.removeSMS("+919999999999");
		user.removeWhatsapp("+919999999999");
        user.removeAndroidPush("__android_push_token__", "__provider_name__");
        user.removeIOSPush("__iospush_token__", "__provider_name__");
        user.removeWebPush(webpush, "vapid");

		JSONObject response = user.save();
		System.out.println(response);
	}
}
```

Note: After calling `append`/`remove`/`add_*`/`remove_*` methods, don't forget to call `user.save()`.

Once channels details are set at User profile, you only have to mention the user's distinctID
while triggering workflow. Associated channels will automatically be picked up from user-profile e.g.

Sample workflow after setting user profile:

```
{
   "template":"Name of registered template",
   "notification_category":"transactional",
   "data":{
      "event": {
	    "logo": "https://ik.imagekit.io/l0quatz6utm/1639998025344-Screenshot 2021-12-20 at 4.13.24 PM.png",
	    "link1": "https://www.suprsend.com",
	    "solution": "https://www.doubtnut.com",
	    "last_name": "ABC",
	    "first_name": "XYZ",
	    "question_subject": "Arithmetic Progression"
	  }
   },
   "name":"SMS",
   "users":[
      {
         "distinct_id":"__distinct_id__"
      }
   ]
}
``` 

### Track and Send Event
You can track and send events to SuprSend platform by using `suprsend.track` method.
Event: `event_name`, tracked wrt a user: `distinct_id`, with event-attributes: `properties`

```
# Method Signature
public JSONObject track(String distinctID, String eventName, JSONObject properties) throws SuprsendException {}
```

```
# Response structure
{
    "success": True, # if true, request was accepted.
    "status": "success",
    "status_code": 202, # http status code
    "message": "OK",
}

{
    "success": False, # error will be present in message
    "status": "fail",
    "status_code": 500, # http status code
    "message": "error message",
}
```