package suprsend;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class SubscriberListBroadcast {

	private JSONObject body;
	private String idempotencyKey;
	private String tenantId;

	public SubscriberListBroadcast(JSONObject body) throws SuprsendException {
		this(body, null, null);
	}

	public SubscriberListBroadcast(JSONObject body, String idempotencyKey) throws SuprsendException {
		this(body, idempotencyKey, null);
	}

	public SubscriberListBroadcast(JSONObject body, String idempotencyKey, String tenantId) throws SuprsendException {
		if (body == null) {
			throw new SuprsendException("broadcast body must be a passed");
		}
		this.body = body;
		if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
			this.idempotencyKey = idempotencyKey.trim();
		}
		if (tenantId != null && !tenantId.trim().isEmpty()) {
			this.tenantId = tenantId.trim();
		}
	}

	public void addAttachment(String filePath) throws InputValueException {
		this.addAttachment(filePath, null, false);
	}

	public void addAttachment(String filePath, String fileName) throws InputValueException {
		this.addAttachment(filePath, fileName, false);
	}

	/**
	 * 
	 * @param filePath      filePath
	 * @param fileName      fileName
	 * @param ignoreIfError ignoreIfError
	 * @throws InputValueException SuprsendException
	 */
	public void addAttachment(String filePath, String fileName, boolean ignoreIfError) throws InputValueException {
		if (this.body.opt("data") == null) {
			this.body.put("data", new JSONObject());
		}
		JSONObject attachment = Attachment.getAttachmentJSON(filePath, fileName, ignoreIfError);
		if (attachment != null) {
			// --- add the attachment to body->data->$attachments
			if (this.body.getJSONObject("data").optJSONArray("$attachments") == null) {
				this.body.getJSONObject("data").put("$attachments", new JSONArray());
			}
			JSONArray attachments = this.body.getJSONObject("data").getJSONArray("$attachments");
			attachments.put(attachment);
		}
	}

	JSONObject getFinalJson() throws SuprsendException, UnsupportedEncodingException {
		this.body.put("$insert_id", UUID.randomUUID().toString());
		this.body.put("$time", Instant.now().getEpochSecond() * 1000);
		if (null != this.idempotencyKey) {
			this.body.put("$idempotency_key", this.idempotencyKey);
		}
		if (null != this.tenantId) {
			this.body.put("tenant_id", this.tenantId);
		}
		JSONObject validatedBody = Utils.validateListBroadcastBodySchema(this.body);
		this.body = validatedBody;

		int apparentSize = Utils.getApparentListBroadcastBodySize(this.body);
		if (apparentSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			throw new SuprsendException(
					String.format("SubscriberListBroadcast body too big - %d Bytes, must not cross %s", apparentSize,
							Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE));
		}
		return new JSONObject().put("event", this.body).put("apparent_size", apparentSize);
	}
}
