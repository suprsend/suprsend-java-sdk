package suprsend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class BulkWorkflowTriggerChunk {
	private static final Logger logger = Logger.getLogger(BulkWorkflowTriggerChunk.class.getName());

	private static final int chunkApparentSizeInBytes = Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES;
	private static final int maxRecordsInChunk = Constants.MAX_WORKFLOWS_IN_BULK_API;

	private final Suprsend config;
	private List<JSONObject> chunk;
	private String url;
	private int runningSize;
	private int runningLength;

	JSONObject response;

	BulkWorkflowTriggerChunk(Suprsend config) {
		this.config = config;
		this.chunk = new ArrayList<JSONObject>();
		this.url = String.format("%strigger/", this.config.baseUrl);
		//
		this.runningSize = 0;
		this.runningLength = 0;
		this.response = new JSONObject();
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent)
				.put("Date", Utils.getCurrentDateTimeHeader());
	}

	private void addBodyToChunk(JSONObject body, int bodySize) {
		this.runningSize += bodySize;
		this.chunk.add(body);
		this.runningLength += 1;
	}

	private boolean checkLimitReached() {
		return (this.runningLength >= BulkWorkflowTriggerChunk.maxRecordsInChunk
				|| this.runningSize >= BulkWorkflowTriggerChunk.chunkApparentSizeInBytes);
	}

	/**
	 * returns whether passed body was able to get added to this chunk or not, if
	 * true, body gets added to chunk
	 */
	boolean tryToAddIntoChunk(JSONObject body, int bodySize) throws InputValueException {
		if (body == null) {
			return true;
		}
		if (checkLimitReached()) {
			return false;
		}
		if (bodySize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			throw new InputValueException(String.format("workflow body too big - %d Bytes, must not cross %s", bodySize,
					Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE));
		}
		// if apparent_size of body crosses limit
		if (this.runningSize + bodySize > BulkWorkflowTriggerChunk.chunkApparentSizeInBytes) {
			return false;
		}
		if (!Constants.ALLOW_ATTACHMENTS_IN_BULK_API) {
			body.getJSONObject("data").remove("$attachments");
		}
		// Add workflow to chunk
		addBodyToChunk(body, bodySize);
		return true;
	}

	void trigger() {
		JSONObject headers = getHeaders();
		try {
			// Signature and Authorization Header
			JSONObject sigResult = Signature.getRequestSignature(this.url, HttpMethod.POST, this.chunk.toString(),
					headers, this.config.apiSecret);
			String contentText = sigResult.getString("contentTxt");
			headers.put("Authorization", String.format("%s:%s", this.config.apiKey, sigResult.getString("signature")));
			// --- Make HTTP POST request
			SuprsendResponse resp = RequestLogs.makeHttpCall(logger, this.config.debug, HttpMethod.POST, this.url,
					headers, contentText);
			int statusCode = resp.statusCode;
			String responseText = resp.responseText;
			//
			if (statusCode >= 200 && statusCode < 300) {
				this.response.put("status", "success").put("status_code", statusCode).put("total", this.chunk.size())
						.put("success", this.chunk.size()).put("failure", 0)
						.put("failed_records", new ArrayList<JSONObject>());
			} else {
				this.response.put("status", "fail").put("status_code", statusCode).put("total", this.chunk.size())
						.put("success", 0).put("failure", this.chunk.size())
						.put("failed_records", getFailedRecords(statusCode, responseText));
			}
		} catch (SuprsendException | IOException e) {
			this.response.put("status", "fail").put("status_code", 500).put("total", this.chunk.size())
					.put("success", 0).put("failure", this.chunk.size())
					.put("failed_records", getFailedRecords(500, e.toString()));
		}

	}

	private List<JSONObject> getFailedRecords(int statusCode, String errMsg) {
		return this.chunk.stream()
				.map(c -> new JSONObject().put("record", c).put("error", errMsg).put("code", statusCode))
				.collect(Collectors.toList());
	}
}
