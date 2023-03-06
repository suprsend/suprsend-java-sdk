package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Event {
	private static final Logger logger = Logger.getLogger(Event.class.getName());

	private String distinctId;
	private String eventName;
	private JSONObject properties;
	private String idempotencyKey;
	private String brandId;

	static List<String> RESERVED_EVENT_NAMES = Arrays.asList("$identify", "$notification_delivered",
	        "$notification_dismiss", "$notification_clicked", "$app_launched", "$user_login", "$user_logout");

	private void validateParams() throws SuprsendException {
		this.validateDistinctId();
		this.validateEventName();
		this.validateProperties();
	}

	public Event(String distinctId, String eventName, JSONObject properties) throws SuprsendException {
		this(distinctId, eventName, properties, null, null);
	}

	public Event(String distinctId, String eventName, JSONObject properties, String idempotencyKey)
			throws SuprsendException {
		this(distinctId, eventName, properties, idempotencyKey, null);
	}

	public Event(String distinctId, String eventName, JSONObject properties, String idempotencyKey, String brandId)
			throws SuprsendException {
		this.distinctId = distinctId;
		this.eventName = eventName;
		this.properties = properties;
		if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
			this.idempotencyKey = idempotencyKey.trim();
		}
		if (brandId != null && !brandId.trim().isEmpty()) {
			this.brandId = brandId.trim();
		}
		// --- validate
		validateParams();
	}

	private void validateDistinctId() throws SuprsendException {
		if (this.distinctId == null || this.distinctId.trim().isEmpty()) {
			throw new SuprsendException("distinct_id missing");
		}
		this.distinctId = this.distinctId.trim();
	}

	private void checkEventPrefix(String eventName) throws SuprsendException {
		if (Event.RESERVED_EVENT_NAMES.contains(eventName) == false) {
			String eLower = eventName.toLowerCase();
			if (eLower.startsWith("$") || (eLower.length() >= 3 && "ss_".equals(eLower.substring(0, 3)))) {
				throw new SuprsendException("event_name starting with [$,ss_] are reserved");
			}
		}
	}

	private void validateEventName() throws SuprsendException {
		if (this.eventName == null || this.eventName.trim().isEmpty()) {
			throw new SuprsendException("event_name must be passed");
		}
		this.eventName = this.eventName.trim();
		checkEventPrefix(this.eventName);
	}

	private void validateProperties() {
		if (this.properties == null) {
			this.properties = new JSONObject();
		}
	}

	public void addAttachment(String filePath) throws SuprsendException, IOException {
		this.addAttachment(filePath, null, false);
	}

	public void addAttachment(String filePath, String fileName) throws SuprsendException, IOException {
		this.addAttachment(filePath, fileName, false);
	}

	public void addAttachment(String filePath, String fileName, boolean ignoreIfError) throws SuprsendException, IOException {
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
			throws SuprsendException, UnsupportedEncodingException {
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
		if (null != brandId) {
			eventDict.put("brand_id", brandId);
		}
		JSONObject validatedEvent = Utils.validateTrackEventSchema(eventDict);
		// Check size
		int apparentSize = Utils.getApparentEventSize(eventDict, isPartOfBulk);
		if (apparentSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("Event size too big - %d Bytes, must not cross %s", apparentSize,
			        Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new SuprsendException(errMsg);
		}
		return new JSONObject().put("event", validatedEvent).put("apparent_size", apparentSize);
	}

}
