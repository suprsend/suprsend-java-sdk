package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkEvents {
    private static final Logger logger = Logger.getLogger(BulkEvents.class.getName());
    private Suprsend config;

    private ArrayList<Event> _events = new ArrayList<Event>();
    private JSONArray eventsJA = new JSONArray();
    private List<BulkEventsChunk> chunks = new ArrayList<>();
    private BulkEventsChunk currChunk;

    private BulkResponse bulkResponse = new BulkResponse();

    BulkEvents(Suprsend config) {
        this.config = config;
    }

    private void validateEvents() throws SuprsendException, UnsupportedEncodingException {
        if (_events.isEmpty()) {
            throw new SuprsendException("events list is empty in bulk request");
        }
        for (Event e : _events) {
            JSONObject evJson = e.getFinalJson(config, true);
            eventsJA.put(evJson);
        }
    }

    private void chunkify(int start) throws SuprsendException {
        currChunk = new BulkEventsChunk(config);
        chunks.add(currChunk);
        for (int i = 0; i < eventsJA.length(); i++) {
            JSONObject event = eventsJA.getJSONObject(i);
            int apparentSize = event.getInt("apparent_size");
            boolean isAdded = currChunk.tryToAddIntoChunk(event.getJSONObject("event"), apparentSize);
            if (!isAdded) {
                chunkify(start + i);
                break;
            }
        }
    }

    public void append(List<Event> objList) throws SuprsendException {
        if (objList.isEmpty()) {
            throw new SuprsendException("events list empty. must pass one or more events");
        }
        for (Event obj : objList) {
            if (obj == null) {
                throw new SuprsendException("null/empty element found in bulk instance");
            }
            // TODO - test Clone events
            _events.add(obj);
        }
    }

    public BulkResponse trigger() throws SuprsendException, UnsupportedEncodingException {
        validateEvents();
        chunkify(0);
        for (int i = 0; i < chunks.size(); i++) {
            BulkEventsChunk chunk = chunks.get(i);
            logger.log(Level.INFO, "triggering api call for chunk:" + i);
            chunk.trigger();
            bulkResponse.mergeChunkResponse(chunk.response);
        }
        return bulkResponse;
    }

}


