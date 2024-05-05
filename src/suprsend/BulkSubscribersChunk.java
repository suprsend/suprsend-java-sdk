package suprsend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

class BulkSubscribersChunk {
	private static final Logger logger = Logger.getLogger(BulkSubscribersChunk.class.getName());

	private static final int chunkApparentSizeInBytes = Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES;
	// private static final String chunkApparentSizeInBytesReadable =
	// Constants.BODY_MAX_APPARENT_SIZE_IN_BYTES_READABLE;
	private static final int maxRecordsInChunk = Constants.MAX_IDENTITY_EVENTS_IN_BULK_API;

	private final Suprsend config;
	private List<JSONObject> chunk;
	private String url;
	private int runningSize;
	private int runningLength;

	JSONObject response;

	BulkSubscribersChunk(Suprsend config) {
		this.config = config;
		this.chunk = new ArrayList<JSONObject>();
		this.url = String.format("%sevent/", this.config.baseUrl);
		//
		this.runningSize = 0;
		this.runningLength = 0;
		this.response = new JSONObject();
	}

	private JSONObject getHeaders() {
		return new JSONObject().put("Content-Type", "application/json; charset=utf-8")
				.put("User-Agent", this.config.userAgent).put("Date", Utils.getCurrentDateTimeHeader());
	}

	private void addEventToChunk(JSONObject event, int eventSize) {
		this.runningSize += eventSize;
		this.chunk.add(event);
		this.runningLength += 1;
	}

	private boolean checkLimitReached() {
		return (this.runningLength >= BulkSubscribersChunk.maxRecordsInChunk
				|| this.runningSize >= BulkSubscribersChunk.chunkApparentSizeInBytes);
	}

	boolean tryToAddIntoChunk(JSONObject event, int eventSize) throws InputValueException {
		if (event == null) {
			return true;
		}
		if (checkLimitReached()) {
			return false;
		}
		if (eventSize > Constants.IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
			throw new InputValueException(String.format("Event too big - %d Bytes, must not cross %s", eventSize,
					Constants.IDENTITY_SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE));
		}
		// if apparent_size of event crosses limit
		if (this.runningSize + eventSize > BulkSubscribersChunk.chunkApparentSizeInBytes) {
			return false;
		}
		// Add Event to chunk
		addEventToChunk(event, eventSize);
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
