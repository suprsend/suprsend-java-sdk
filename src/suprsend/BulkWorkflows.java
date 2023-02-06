package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkWorkflows {
    private static final Logger logger = Logger.getLogger(BulkWorkflows.class.getName());
    private Suprsend config;
    private ArrayList<Workflow> _workflows = new ArrayList<Workflow>();
    private JSONArray workflowsJA = new JSONArray();
    private List<BulkWorkflowsChunk> chunks = new ArrayList<>();
    private BulkWorkflowsChunk currChunk;

    private BulkResponse bulkResponse = new BulkResponse();

    BulkWorkflows(Suprsend config) {
        this.config = config;
    }

    private void validateWorkflows() throws SuprsendException, UnsupportedEncodingException {
        if (_workflows.isEmpty()) {
            throw new SuprsendException("events list is empty in bulk request");
        }
        for (Workflow wf : _workflows) {
            JSONObject evJson = wf.getFinalJson(config, true);
            workflowsJA.put(evJson);
        }
    }

    private void validateEvents() throws SuprsendException, UnsupportedEncodingException {
        if (_workflows.isEmpty()) {
            throw new SuprsendException("events list is empty in bulk request");
        }
        for (Workflow wf : _workflows) {
            JSONObject evJson = wf.getFinalJson(config, true);
            workflowsJA.put(evJson);
        }
    }

    private void chunkify(int start) throws SuprsendException {
        currChunk = new BulkWorkflowsChunk(config);
        chunks.add(currChunk);
        for (int i = 0; i < workflowsJA.length(); i++) {
            JSONObject event = workflowsJA.getJSONObject(i);
            int apparentSize = event.getInt("apparent_size");
            boolean isAdded = currChunk.tryToAddIntoChunk(event.getJSONObject("event"), apparentSize);
            if (!isAdded) {
                chunkify(start + i);
                break;
            }
        }
    }

    public void append(List<Workflow> workflows) throws SuprsendException {
        if (workflows.isEmpty()) {
            System.out.println("events list empty. must pass one or more events");
            return;
        }
        for (Workflow wf : workflows) {
            if (wf == null) {
                throw new SuprsendException("null/empty element found in bulk instance");
            }
            //Todo : niks Clone events
            _workflows.addAll(workflows);
        }
    }

    public BulkResponse trigger() throws SuprsendException, UnsupportedEncodingException {
        validateEvents();
        chunkify(0);
        for (int i = 0; i < chunks.size(); i++) {
            BulkWorkflowsChunk chunk = chunks.get(i);
            logger.log(Level.INFO, "triggering api call for chunk:" + i);
            chunk.trigger();
            bulkResponse.mergeChunkResponse(chunk.response);
        }
        return bulkResponse;
    }

}
