package suprsend;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This class takes care of loading the JSON schema which will be used to
 * validate the data sent by client.
 * 
 * @author Suprsend
 */
class RequestSchema {
	static Map<String, Schema> JSON_SCHEMA = new HashMap<>();

	static Schema getSchemaValidator(String schemaName) throws SuprsendException {
		switch (schemaName) {
		case "workflow":
		case "workflow_trigger":
		case "event":
		case "list_broadcast":
			if (JSON_SCHEMA.get(schemaName) == null) {
				Schema jsonSchema = RequestSchema.loadJSONSchema(schemaName);
				JSON_SCHEMA.put(schemaName, jsonSchema);
			}
			return JSON_SCHEMA.get(schemaName);
		default:
			return null;
		}
	}

	/**
	 * Load JSON schema from schema file.
	 * 
	 * @param schemaName Name of schema to be loaded
	 * @return Return JSON object of schema
	 * @throws SuprsendException
	 */
	private static Schema loadJSONSchema(String schemaName) throws SuprsendException {
		Schema jsonSchema;
		String relativePath = String.format("/%s.json", schemaName);
		InputStream schemaStream = RequestSchema.class.getResourceAsStream(relativePath);
		try {
			JSONObject jsonObject = new JSONObject(new JSONTokener(schemaStream));
			jsonSchema = SchemaLoader.load(jsonObject);
		} catch (JSONException e) {
			throw new SuprsendException("Error occured while loading schema with name " + schemaName, e);
		}
		return jsonSchema;
	}
}
