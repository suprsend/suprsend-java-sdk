package suprsend;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

public class Workflow {
	private static final Logger logger = Logger.getLogger(Workflow.class.getName());

	private JSONObject body;
	private String idempotencyKey;

	/**
	 * 
	 * @param body json body of workflow
	 */
	public Workflow(JSONObject body) {
		if (null == body) {
			body = new JSONObject();
		}
		this.body = body;
	}

	/**
	 * 
	 * @param body json body of workflow
	 * @param idempotencyKey idempotency-key for workflow request
	 */
	public Workflow(JSONObject body, String idempotencyKey) {
		if (null == body) {
			body = new JSONObject();
		}
		this.body = body;
		if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
			this.idempotencyKey = idempotencyKey.trim();
		}
	}

	/**
	 * 
	 * @param filePath local path of attachment
	 * @throws Exception error while reading file path
	 */
	public void addAttachment(String filePath) throws Exception {
		if (this.body.opt("data") == null) {
			this.body.put("data", new JSONObject());
		}
		JSONObject attachment = Attachment.getAttachmentJSONForFile(filePath);
		// --- add the attachment to body->data->$attachments
		if (this.body.getJSONObject("data").optJSONArray("$attachments") == null) {
			this.body.getJSONObject("data").put("$attachments", new JSONArray());
		}
		JSONArray attachments = this.body.getJSONObject("data").getJSONArray("$attachments");
		attachments.put(attachment);
	}

	protected JSONObject getFinalJson(Suprsend config, boolean isPartOfBulk)
			throws SuprsendException, UnsupportedEncodingException {
		// add idempotency key in body if present
		if (null != idempotencyKey) {
			this.body.put("$idempotency_key", idempotencyKey);
		}
		//
		JSONObject validatedBody = Utils.validateWorkflowSchema(this.body);
		// Check body size
		int apparentSize = Utils.getApparentWorkflowBodySize(body, isPartOfBulk);
		if (apparentSize > Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES) {
			String errMsg = String.format("workflow body too big - %d Bytes, must not cross %s", apparentSize,
					Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
			throw new SuprsendException(errMsg);
		}
		return new JSONObject().put("event", validatedBody).put("apparent_size", apparentSize);
	}
}
