package suprsend;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Event {
	private static final Logger logger = Logger.getLogger(Event.class.getName());

	private String distinctID;
	private String eventName;
	private JSONObject properties;
	private String idempotencyKey;

	public static List<String> RESERVED_EVENT_NAMES = Arrays.asList("$identify", "$notification_delivered",
			"$notification_dismiss", "$notification_clicked", "$app_launched", "$user_login", "$user_logout");

	private void validateParams() throws SuprsendException {
		this.validateDistinctId();
		this.validateEventName();
		this.validateProperties();
	}

	public Event(String distinctID, String eventName, JSONObject properties) throws SuprsendException {
		this.distinctID = distinctID;
		this.eventName = eventName;
		this.properties = properties;
		// --- validate
		validateParams();
	}

	public Event(String distinctID, String eventName, JSONObject properties, String idempotencyKey)
			throws SuprsendException {
		this.distinctID = distinctID;
		this.eventName = eventName;
		this.properties = properties;
		if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
			this.idempotencyKey = idempotencyKey.trim();
		}
		// --- validate
		validateParams();
	}

	private void validateDistinctId() throws SuprsendException {
		if (this.distinctID == null || this.distinctID.trim().isEmpty()) {
			throw new SuprsendException("distinct_id missing");
		}
		this.distinctID = this.distinctID.trim();
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

	public void addAttachment(String filePath) throws Exception {
		JSONObject attachment = Attachment.getAttachmentJSONForFile(filePath);
		// --- add the attachment to properties->$attachments
		if (this.properties.optJSONArray("$attachments") == null) {
			this.properties.put("$attachments", new JSONArray());
		}
		JSONArray attachments = this.properties.getJSONArray("$attachments");
		attachments.put(attachment);
	}

	public JSONObject getFinalJson(Suprsend config, boolean isPartOfBulk)
			throws SuprsendException, UnsupportedEncodingException {
		JSONObject superProps = new JSONObject().put("$ss_sdk_version", config.userAgent);
		JSONObject merged = Utils.mergeJSONObjects(this.properties, superProps);
		JSONObject eventDict = new JSONObject()
				.put("$insert_id", UUID.randomUUID().toString())
				.put("$time", Instant.now().getEpochSecond() * 1000)
				.put("event", this.eventName)
				.put("env", config.workspaceKey)
				.put("distinct_id", this.distinctID)
				.put("properties", merged);
		if (null != idempotencyKey) {
			eventDict.put("$idempotency_key", idempotencyKey);
		}
		JSONObject validatedEvent = Utils.validateEventSchema(eventDict);
		// Check size
		int apparentSize = Utils.getApparentEventSize(eventDict, isPartOfBulk);
		if (apparentSize > Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("Event properties too big - %d Bytes, must not cross %s", apparentSize,
					Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new SuprsendException(errMsg);
		}
		return new JSONObject().put("event", validatedEvent).put("apparent_size", apparentSize);

	}

}
