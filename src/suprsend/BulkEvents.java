package suprsend;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkEvents {
    private static final Logger logger = Logger.getLogger(BulkEvents.class.getName());

    private Suprsend config;

    private List<Event> __events;
    private List<JSONObject> __pendingRecords;
    
    private List<BulkEventsChunk> chunks;
    private BulkResponse response;

    private List<JSONObject> __invalidRecords;

    BulkEvents(Suprsend config) {
        this.config = config;
        this.__events = new ArrayList<Event>();
        this.__pendingRecords = new ArrayList<>();
        // invalid_record json: {"record": event-json, "error": error_str, "code": 500}
        this.__invalidRecords = new ArrayList<>();
        this.chunks = new ArrayList<>();
        this.response = new BulkResponse();
    }

    private void validateEvents() {
        for (Event e : this.__events) {
            try {
                // {"event": validatedEvent, "apparent_size": apparentSize}
                JSONObject evJson = e.getFinalJson(this.config, true);   
                this.__pendingRecords.add(evJson); 
            } catch (Exception ex) {
                // invalid_record json: {"record": event-json, "error": error_str, "code": 500}
                JSONObject invRec = Utils.invalidRecordJson(e.asJson(), ex);
                this.__invalidRecords.add(invRec);
            }
        }
    }

    private void chunkify(int startIdx) throws InputValueException {
        BulkEventsChunk currChunk = new BulkEventsChunk(this.config);
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

    public void append(Event... events) {
        if (events.length == 0) {
            return;
        }
        for (Event obj : events) {
            if (obj == null) {
                continue;
            }
            // TODO: deep copy
            this.__events.add(obj);
        }
    }

    public BulkResponse trigger() throws InputValueException {
        validateEvents();
        if (this.__invalidRecords.size() > 0) {
            JSONObject chResponse = BulkResponse.invalidRecordsChunkResponse(this.__invalidRecords);
            this.response.mergeChunkResponse(chResponse);
        }
        if (this.__pendingRecords.size() > 0) {
            chunkify(0);
            for (int cIdx = 0; cIdx < this.chunks.size(); cIdx++) {
                BulkEventsChunk chunk = this.chunks.get(cIdx);
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
        // ----
        return this.response;
    }

}
