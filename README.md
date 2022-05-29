# suprsend-java-sdk
This package can be included in a java project to easily integrate
with `Suprsend` platform.

We're working towards creating SDK in other languages as well.

### Suprsend SDKs available in following languages

* java (`suprsend-java-sdk`)
* python3 >= 3.7 (`suprsend-py-sdk`)
* node (`suprsend-node-sdk`)

### Installation

You can include the jar using following two ways:
1. As a Maven dependency for maven projects:

   suprsend-java-sdk is present as a maven dependency on maven central.
   Add following to your pom.xml to include the sdk:
   ```xml
      <dependencies>
        <dependency>
          <groupId>com.suprsend</groupId>
          <artifactId>suprsend-java-sdk</artifactId>
          <version>0.3.0</version>
	    </dependency>
      </dependencies>
    ```

2. As a jar file for non maven projects:

   Please download jar from releases section.
   `suprsend-java-sdk` is available as a JAR with following name - suprsend-java-sdk-0.3.0-jar-with-dependencies.jar

   - Right click on your java project.
   - Click on "Build Path".
   - Click on "Add External JARs"
   - Select the jar file you received from local machine.
   - Click "Apply and Close"

### Usage
Initialize the Suprsend library using the following:

For initializing SDK, you need workspace key and workspace secret. You will get both the tokens from client dashboard.

```java
import suprsend.Suprsend;
Suprsend suprsend = new Suprsend("workspace_key", "workspace_secret");
```

To logs HTTP calls to Suprsend, pass debug=true as third parameter.
```java
import suprsend.Suprsend;
Suprsend suprsend = new Suprsend("workspace_key", "workspace_secret", true);
```

### Trigger Workflow
Once you have the object initialized you can make a call to suprsend backend using following line:

```java
// prepare workflow body
// body = new JSONObject()
JSONObject response = suprsend.triggerWorkflow(body);
```

Response could be one of the following:
```json
# If the call succeeds, response will looks like:
{
    "success": true,
    "status":"success",
    "status_code": 202,
    "message": "Accepted",
}

# In case the call fails. You will receive a response with success=False
{
    "success": false,
    "status": "fail",
    "status_code": 400/500,
    "message": "error message",
}
```

Note: The actual processing/execution of workflow happens asynchronously.

### Sample
Following example shows a sample request for triggering a workflow.
It triggers a notification to a user with id: `distinct_id`,
mobile: `919999999999`
using template `purchase-made` and notification_category `transactional`

Sample workflow body
```json
{
   "name":"Retail User Purchase",
   "template":"purchase-made",
   "notification_category":"transactional",
   "users":[
      {
         "$sms":["+919999999999"],
         "distinct_id":"__distinct_id__"
      }
   ],
   "delivery": {
       "smart": false,
       "success": "seen"
   },
   "data":{
      "event": {
	    "logo": "https://ik.imagekit.io/l0quatz6utm/1639998025344-Screenshot 2021-12-20 at 4.13.24 PM.png",
	    "link1": "https://www.suprsend.com",
	    "solution": "https://www.doubtnut.com",
	    "last_name": "ABC",
	    "first_name": "XYZ",
	    "question_subject": "Arithmetic Progression"
	  }
   }
}
```

Put the above code in a file named input.json

Code to call Suprsend backend using SDK

```java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

import suprsend.Suprsend;

public class TestSuprsendSDK {

    private JSONObject loadBody(String fileName) throws FileNotFoundException {
        JSONObject jsonObject;
        String relativePath = String.format("%s/src/%s/resources/%s.json", System.getProperty("user.dir"),
                this.getClass().getPackage().getName(), fileName);
        InputStream schemaStream = new FileInputStream(new File(relativePath));
        jsonObject = new JSONObject(new JSONTokener(schemaStream));
        return jsonObject;
    }

    public static void main(String[] args) throws Exception {
        TestSuprsendSDK sdk = new TestSuprsendSDK();
        // Load workflow body json
        JSONObject body = sdk.loadBody("input");
        // SDK instance
        Suprsend suprsend = new Suprsend("workspace_key", "workspace_secret");
        // Trigger workflow
        JSONObject response = suprsend.triggerWorkflow(body);
        System.out.println(response);
    }

}}
```

#### Duration Format
format for specifying duration: `[xx]d[xx]h[xx]m[xx]s`
Where
* `d` stands for days. value boundary: 0 <= `d`
* `h` stands for hours. value boundary: 0 <= `h` <= 23
* `m` stands for minutes. value boundary: 0 <= `m` <= 59
* `s` stands for seconds. value boundary: 0 <= `s` <= 59

Examples:
* 2 days, 3 hours, 12 mins, 23 seconds -> 2d3h12m23s or 02d03h12m23s
* 48 hours -> 2d
* 30 hours -> 1d6h
* 300 seconds -> 5m
* 320 seconds -> 5m20s
* 60 seconds -> 1m

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

You can set user channel details viz. email, sms, whatsapp, androidpush etc (using `user.add_*` methods) as shown in the example below.

