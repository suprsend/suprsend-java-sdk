package suprsend;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkflowBatch {
	Suprsend config;
	JSONArray pendingRecords;
	List<BatchChunk> chunks;
	BatchResponse response;
	
	public WorkflowBatch(Suprsend config) {
		this.config = config;
		this.pendingRecords = new JSONArray();
		this.chunks = new ArrayList<BatchChunk>();
		this.response = new BatchResponse();
	}
	
	private void validateBody() throws SuprsendException {
		if (this.pendingRecords.length() <= 0) {
			throw new SuprsendException("body is empty in batch request");
		}
		for (int i=0; i < this.pendingRecords.length(); i++) {
			Utils.validateWorkflowBodySchema(this.pendingRecords.getJSONObject(i));
		}
	}
	
	private void chunkify(int startIndex) throws JSONException, SuprsendException {
		BatchChunk currentChunk = new BatchChunk(this.config);
		this.chunks.add(currentChunk);
		for (int i=startIndex; i < this.pendingRecords.length() ; i++) {
			boolean isAdded = currentChunk.tryToAddInfoChunk(this.pendingRecords.getJSONObject(i));
			if (isAdded == false) {
				chunkify(startIndex + i);
				break;
			}
		}
	}
	
	public void append(JSONArray body) throws SuprsendException {
		if (body.length() <= 0) {
			throw new SuprsendException("body list empty. must pass one or more valid workflow body");
		}
		for (int i=0; i < body.length(); i++) {
			JSONObject bd = body.getJSONObject(i);
			if (bd.length() <= 0) {
				throw new SuprsendException("body element is empty. must be a valid workflow body");
			}
			JSONObject bdCopy = new JSONObject(bd.toString());
			this.pendingRecords.put(bdCopy);
		}
	}
	
	public BatchResponse trigger() throws SuprsendException {
		validateBody();
		chunkify(0);
		for (int i=0; i < this.chunks.size(); i++) {
			BatchChunk ch = this.chunks.get(i);
			if (this.config.debug) {
				System.out.println(String.format("DEBUG: triggering api call for chunk: %s", i));
			}
			ch.trigger();
			this.response.mergeChunkResponse(ch.response);
		}
		return this.response;
	}
}
