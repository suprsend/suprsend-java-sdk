package suprsend;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class BulkWorkflowTrigger {
	private static final Logger logger = Logger.getLogger(BulkWorkflowTrigger.class.getName());

	private Suprsend config;

	private List<WorkflowTriggerRequest> __workflows;
	private List<JSONObject> __pendingRecords;

	private List<BulkWorkflowTriggerChunk> chunks;
	private BulkResponse response;

	private List<JSONObject> __invalidRecords;

	BulkWorkflowTrigger(Suprsend config) {
		this.config = config;
		this.__workflows = new ArrayList<WorkflowTriggerRequest>();
		this.__pendingRecords = new ArrayList<JSONObject>();
		this.chunks = new ArrayList<>();
		this.response = new BulkResponse();
		// invalid_record json: {"record": event-json, "error": error_str, "code": 500}
		this.__invalidRecords = new ArrayList<>();
	}

	private void validateWorkflows() {
		for (WorkflowTriggerRequest wf : this.__workflows) {
			try {
				// {"event", validatedEvent, "apparent_size", apparentSize}
				JSONObject wfJson = wf.getFinalJson(this.config, true);
				this.__pendingRecords.add(wfJson);
			} catch (Exception ex) {
				// invalid_record json: {"record": event-json, "error": error_str, "code": 500}
				JSONObject invRec = Utils.invalidRecordJson(wf.asJson(), ex);
				this.__invalidRecords.add(invRec);
			}
		}
	}

	private void chunkify(int startIdx) throws InputValueException {
		BulkWorkflowTriggerChunk currChunk = new BulkWorkflowTriggerChunk(this.config);
		this.chunks.add(currChunk);
		// loop on slice pendingRecords[startIdx:]
		int recordsLen = this.__pendingRecords.size();
		List<JSONObject> slice = this.__pendingRecords.subList(startIdx, recordsLen);
		//
		for (int idx = 0; idx < slice.size(); idx++) {
			JSONObject wfJson = slice.get(idx);
			boolean isAdded = currChunk.tryToAddIntoChunk(wfJson.getJSONObject("event"),
					wfJson.getInt("apparent_size"));
			if (!isAdded) {
				// create chunks from remaining records
				chunkify(startIdx + idx);
				// Don't forget to break. As current loop must not continue further
				break;
			}
		}
	}

	public void append(WorkflowTriggerRequest... workflows) {
		if (workflows.length == 0) {
			return;
		}
		for (WorkflowTriggerRequest obj : workflows) {
			if (obj == null) {
				continue;
			}
			this.__workflows.add(obj);
		}
	}

	public BulkResponse trigger() throws InputValueException {
		validateWorkflows();
		if (this.__invalidRecords.size() > 0) {
			JSONObject chResponse = BulkResponse.invalidRecordsChunkResponse(this.__invalidRecords);
			this.response.mergeChunkResponse(chResponse);
		}
		if (this.__pendingRecords.size() > 0) {
			chunkify(0);
			for (int cIdx = 0; cIdx < this.chunks.size(); cIdx++) {
				BulkWorkflowTriggerChunk chunk = this.chunks.get(cIdx);
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
		return this.response;
	}
}
