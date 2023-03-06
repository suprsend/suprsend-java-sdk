package suprsend;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkSubscribers {
    private static final Logger logger = Logger.getLogger(BulkSubscribers.class.getName());

    private final Suprsend config;

    private List<Subscriber> __subscribers;
    private List<JSONObject> __pendingRecords;

    private List<BulkSubscribersChunk> chunks;
    private BulkResponse response;

    BulkSubscribers(Suprsend config) {
        this.config = config;
        this.__subscribers = new ArrayList<Subscriber>();
        this.__pendingRecords = new ArrayList<JSONObject>();
        this.chunks = new ArrayList<>();
        this.response = new BulkResponse();
    }

    private void validateSubscribers() throws SuprsendException, UnsupportedEncodingException {
        if (this.__subscribers.isEmpty()) {
            throw new SuprsendException("users list is empty in bulk request");
        }
        for (Subscriber s : this.__subscribers) {
            // check if there is any error/warning, if so add it to warnings list of BulkResponse
            List<String> warningsList = s.validateBody(true);
            if (!warningsList.isEmpty())
                this.response.warnings.addAll(warningsList);
            // 
            JSONObject ev = s.getEvent();
            // {"event", validatedEvent, "apparent_size", apparentSize}
            JSONObject evJson = s.validateEventSize(ev);
            this.__pendingRecords.add(evJson);
        }
    }

    private void chunkify(int startIdx) throws SuprsendException {
        BulkSubscribersChunk currChunk = new BulkSubscribersChunk(this.config);
        this.chunks.add(currChunk);
        // loop on slice pendingRecords[startIdx:]
        int recordsLen = this.__pendingRecords.size();
        List<JSONObject> slice = this.__pendingRecords.subList(startIdx, recordsLen);
        // 
        for (int idx = 0; idx < slice.size(); idx++) {
            JSONObject evJson = slice.get(idx);
            boolean isAdded = currChunk.tryToAddIntoChunk(evJson.getJSONObject("event"), evJson.getInt("apparent_size"));
            if (!isAdded) {
                // create chunks from remaining records
                chunkify(startIdx + idx);
                // Don't forget to break. As current loop must not continue further
                break;
            }
        }
    }

    public void append(Subscriber... subscribers) throws SuprsendException {
        if (subscribers.length == 0) {
            throw new SuprsendException("users list empty. must pass one or more users");
        }
        for (Subscriber obj : subscribers) {
            if (obj == null) {
                continue;
            }
            // TODO: deep copy
            this.__subscribers.add(obj);
        }
    }

    public BulkResponse trigger() throws SuprsendException, UnsupportedEncodingException {
        return save();
    }

    public BulkResponse save() throws SuprsendException, UnsupportedEncodingException {
        validateSubscribers();
        chunkify(0);
        for (int cIdx = 0; cIdx < this.chunks.size(); cIdx++) {
            BulkSubscribersChunk chunk = this.chunks.get(cIdx);
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
