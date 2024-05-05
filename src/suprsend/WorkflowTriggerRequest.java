package suprsend;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class WorkflowTriggerRequest {
	private static final Logger logger = Logger.getLogger(WorkflowTriggerRequest.class.getName());

	private JSONObject body;
	private String idempotencyKey;
	private String tenantId;
	private String cancellationKey;

	/**
	 * 
	 * @param body json body of workflow
	 */
	public WorkflowTriggerRequest(JSONObject body) {
		this(body, null, null, null);
	}

	/**
	 * 
	 * @param body           json body of workflow
	 * @param idempotencyKey idempotency-key for workflow request
	 */
	public WorkflowTriggerRequest(JSONObject body, String idempotencyKey) {
		this(body, idempotencyKey, null, null);
	}

	/**
	 * 
	 * @param body           json body of workflow
	 * @param idempotencyKey idempotency-key for workflow request
	 * @param tenantId       tenantId for workflow request
	 */
	public WorkflowTriggerRequest(JSONObject body, String idempotencyKey, String tenantId) {
		this(body, idempotencyKey, tenantId, null);
	}

	/**
	 * 
	 * @param body            json body of workflow
	 * @param idempotencyKey  idempotency-key for workflow request
	 * @param tenantId        tenantId for workflow request
	 * @param cancellationKey cancellationKey for workflow request
	 */
	public WorkflowTriggerRequest(JSONObject body, String idempotencyKey, String tenantId, String cancellationKey) {
		if (null == body) {
			body = new JSONObject();
		}
		this.body = body;
		if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
			this.idempotencyKey = idempotencyKey.trim();
		}
		if (tenantId != null && !tenantId.trim().isEmpty()) {
			this.tenantId = tenantId.trim();
		}
		if (cancellationKey != null && !cancellationKey.trim().isEmpty()) {
			this.cancellationKey = cancellationKey.trim();
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

	JSONObject getFinalJson(Suprsend config, boolean isPartOfBulk)
			throws SuprsendException, UnsupportedEncodingException {
		// add idempotency key in body if present
		if (null != idempotencyKey) {
			this.body.put("$idempotency_key", idempotencyKey);
		}
		if (null != tenantId) {
			this.body.put("tenant_id", tenantId);
		}
		if (null != cancellationKey) {
			this.body.put("cancellation_key", cancellationKey);
		}
		//
		JSONObject validatedBody = Utils.validateWorkflowTriggerBodySchema(this.body);
		// Check body size
		int apparentSize = Utils.getApparentWorkflowBodySize(body, isPartOfBulk);
		if (apparentSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("workflow body too big - %d Bytes, must not cross %s", apparentSize,
					Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new SuprsendException(errMsg);
		}
		return new JSONObject().put("event", validatedBody).put("apparent_size", apparentSize);
	}

	JSONObject asJson() {
		JSONObject obj = new JSONObject(this.body); // TODO: check if this clone works fine??
		if (null != idempotencyKey) {
			obj.put("$idempotency_key", idempotencyKey);
		}
		if (null != tenantId) {
			obj.put("tenant_id", tenantId);
		}
		if (null != cancellationKey) {
			obj.put("cancellation_key", cancellationKey);
		}
		return obj;
	}
}
