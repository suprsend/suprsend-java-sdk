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

    public void append(Event event) throws SuprsendException {
        ArrayList<Event> events = new ArrayList<Event>();
        events.add(event);
        append(events);
    }
    public void append(List<Event> events) throws SuprsendException {
        if (events.isEmpty()) {
            System.out.println("events list empty. must pass one or more events");
            return;
        }
        for (Event e : events) {
            if (e == null) {
                throw new SuprsendException("null/empty element found in bulk instance");
            }
            //Todo : niks Clone events
            _events.addAll(events);
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


