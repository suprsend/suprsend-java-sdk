# suprsend-java-sdk
This package can be included in a java project to easily integrate
with `Suprsend` platform.

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
          <version>0.7.1</version>
	    </dependency>
      </dependencies>
    ```

2. As a jar file for non maven projects:

   Please download jar from releases section.
   `suprsend-java-sdk` is available as a JAR with name - `suprsend-java-sdk-0.7.0-jar-with-dependencies.jar` and add it as an External Jar in your build path.

### Usage
Initialize the Suprsend library using the following:

For initializing SDK, you need workspace key and workspace secret. You will get both the tokens from client dashboard.

```java
import suprsend.Suprsend;
Suprsend suprClient = new Suprsend("api_key", "api_secret");
```

To logs HTTP calls to Suprsend, pass debug=true as third parameter.
```java
import suprsend.Suprsend;
Suprsend suprClient = new Suprsend("api_key", "api_secret", true);
```

### Trigger Workflow
Once you have the sdk initialized you can make a call to suprsend backend using following line:

```java
import suprsend.WorkflowTriggerRequest;
// prepare workflow body
// body = new JSONObject()
WorkflowTriggerRequest wf = new WorkflowTriggerRequest(body)
JSONObject response = suprClient.workflows.trigger(wf);
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
It triggers a pre-created workflow `purchase-made` to a recipient with id: `distinct_id`, mobile: `+419999999999`.

Sample workflow body
```json
{
   "workflow":"purchase-made",
   "actor": { // optional
      "distinct_id": "__actor_id__"
   },
   "recipients":[
      {
        "distinct_id":"__distinct_id__",
        "$sms":["+419999999999"],
      }
   ],
   "data":{
      "link1": "https://www.suprsend.com",
      "solution": "https://www.suprsend.com",
      "last_name": "ABC",
      "first_name": "XYZ",
      "question_subject": "Arithmetic Progression",
      "address": {
        "city": "Zurich"
      }
   }
}
```

### Set channels in User Profile 
You can add/remove channel details viz. email, sms, whatsapp, androidpush etc. from user's profile (using `user.add*`/
`user.remove*`/`user.set*`/`user.unset` methods)  as shown in the example below.

* First Instantiate a user object
```java
import suprsend.Subscriber;

String distinctId = "__uniq_user_id__"  // Unique id of user in your application
//Instantiate User profile
Subscriber user = suprClient.user.getInstance(distinctId)
```
* To add channel details to this user (viz. email, sms, whatsapp, androidpush, iospush etc) use user.add* method(s) as shown in the example below.

```java
// Add channel details to user-instance. Call relevant add_* methods -->

user.addEmail("user@example.com") // - To add Email

user.addSms("+919999999999") // - To add SMS

user.addWhatsapp("+919999999999") // - To add Whatsapp

user.addAndroidpush("__androidpush_fcm_token__") // - by default, token is assumed to be fcm-token

// You can set the optional provider value [fcm/xiaomi/oppo] if its not a fcm-token
user.addAndroidpush("__androidpush_xiaomi_token__", provider="xiaomi")

user.addIospush("__iospush_token__")

user.addSlack(new JSONObject('{"email": "user@example.com", "access_token": "xoxb-XXXXXXXXXXXX"}'))  // - DM user using email
user.addSlack(new JSONObject('{"user_id": "U03XXXXXXXX", "access_token": "xoxb-XXXXXXXXXXXX"}'))  // - DM user using slack member_id if known
user.addSlack(new JSONObject('{"channel_id": "C03XXXXXXXX", "access_token": "xoxb-XXXXXXXXXXXX"}'))  // - Use channel id
user.addSlack(new JSONObject('{"incoming_webhook": {"url": "https://hooks.slack.com/services/TXXXXXXXXX/BXXXXXX/XXXXXXX"}}'))  // - Use incoming webhook

// After setting the channel details on user-instance, call save()
JSONObject response = user.save()
System.out.println(response);

```
* Similarly, If you want to remove certain channel details from user, you can call user.remove* method as shown in the example below.

```java
// Remove channel helper methods
user.removeEmail("user@example.com")
user.removeSms("+919999999999")
user.removeWhatsapp("+919999999999")
user.removeAndroidpush("__android_push_fcm_token__")
user.removeAndroidpush("__android_push_xiaomi_token__", provider="xiaomi")
user.removeIospush("__iospush_token__")

user.removeSlack(new JSONObject('{"email": "user@example.com", "access_token": "xoxb-XXXXXXXXXXXX"}'))  // - DM user using email
user.removeSlack(new JSONObject('{"user_id": "U03XXXXXXXX", "access_token": "xoxb-XXXXXXXXXXXX"}'))  // - DM user using slack member_id if known
user.removeSlack(new JSONObject('{"channel_id": "C03XXXXXXXX", "access_token": "xoxb-XXXXXXXXXXXX"}'))  // - Use channel id
user.removeSlack(new JSONObject('{"incoming_webhook": {"url": "https://hooks.slack.com/services/TXXXXXXXXX/BXXXXXX/XXXXXXX"}}'))  // - Use incoming webhook

// save
JSONObject response = user.save()
System.out.println(response);

```