```java

import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.Subscriber;

public class TestSubscribers {
  public static void main(String[] args) throws Exception {
    testSave();
  }

  public static void testSave() throws Exception {
    // SDK instance
    Suprsend suprsendClient = new Suprsend("workspace_key", "workspace_secret");
    // Subscriber Instance
    String distinctID = "__distinct_id__";
    Subscriber user = suprsendClient.user.getInstance(distinctID);
    // Add properties
    user.addEmail("example@example.com");
    user.addSms("+919999999999");
    user.addWhatsapp("+919999999999");
    // Save
    JSONObject response = user.save();
    System.out.println(response);
  }

  public static void testAddWebpush() throws Exception {
    // SDK instance
    Suprsend suprsendClient = new Suprsend("workspace_key", "workspace_secret");

    // Subscriber Instance
    String distinctID = "__distinct_id__";
    Subscriber user = suprsendClient.user.getInstance(distinctID);

    // Webpush token json (VAPID)
    JSONObject webpush = new JSONObject()
        .put("endpoint", "__end_point__")
        .put("expirationTime", "")
        .put("keys", new JSONObject()
            .put("p256dh", "__p256dh__")
            .put("auth", "__auth_key__"));
    //
    user.addWebpush(webpush, "vapid");
    // Save
    JSONObject response = user.save();
    System.out.println(response);
  }

  public static void testAddAndroidpush() throws Exception {
    Suprsend suprsendClient = new Suprsend("workspace_key", "workspace_secret");
    // 
    String distinctID = "__distinct_id__";
    Subscriber user = suprsendClient.user.getInstance(distinctID);
    // 
    user.addAndroidpush("__android_push_key__");
    JSONObject res = user.save();
    System.out.println(res);
  }
}

```

#### Response structure
```json
{
    "success": true, // if true, request was accepted.
    "status": "success",
    "status_code": 202, // http status code
    "message": "OK",
}

{
    "success": false, // error will be present in message
    "status": "fail",
    "status_code": 500, // http status code
    "message": "error message",
}
```

If you want to remove some channel details, use `user.remove_*` method.

```java
import org.json.JSONObject;

import suprsend.Suprsend;
import suprsend.Subscriber;

public class TestSubscriber2 {
  public static void main(String[] args) throws Exception {
    testRemove();
  }

  public static void testRemove() throws Exception {
    Suprsend suprsendClient = new Suprsend("workspace_key", "workspace_secret");
    //
    String distinctID = "__distinct_id__";
    Subscriber user = suprsendClient.user.getInstance(distinctID);
    //
    user.removeWhatsapp("+919999999999");
    JSONObject response = user.save();
    System.out.println(response);
  }

  public static void testRemoveWebpush() throws Exception {
    Suprsend suprsendClient = new Suprsend("workspace_key", "workspace_secret");
    //
    String distinctID = "__distinct_id__";
    Subscriber user = suprsendClient.user.getInstance(distinctID);
    // Webpush token json (VAPID)
    JSONObject webpush = new JSONObject()
        .put("endpoint", "__end_point__")
        .put("expirationTime", "")
        .put("keys", new JSONObject()
            .put("p256dh", "__p256dh__")
            .put("auth", "__auth_key__"));
    //
    user.removeWebpush(webpush);
    JSONObject response = user.save();
    System.out.println(response);
  }

  public static void testRemoveAndroidpush() throws Exception {
    Suprsend suprsendClient = new Suprsend("workspace_key", "workspace_secret");
    //
    String distinctID = "__distinct_id__";
    Subscriber user = suprsendClient.user.getInstance(distinctID);
    //
    user.removeAndroidpush("__android_push_key__");
    JSONObject res = user.save();
    System.out.println(res);
  }
}
```

Below are all the methods to add/remove channel details.

```java
String distinctID = "__distinct_id__";
Subscriber user = suprsendClient.user.getInstance(distinctID);
//
user.addEmail("user@example.com");
user.removeEmail("user@example.com");

user.addSms("+919999999999");
user.removeSms("+919999999999");

user.addWhatsapp("+919999999999");
user.removeWhatsapp("+919999999999");

// Add and remove Androidpush token.. By default token is assumed to be fcm.
// If token being passed is from other vendor, pass the vendor as 2nd param.
user.addAndroidpush("androidpush_fcm_token__");
user.addAndroidpush("androidpush_xiaomi_token__", "xiaomi");

user.removeAndroidpush("androidpush_fcm_token__");
user.removeAndroidpush("androidpush_xiaomi_token__", "xiaomi");

// Add and remove iospush token 
user.addIospush("__iospush_apns_token__");
user.removeIospush("__iospush_apns_token__");

// Add and remove webpush token 
user.addWebpush(webpushToken);
user.removeWebpush(webpushToken);
```

Note: After calling `add_*`/`remove_*` methods, don't forget to call `user.save()`.

Once channels details are set at User profile, you only have to mention the user's distinctID
while triggering workflow. Associated channels will automatically be picked up from user-profile e.g.

Sample workflow after setting user profile:

```json
{
  "name":"SMS",
  "template":"Name of registered template",
  "notification_category":"transactional",
  "users":[
    {
        "distinct_id":"__distinct_id__"
    }
  ],
  "data":{
    "event": {
    "logo": "https://ik.imagekit.io/l0quatz6utm/1639998025344-Screenshot 2021-12-20 at 4.13.24 PM.png",
    "link1": "https://www.suprsend.com",
    "solution": "https://www.doubtnut.com",
    "last_name": "ABC",
    "first_name": "XYZ",
    "question_subject": "Arithmetic Progression"
  }
  }
}
```

### Track and Send Event
You can track and send events to SuprSend platform by using `suprsend.track` method.
Event: `event_name`, tracked wrt a user: `distinct_id`, with event-attributes: `properties`

```java
// Method Signature
public JSONObject track(String distinctID, String eventName, JSONObject properties) throws SuprsendException {}
```

```json
// Response structure
{
    "success": true, // if true, request was accepted.
    "status": "success",
    "status_code": 202, // http status code
    "message": "OK",
}

{
    "success": false, // error will be present in message
    "status": "fail",
    "status_code": 500, // http status code
    "message": "error message",
}
```
