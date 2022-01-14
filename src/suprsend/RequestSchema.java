package suprsend;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.ValidationException;

import org.json.JSONObject;
import org.json.JSONTokener;


public class RequestSchema {
	public static JSONObject JSON_SCHEMA;
	
	public JSONObject getSchema(String schemaName) throws IOException, ValidationException {
		if (JSON_SCHEMA == null) {
			JSON_SCHEMA = new JSONObject();
		}
		JSONObject schemaBody;
		try {
			schemaBody = (JSONObject)JSON_SCHEMA.get(schemaName);
		} catch(Exception e) {
			schemaBody = null;
		}
		if(schemaBody == null) {
			schemaBody = loadJSONSchema(schemaName);
			if(schemaBody == null) {
				throw new ValidationException("Invalid or no schema");
			}
			else {
				JSON_SCHEMA.put(schemaName, schemaBody);
			}
		}
		return schemaBody;
	}
	
	private JSONObject loadJSONSchema(String schemaName) throws IOException {
		String rootPath = new File(".").getCanonicalPath();
		String relativePath = String.format("request_json/%s.json", schemaName);
		JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(String.format("%s/%s", rootPath, relativePath))));
		return jsonObject;
	}
}
