package suprsend;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class BulkUsersEdit {
	private static final Logger logger = Logger.getLogger(BulkUsersEdit.class.getName());

	private final Suprsend config;

	private List<UserEdit> __users;
	private List<JSONObject> __pendingRecords;

	private List<BulkUsersEditChunk> chunks;
	private BulkResponse response;

	// invalid_record json: {"record": payload-json, "error": error_str, "code":
	// 500}
	private List<JSONObject> __invalidRecords;

	BulkUsersEdit(Suprsend config) {
		this.config = config;
		this.__users = new ArrayList<UserEdit>();
		this.__pendingRecords = new ArrayList<JSONObject>();
		// invalid_record json: {"record": event-json, "error": error_str, "code": 500}
		this.__invalidRecords = new ArrayList<JSONObject>();
		this.chunks = new ArrayList<>();
		this.response = new BulkResponse();
	}

	private void validateUsers() {
		for (UserEdit u : this.__users) {
			try {
				// check if there is any error/warning, if so add it to warnings list of
				// BulkResponse
				List<String> warningsList = u.validateBody();
				if (!warningsList.isEmpty())
					this.response.warnings.addAll(warningsList);
				//
				JSONObject pl = u.getAsyncPayload();
				// {"payload", validatedPayload, "apparent_size", apparentSize}
				JSONObject plJson = u.validatPayloadSize(pl);
				this.__pendingRecords.add(plJson);

			} catch (Exception ex) {
				// invalid_record json: {"record": payload-json, "error": error_str, "code":
				// 500}
				JSONObject invRec = Utils.invalidRecordJson(u.asJsonAsync(), ex);
				this.__invalidRecords.add(invRec);
			}
		}
	}

	private void chunkify(int startIdx) throws InputValueException {
		BulkUsersEditChunk currChunk = new BulkUsersEditChunk(this.config);
		this.chunks.add(currChunk);
		// loop on slice pendingRecords[startIdx:]
		int recordsLen = this.__pendingRecords.size();
		List<JSONObject> slice = this.__pendingRecords.subList(startIdx, recordsLen);
		//
		for (int idx = 0; idx < slice.size(); idx++) {
			JSONObject plJson = slice.get(idx);
			boolean isAdded = currChunk.tryToAddIntoChunk(plJson.getJSONObject("payload"),
					plJson.getInt("apparent_size"));
			if (!isAdded) {
				// create chunks from remaining records
				chunkify(startIdx + idx);
				// Don't forget to break. As current loop must not continue further
				break;
			}
		}
	}

	public void append(UserEdit... users) {
		if (users.length == 0) {
			return;
		}
		for (UserEdit obj : users) {
			if (obj == null) {
				continue;
			}
			// TODO: deep copy
			this.__users.add(obj);
		}
	}

	public BulkResponse save() throws InputValueException {
		validateUsers();
		if (this.__invalidRecords.size() > 0) {
			JSONObject chResponse = BulkResponse.invalidRecordsChunkResponse(this.__invalidRecords);
			this.response.mergeChunkResponse(chResponse);
		}
		if (this.__pendingRecords.size() > 0) {
			chunkify(0);
			for (int cIdx = 0; cIdx < this.chunks.size(); cIdx++) {
				BulkUsersEditChunk chunk = this.chunks.get(cIdx);
				if (this.config.debug) {
					logger.log(Level.INFO, "DEBUG: triggering api call for chunk: " + cIdx);
				}
				// do api call
				chunk.trigger();
				// merge response
				this.response.mergeChunkResponse(chunk.response);
			}
		} else {
			// if no records. i.e. len(invalid_records) and len(pending_records) both are 0
			// then add empty success response
			if (this.__invalidRecords.size() == 0) {
				this.response.mergeChunkResponse(BulkResponse.emptyChunkSuccessResponse());
			}
		}
		// -----
		return this.response;
	}

}
