package suprsend;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

class Utils {
	private static ZoneId zone = ZoneId.of("UTC"); 
	private static DateTimeFormatter dateTimeHeaderFormat = DateTimeFormatter.ofPattern(Constants.HEADER_DATE_FMT);

	static String getCurrentDateTimeHeader() {
		LocalDateTime currentDateTime = LocalDateTime.now(zone);
		ZonedDateTime zonedCurrentDateTime = currentDateTime.atZone(zone);
		return dateTimeHeaderFormat.format(zonedCurrentDateTime);
	}

	static JSONObject mergeJSONObjects(JSONObject... objs) {
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

	static int getApparentWorkflowBodySize(JSONObject body, boolean isPartOfBulk) throws UnsupportedEncodingException {
		int extraBytes = Constants.WORKFLOW_RUNTIME_KEYS_POTENTIAL_SIZE_IN_BYTES;
		JSONObject apparentBody = body;
		//
		if (body.opt("data") != null) {
			JSONObject data = body.getJSONObject("data");
			if (data.optJSONArray("$attachments") != null) {
				JSONArray attachments = data.getJSONArray("$attachments");
				int numAttachment = attachments.length();
				//
				if (isPartOfBulk) {
					if (Constants.ALLOW_ATTACHMENTS_IN_BULK_API) {
						// if attachment is allowed in bulk api, then calculate size based on whether
						// auto Upload is enabled
						if (Constants.ATTACHMENT_UPLOAD_ENABLED) {
							// If auto upload enabled, To calculate size, replace attachment size with
							// equivalent url size
							extraBytes += numAttachment * Constants.ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES;
							// remove attachments->data key to calculate data size
							apparentBody = new JSONObject(body.toString());
							JSONArray apparentAttachments = apparentBody.getJSONObject("data")
									.getJSONArray("$attachments");
							for (int i = 0; i < apparentAttachments.length(); i++) {
								apparentAttachments.getJSONObject(i).remove("data");
							}
						} else {
							// if auto upload is not enabled, attachment data will be passed as it is.
						}
					} else {
						// If attachment not allowed, then remove data->$attachments before calculating size
						apparentBody = new JSONObject(body.toString());
						apparentBody.getJSONObject("data").remove("$attachments");
					}
				} else {
					if (Constants.ATTACHMENT_UPLOAD_ENABLED) {
						// If auto upload enabled, To calculate size, replace attachment size with
						// equivalent url size
						extraBytes += numAttachment * Constants.ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES;
						// remove attachments->data key to calculate data size
						apparentBody = new JSONObject(body.toString());
						JSONArray apparentAttachments = apparentBody.getJSONObject("data").getJSONArray("$attachments");
						for (int i = 0; i < apparentAttachments.length(); i++) {
							apparentAttachments.getJSONObject(i).remove("data");
						}
					} else {
						// if auto upload is not enabled, attachment data will be passed as it is.
					}
				}
			}
		}
		//
		int bodySize = apparentBody.toString().getBytes("utf-8").length;
		int apparentSize = bodySize + extraBytes;
		return apparentSize;
	}

	static int getApparentEventSize(JSONObject event, boolean isPartOfBulk) throws UnsupportedEncodingException {
		int extraBytes = 0;
		JSONObject apparentBody = event;

		if (event.opt("properties") != null) {
			JSONObject properties = event.getJSONObject("properties");
			if (properties.optJSONArray("$attachments") != null) {
				JSONArray attachments = properties.getJSONArray("$attachments");
				int numAttachment = attachments.length();
				//
				if (isPartOfBulk) {
					if (Constants.ALLOW_ATTACHMENTS_IN_BULK_API) {
						// if attachment is allowed in bulk api, then calculate size based on whether
						// auto Upload is enabled
						if (Constants.ATTACHMENT_UPLOAD_ENABLED) {
							// If auto upload enabled, To calculate size, replace attachment size with
							// equivalent url size
							extraBytes += numAttachment * Constants.ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES;
							// remove attachments->data key to calculate data size
							apparentBody = new JSONObject(event.toString());
							JSONArray apparentAttachments = apparentBody.getJSONObject("properties")
									.getJSONArray("$attachments");
							for (int i = 0; i < apparentAttachments.length(); i++) {
								apparentAttachments.getJSONObject(i).remove("data");
							}
						} else {
							// if auto upload is not enabled, attachment data will be passed as it is.
						}
					} else {
						// If attachment not allowed, then remove data->$attachments before calculating size
						apparentBody = new JSONObject(event.toString());
						apparentBody.getJSONObject("properties").remove("$attachments");
					}
				} else {
					if (Constants.ATTACHMENT_UPLOAD_ENABLED) {
						// If auto upload enabled, To calculate size, replace attachment size with
						// equivalent url size
						extraBytes += numAttachment * Constants.ATTACHMENT_URL_POTENTIAL_SIZE_IN_BYTES;
						// remove attachments->data key to calculate data size
						apparentBody = new JSONObject(event.toString());
						JSONArray apparentAttachments = apparentBody.getJSONObject("properties")
								.getJSONArray("$attachments");
						for (int i = 0; i < apparentAttachments.length(); i++) {
							apparentAttachments.getJSONObject(i).remove("data");
						}
					} else {
						// if auto upload is not enabled, attachment data will be passed as it is.
					}
				}
			}
		}
		//
		int bodySize = apparentBody.toString().getBytes("utf-8").length;
		int apparentSize = bodySize + extraBytes;
		return apparentSize;
	}

	static int getApparentIdentityEventSize(JSONObject event) throws UnsupportedEncodingException {
		int bodySize = event.toString().getBytes("utf-8").length;
		return bodySize;
	}

	static int getApparentListBroadcastBodySize(JSONObject body) throws UnsupportedEncodingException {
		return body.toString().getBytes("utf-8").length;
	}

	/**
	 * Validate data against the workflow JSON schema
	 * 
	 * @param body worflow payload
	 * @return Validated data
	 * @throws SuprsendException if schema not found.
	 */
	static JSONObject validateWorkflowBodySchema(JSONObject body) throws SuprsendException {
		// --- In case data is not provided, set it to empty dict
		if (body.opt("data") == null) {
			body.put("data", new JSONObject());
		}
		Schema schemaValidator = RequestSchema.getSchemaValidator("workflow");
		try {
			schemaValidator.validate(body);
		} catch (ValidationException e) {
			String msg = String.format("%s\n%s", e.getMessage(), String.join("\n", e.getAllMessages()));
			throw new SuprsendException(msg, e);
		}
		return body;
	}

	/**
	 * Validate data against the event JSON schema
	 * 
	 * @param data event payload
	 * @return Validated data
	 * @throws SuprsendException if schema not found.
	 */
	static JSONObject validateTrackEventSchema(JSONObject data) throws SuprsendException {
		// --- In case props is not provided, set it to empty dict
		if (data.opt("properties") == null) {
			data.put("properties", new JSONObject());
		}
		Schema schemaValidator = RequestSchema.getSchemaValidator("event");
		try {
			schemaValidator.validate(data);
		} catch (ValidationException e) {
			String msg = String.format("%s\n%s", e.getMessage(), String.join("\n", e.getAllMessages()));
			throw new SuprsendException(msg, e);
		}
		return data;
	}

    static JSONObject validateListBroadcastBodySchema(JSONObject body) throws SuprsendException {
		// --- In case data is not provided, set it to empty dict
        if (body.opt("data") == null) {
            body.put("data", new JSONObject());
        }
        Schema schemaValidator = RequestSchema.getSchemaValidator("list_broadcast");
        try {
            schemaValidator.validate(body);
        } catch (ValidationException e) {
            String msg = String.format("%s\n%s", e.getMessage(), String.join("\n", e.getAllMessages()));
            throw new SuprsendException(msg, e);
        }
        return body;
    }

    static String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

	static String buildQueryParams(HashMap<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = params.entrySet().iterator();
        while(iter.hasNext()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            Entry<?, ?> entry = (Entry<?, ?>) iter.next();
			String encK = Utils.urlEncode(String.valueOf(entry.getKey()));
			String encV = Utils.urlEncode(String.valueOf(entry.getValue()));
			sb.append(encK).append("=").append(encV);
        }
        return sb.toString();
    }
}