* If you need to delete/unset all emails (or any other channel) of a user, you can call unset method on the user instance. The method accepts the channel key/s (a single key or list of keys)

```java
// --- To delete all emails associated with user
user.unset("$email")
JSONObject response = user.save()
System.out.println(response);

// what value to pass to unset channels
// for email:                $email
// for whatsapp:             $whatsapp
// for SMS:                  $sms
// for androidpush tokens:   $androidpush
// for iospush tokens:       $iospush
// for webpush tokens:       $webpush
// for slack:                $slack

// --- multiple channels can also be deleted in one call by passing argument as a list
user.unset(["$email", "$sms", "$whatsapp"])
JSONObject response = user.save()

```

- You can also set preferred language of user using `setPreferredLanguage(langCode)`. Value for langCode
  must be 2-letter code in the `ISO 639-1 Alpha-2 code` format.
  e.g. en (for English), es (for Spanish), fr (for French) etc.
```java
# --- Set 2-letter language code in "ISO 639-1 Alpha-2" format
user.setPreferredLanguage("en")
JSONObject response = user.save()
System.out.println(response);
```

- You can also set timezone of user using `setTimezone(timezone)`. Value for timezone
  must be from amongst the IANA timezones as maintained in the latest release here:
  https://data.iana.org/time-zones/tzdb-2024a/zonenow.tab.
```java
# --- Set timezone property at user level in IANA timezone format
user.setTimezone("America/Los_Angeles")
JSONObject response = user.save()
System.out.println(response);
```

* Note: After calling `add*`/`remove*`/`unset`/`set_*` methods, don't forget to call `user.save()`. On call of save(), SDK sends the request to SuprSend platform to update the User-Profile.


#### Profile Update Examples

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
    Suprsend suprClient = new Suprsend("api_key", "api_secret");
    // Subscriber Instance
    String distinctId = "__distinct_id__";
    Subscriber user = suprClient.user.getInstance(distinctId);
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
    Suprsend suprClient = new Suprsend("api_key", "api_secret");

    // Subscriber Instance
    String distinctId = "__distinct_id__";
    Subscriber user = suprClient.user.getInstance(distinctId);

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
    Suprsend suprClient = new Suprsend("api_key", "api_secret");
    // 
    String distinctId = "__distinct_id__";
    Subscriber user = suprClient.user.getInstance(distinctId);
    // 
    user.addAndroidpush("__android_push_key__", "fcm");
    JSONObject res = user.save();
    System.out.println(res);
  }


  public static void testRemove() throws Exception {
    Suprsend suprClient = new Suprsend("api_key", "api_secret");
    //
    String distinctId = "__distinct_id__";
    Subscriber user = suprClient.user.getInstance(distinctId);
    //
    user.removeWhatsapp("+919999999999");
    JSONObject response = user.save();
    System.out.println(response);
  }

  public static void testRemoveWebpush() throws Exception {
    Suprsend suprClient = new Suprsend("api_key", "api_secret");
    //
    String distinctId = "__distinct_id__";
    Subscriber user = suprClient.user.getInstance(distinctId);
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
    Suprsend suprClient = new Suprsend("api_key", "api_secret");
    //
    String distinctId = "__distinct_id__";
    Subscriber user = suprClient.user.getInstance(distinctId);
    //
    user.removeAndroidpush("__android_push_key__");
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

### Track and Send Event
You can track and send events to SuprSend platform by using `suprsend.track_event` method.
An event is composed of an `eventName`, tracked wrt a user: `distinctId`, with event-attributes: `properties`
and optional `idempotencyKey`

```java
import org.json.JSONObject;
import suprsend.Event;

// Example
String distinctId = "__uniq_user_id__"; // Mandatory, Unique id of user in your application
String eventName = "__event_name__";   // Mandatory, name of the event you're tracking
JSONObject properties = new JSONObject(); // default=null, a json object representing event-attributes
String idempotencyKey = "__uuid__"
String tenantId = "__tenant_id__"

Event event = new Event(distinctId, eventName, properties);
// You can also add Idempotency-key
Event event = new Event(distinctId, eventName, properties, idempotencyKey);
// You can also pass the tenantId to be used for templates/notifications
Event event = new Event(distinctId, eventName, properties, idempotencyKey, tenantId);

// Send event
JSONObject res = suprClient.track_event(event);
System.out.println(res);
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
