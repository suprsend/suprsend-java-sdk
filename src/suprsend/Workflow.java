package suprsend;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @deprecated. Use WorkflowTriggerRequest() instead
 */
@Deprecated
public class Workflow {
	private static final Logger logger = Logger.getLogger(Workflow.class.getName());

	private JSONObject body;
	private String idempotencyKey;
	private String tenantId;

	/**
	 * 
	 * @param body json body of workflow
	 */
	public Workflow(JSONObject body) {
		this(body, null, null);
	}

	/**
	 * 
	 * @param body           json body of workflow
	 * @param idempotencyKey idempotency-key for workflow request
	 */
	public Workflow(JSONObject body, String idempotencyKey) {
		this(body, idempotencyKey, null);
	}

	/**
	 * 
	 * @param body           json body of workflow
	 * @param idempotencyKey idempotency-key for workflow request
	 * @param tenantId       tenantId for workflow request
	 */
	public Workflow(JSONObject body, String idempotencyKey, String tenantId) {
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
		//
		JSONObject validatedBody = Utils.validateWorkflowBodySchema(this.body);
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
		return obj;
	}

}
