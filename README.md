# suprsend-java-sdk
This package can be included in a java project to easily integrate
with `Suprsend` platform.

We're working towards creating SDK in other languages as well.

### Suprsend SDKs available in following languages

* java (`suprsend-java-sdk`)

### Installation
`suprsend-java-sdk` is available as a JAR with following name - suprsend-java-sdk-0.0.1-SNAPSHOT-jar-with-dependencies.jar. 

You can include the jar using following two ways:
1. As a Maven dependency for maven projects
   You can include the jar as maven dependency using following procedure:
   a. Run following command:
   
   `mvn install:install-file -Dfile=suprsend-java-sdk-0.0.1-SNAPSHOT-jar-with-dependencies.jar  -DgroupId=suprsend-java-sdk -DartifactId=suprsend-java-sdk -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar`
   
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
   ######Note: We are working towards making the dependency available via Maven central. It will be live shortly.
   
2. As a jar file for non maven projects:
   a. Right click on your java project.
   b. Click on "Build Path".
   c. Click on "Add External JARs"
   d. Select the jar file you received from local machine.
   e. Click "Apply and Close"

###Initialization
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

###How the call is made to SuprSend?
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

###Sample

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
		suprsend = new Suprsend("kfWdrPL1nFqs7OUihiBn", "From1HA1ZiSXs3ofBHXh");
		JSONObject response = suprsend.triggerWorkflow(body);
		System.out.println(response);
	}

}
```


