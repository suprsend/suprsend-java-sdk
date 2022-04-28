package suprsend;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {
	public static int getApparentBodySize(JSONObject body) {
		int extraBytes = Constants.RUNTIME_KEYS_POTENTIAL_SIZE_IN_BYTES;
		int bodySize, apparentBodySize;
		JSONObject apparentBody = body;
		JSONArray attachments;
		
		if (body.has("data") && body.getJSONObject("data").has("$attachments")) {
			int numAttachments = body.getJSONObject("data").getJSONArray("$attachments").length();
			extraBytes += numAttachments * Constants.ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES;
			apparentBody = new JSONObject(body.toString());
			attachments = apparentBody.getJSONObject("data").getJSONArray("$attachments");
			for (int i=0; i <= attachments.length(); i++) {
				attachments.getJSONObject(i).remove("data");
			}
		}
		
		bodySize = apparentBody.toString().length();
		apparentBodySize = bodySize + extraBytes;
		return apparentBodySize;
	}
	
	public static JSONObject validateWorkflowBodySchema(JSONObject data) throws SuprsendException {
		JSONObject jsonSchema;
		
		if(data.has("data") == false) {
			data.put("data", new JSONObject());
		}
		RequestSchema schema = new RequestSchema();
		jsonSchema = schema.getSchema("workflow");
		Schema schemaValidator = SchemaLoader.load(jsonSchema);
		try {
			schemaValidator.validate(data);
		} catch(ValidationException e) {
			throw new ValidationException(schemaValidator, e.getMessage());
		}
		return data;
	}

}
