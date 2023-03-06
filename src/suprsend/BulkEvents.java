package suprsend;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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

    BulkEvents(Suprsend config) {
        this.config = config;
        this.__events = new ArrayList<Event>();
        this.__pendingRecords = new ArrayList<JSONObject>();
        this.chunks = new ArrayList<>();
        this.response = new BulkResponse();
    }

    private void validateEvents() throws SuprsendException, UnsupportedEncodingException {
        if (this.__events.isEmpty()) {
            throw new SuprsendException("events list is empty in bulk request");
        }
        for (Event e : this.__events) {
            // {"event", validatedEvent, "apparent_size", apparentSize}
            JSONObject evJson = e.getFinalJson(this.config, true);
            this.__pendingRecords.add(evJson);
        }
    }

    private void chunkify(int startIdx) throws SuprsendException {
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

    public void append(Event... events) throws SuprsendException {
        if (events.length == 0) {
            throw new SuprsendException("events list empty. must pass one or more events");
        }
        for (Event obj : events) {
            if (obj == null) {
                continue;
            }
            // TODO: deep copy
            this.__events.add(obj);
        }
    }

    public BulkResponse trigger() throws SuprsendException, UnsupportedEncodingException {
        validateEvents();
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
        return this.response;
    }

}
