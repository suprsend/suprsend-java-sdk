package suprsend;

import org.json.JSONObject;

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

    // invalid_record json: {"record": event-json, "error": error_str, "code": 500}
    private List<JSONObject> __invalidRecords;

    BulkSubscribers(Suprsend config) {
        this.config = config;
        this.__subscribers = new ArrayList<Subscriber>();
        this.__pendingRecords = new ArrayList<JSONObject>();
        // invalid_record json: {"record": event-json, "error": error_str, "code": 500}
        this.__invalidRecords = new ArrayList<JSONObject>();
        this.chunks = new ArrayList<>();
        this.response = new BulkResponse();

    }

    private void validateSubscribers() {
        for (Subscriber s : this.__subscribers) {
            try {
                // check if there is any error/warning, if so add it to warnings list of BulkResponse
                List<String> warningsList = s.validateBody(true);
                if (!warningsList.isEmpty())
                    this.response.warnings.addAll(warningsList);
                // 
                JSONObject ev = s.getEvent();
                // {"event", validatedEvent, "apparent_size", apparentSize}
                JSONObject evJson = s.validateEventSize(ev);
                this.__pendingRecords.add(evJson);

            } catch (Exception ex) {
                // invalid_record json: {"record": event-json, "error": error_str, "code": 500}
                JSONObject invRec = Utils.invalidRecordJson(s.asJson(), ex);
                this.__invalidRecords.add(invRec);
            }
        }
    }

    private void chunkify(int startIdx) throws InputValueException {
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

    public void append(Subscriber... subscribers) {
        if (subscribers.length == 0) {
            return;
        }
        for (Subscriber obj : subscribers) {
            if (obj == null) {
                continue;
            }
            // TODO: deep copy
            this.__subscribers.add(obj);
        }
    }

    public BulkResponse trigger() throws InputValueException {
        return save();
    }

    public BulkResponse save() throws InputValueException {
        validateSubscribers();
        if (this.__invalidRecords.size() > 0) {
            JSONObject chResponse = BulkResponse.invalidRecordsChunkResponse(this.__invalidRecords);
            this.response.mergeChunkResponse(chResponse);
        }
        if (this.__pendingRecords.size() > 0) {
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
