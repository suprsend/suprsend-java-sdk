package suprsend;

import java.io.InputStream;

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
	protected static JSONObject JSON_SCHEMA;
	protected static Schema workflowSchemaValidator;
	protected static Schema eventSchemaValidator;
	protected static Schema listBroadcastSchemaValidator;

	protected static Schema getSchemaValidator(String schemaName) throws SuprsendException {
		if (schemaName == "workflow") {
			if (null == workflowSchemaValidator) {
				JSONObject jsonSchema = RequestSchema.getSchema("workflow");
				workflowSchemaValidator = SchemaLoader.load(jsonSchema);
			}
			return workflowSchemaValidator;

		} else if  (schemaName == "event") {
			if (null == eventSchemaValidator) {
				JSONObject jsonSchema = RequestSchema.getSchema("event");
				eventSchemaValidator = SchemaLoader.load(jsonSchema);
			}
			return eventSchemaValidator;
		} else if  (schemaName == "list_broadcast") {
			if (null == listBroadcastSchemaValidator) {
				JSONObject jsonSchema = RequestSchema.getSchema("list_broadcast");
				listBroadcastSchemaValidator = SchemaLoader.load(jsonSchema);
			}
			return listBroadcastSchemaValidator;
		} else {
			return null;
		}
	}

	/**
	 * get JSON schema from existing JSON object or from respective file.
	 * 
	 * @param schemaName Name of schema which needs to be loaded
	 * @return JSON object of the loaded schema
	 * @throws SuprsendException Throw custom exception
	 */
	protected static JSONObject getSchema(String schemaName) throws SuprsendException {
		if (JSON_SCHEMA == null) {
			JSON_SCHEMA = new JSONObject();
		}
		JSONObject schemaBody = (JSONObject) JSON_SCHEMA.opt(schemaName);
		if (schemaBody == null) {
			schemaBody = loadJSONSchema(schemaName);
			if (schemaBody == null) {
				throw new SuprsendException("Error occured while loading schema with name " + schemaName);
			} else {
				JSON_SCHEMA.put(schemaName, schemaBody);
			}
		}
		return schemaBody;
	}

	/**
	 * Load JSON schema from schema file.
	 * 
	 * @param schemaName Name of schema to be loaded
	 * @return Return JSON object of schema
	 * @throws SuprsendException
	 */
	private static JSONObject loadJSONSchema(String schemaName) throws SuprsendException {
		JSONObject jsonObject;
		String relativePath = String.format("/%s.json", schemaName);
		InputStream schemaStream = RequestSchema.class.getResourceAsStream(relativePath);
		try {
			jsonObject = new JSONObject(new JSONTokener(schemaStream));
		} catch (JSONException e) {
			throw new SuprsendException("Error occured while loading schema with name " + schemaName, e);
		}
		return jsonObject;
	}
}
