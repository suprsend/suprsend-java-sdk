package suprsend;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkWorkflows {
    private static final Logger logger = Logger.getLogger(BulkWorkflows.class.getName());

    private Suprsend config;

    private List<Workflow> __workflows;
    private List<JSONObject> __pendingRecords;

    private List<BulkWorkflowsChunk> chunks;
    private BulkResponse response;

    BulkWorkflows(Suprsend config) {
        this.config = config;
        this.__workflows = new ArrayList<Workflow>();
        this.__pendingRecords = new ArrayList<JSONObject>();
        this.chunks = new ArrayList<>();
        this.response = new BulkResponse();
    }

    private void validateWorkflows() throws SuprsendException, UnsupportedEncodingException {
        if (this.__workflows.isEmpty()) {
            throw new SuprsendException("workflow list is empty in bulk request");
        }
        for (Workflow wf : this.__workflows) {
            // {"event", validatedEvent, "apparent_size", apparentSize}
            JSONObject wfJson = wf.getFinalJson(this.config, true);
            this.__pendingRecords.add(wfJson);
        }
    }

    private void chunkify(int startIdx) throws SuprsendException {
        BulkWorkflowsChunk currChunk = new BulkWorkflowsChunk(this.config);
        this.chunks.add(currChunk);
        // loop on slice pendingRecords[startIdx:]
        int recordsLen = this.__pendingRecords.size();
        List<JSONObject> slice = this.__pendingRecords.subList(startIdx, recordsLen);
        //
        for (int idx = 0; idx < slice.size(); idx++) {
            JSONObject wfJson = slice.get(idx);
            boolean isAdded = currChunk.tryToAddIntoChunk(wfJson.getJSONObject("event"), wfJson.getInt("apparent_size"));
            if (!isAdded) {
                // create chunks from remaining records
                chunkify(startIdx + idx);
                // Don't forget to break. As current loop must not continue further
                break;
            }
        }
    }

    public void append(Workflow... workflows) throws SuprsendException {
        if (workflows.length == 0) {
            throw new SuprsendException("workflow list empty. must pass one or more valid workflow instances");
        }
        for (Workflow obj : workflows) {
            if (obj == null) {
                continue;
            }
            this.__workflows.add(obj);
        }
    }

    public BulkResponse trigger() throws SuprsendException, UnsupportedEncodingException {
        validateWorkflows();
        chunkify(0);
        for (int cIdx = 0; cIdx < this.chunks.size(); cIdx++) {
            BulkWorkflowsChunk chunk = this.chunks.get(cIdx);
            if (this.config.debug) {
                logger.log(Level.INFO, "DEBUG: triggering api call for chunk: " + cIdx);
            }
            // do api call
            chunk.trigger();
            // merge response
            this.response.mergeChunkResponse(chunk.response);
        }
        return this.response;
    }

}
