package suprsend;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;

public class Utils {

	public static String getCurrentDateTimeFormatted(String formatPattern) {
		ZoneId zone = ZoneId.of("UTC");
		LocalDateTime currentDateTime = LocalDateTime.now(zone);
		ZonedDateTime zonedCurrentDateTime = currentDateTime.atZone(zone);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(formatPattern);
		return dateTimeFormat.format(zonedCurrentDateTime);
	}

	public static JSONObject mergeJSONObjects(JSONObject... objs) {
		JSONObject merged = new JSONObject();
		if (objs == null) {
			return merged;
		}
		for (JSONObject o : objs) {
			if (o != null && !o.isEmpty()) {
				for (String key : JSONObject.getNames(o)) {
					merged.put(key, o.get(key));
				}
			}
		}
		return merged;
	}

	/**
	 * Validate data against the event JSON schema
	 * 
	 * @param data event payload
	 * @return Validated data
	 * @throws SuprsendException if schema not found.
	 */
	public static JSONObject validateWorkflowSchema(JSONObject data) throws SuprsendException, ValidationException {
		JSONObject jsonSchema;
		if (data.opt("properties") == null) {
			data.put("properties", new JSONObject());
		}
		jsonSchema = RequestSchema.getSchema("workflow");
		Schema schemaValidator = SchemaLoader.load(jsonSchema);
		try {
			schemaValidator.validate(data);
		} catch (ValidationException e) {
			throw e;
		}
		return data;
	}

	/**
	 * Validate data against the event JSON schema
	 * 
	 * @param data event payload
	 * @return Validated data
	 * @throws SuprsendException if schema not found.
	 */
	public static JSONObject validateEventSchema(JSONObject data) throws SuprsendException, ValidationException {
		if (data.opt("properties") == null) {
			data.put("properties", new JSONObject());
		}
		JSONObject jsonSchema = RequestSchema.getSchema("event");
		Schema schemaValidator = SchemaLoader.load(jsonSchema);
		try {
			schemaValidator.validate(data);
		} catch (ValidationException e) {
			throw e;
		}
		return data;
	}

}
