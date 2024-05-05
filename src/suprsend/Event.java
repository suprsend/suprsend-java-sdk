package suprsend;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class Event {
	private static final Logger logger = Logger.getLogger(Event.class.getName());

	private String distinctId;
	private String eventName;
	private JSONObject properties;
	private String idempotencyKey;
	private String tenantId;

	static List<String> RESERVED_EVENT_NAMES = Arrays.asList("$identify", "$notification_delivered",
			"$notification_dismiss", "$notification_clicked", "$app_launched", "$user_login", "$user_logout");

	public Event(String distinctId, String eventName, JSONObject properties) {
		this(distinctId, eventName, properties, null, null);
	}

	public Event(String distinctId, String eventName, JSONObject properties, String idempotencyKey) {
		this(distinctId, eventName, properties, idempotencyKey, null);
	}

	public Event(String distinctId, String eventName, JSONObject properties, String idempotencyKey, String tenantId) {
		this.distinctId = distinctId;
		this.eventName = eventName;
		this.properties = properties;
		if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
			this.idempotencyKey = idempotencyKey.trim();
		}
		if (tenantId != null && !tenantId.trim().isEmpty()) {
			this.tenantId = tenantId.trim();
		}
		if (properties == null) {
			properties = new JSONObject();
		}
	}

	private void validateDistinctId() throws InputValueException {
		if (this.distinctId == null || this.distinctId.trim().isEmpty()) {
			throw new InputValueException("distinct_id missing");
		}
		this.distinctId = this.distinctId.trim();
	}

	private void checkEventPrefix(String eventName) throws InputValueException {
		if (Event.RESERVED_EVENT_NAMES.contains(eventName) == false) {
			String eLower = eventName.toLowerCase();
			if (eLower.startsWith("$") || (eLower.length() >= 3 && "ss_".equals(eLower.substring(0, 3)))) {
				throw new InputValueException("event_name starting with [$,ss_] are reserved by SuprSend");
			}
		}
	}

	private void validateEventName() throws InputValueException {
		if (this.eventName == null || this.eventName.trim().isEmpty()) {
			throw new InputValueException("event_name missing");
		}
		this.eventName = this.eventName.trim();
		checkEventPrefix(this.eventName);
	}

	public void addAttachment(String filePath) throws InputValueException {
		this.addAttachment(filePath, null, false);
	}

	public void addAttachment(String filePath, String fileName) throws InputValueException {
		this.addAttachment(filePath, fileName, false);
	}

	public void addAttachment(String filePath, String fileName, boolean ignoreIfError) throws InputValueException {
		JSONObject attachment = Attachment.getAttachmentJSON(filePath, fileName, ignoreIfError);
		if (attachment != null) {
			// --- add the attachment to properties->$attachments
			if (this.properties.optJSONArray("$attachments") == null) {
				this.properties.put("$attachments", new JSONArray());
			}
			JSONArray attachments = this.properties.getJSONArray("$attachments");
			attachments.put(attachment);
		}
	}

	JSONObject getFinalJson(Suprsend config, boolean isPartOfBulk)
			throws InputValueException, SuprsendException, UnsupportedEncodingException {
		// -- validate
		this.validateDistinctId();
		this.validateEventName();
		JSONObject superProps = new JSONObject().put("$ss_sdk_version", config.userAgent);
		JSONObject merged = Utils.mergeJSONObjects(this.properties, superProps);
		JSONObject eventDict = new JSONObject()
				.put("$insert_id", UUID.randomUUID().toString())
				.put("$time", Instant.now().getEpochSecond() * 1000)
				.put("event", this.eventName)
				.put("env", config.apiKey)
				.put("distinct_id", this.distinctId)
				.put("properties", merged);
		if (null != idempotencyKey) {
			eventDict.put("$idempotency_key", idempotencyKey);
		}
		if (null != tenantId) {
			eventDict.put("tenant_id", tenantId);
		}
		JSONObject validatedEvent = Utils.validateTrackEventSchema(eventDict);
		// Check size
		int apparentSize = Utils.getApparentEventSize(eventDict, isPartOfBulk);
		if (apparentSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("Event size too big - %d Bytes, must not cross %s", apparentSize,
					Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new InputValueException(errMsg);
		}
		return new JSONObject().put("event", validatedEvent).put("apparent_size", apparentSize);
	}

	JSONObject asJson() {
		JSONObject eventDict = new JSONObject()
				.put("event", this.eventName)
				.put("distinct_id", this.distinctId)
				.put("properties", this.properties);
		if (null != idempotencyKey) {
			eventDict.put("$idempotency_key", idempotencyKey);
		}
		if (null != tenantId) {
			eventDict.put("tenant_id", tenantId);
		}
		return eventDict;
	}

}
