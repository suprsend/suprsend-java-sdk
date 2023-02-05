package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BulkSubscribers {
    private static final Logger logger = Logger.getLogger(BulkSubscribers.class.getName());
    private final Suprsend config;
    private ArrayList<Subscriber> _subscribers = new ArrayList<Subscriber>();
    private JSONArray subscribersJA = new JSONArray();
    private List<BulkSubscribersChunk> chunks = new ArrayList<>();
    private BulkSubscribersChunk currChunk;

    private BulkResponse bulkResponse = new BulkResponse();

    BulkSubscribers(Suprsend config) {
        this.config = config;
    }

    private void validateSubscribers() throws SuprsendException, UnsupportedEncodingException {
        if (_subscribers.isEmpty()) {
            throw new SuprsendException("events list is empty in bulk request");
        }
        for (Subscriber s : _subscribers) {
            ArrayList<String> warnings = s.validateBody(true);
            if (!warnings.isEmpty())
                bulkResponse.warnings.addAll(warnings);
            List<JSONObject> allEvents = s.getEvents();
            for (JSONObject ev : allEvents) {
                JSONObject result = s.validateEventSize(ev);
                subscribersJA.put(result);
            }
        }
    }

    private void chunkify(int start) throws SuprsendException {
        currChunk = new BulkSubscribersChunk(config);
        chunks.add(currChunk);
        // TODO - test chunk pagination
        for (int i = 0; i < subscribersJA.length(); i++) {
            JSONObject event = subscribersJA.getJSONObject(i);
            int apparentSize = event.getInt("apparent_size");
            boolean isAdded = currChunk.tryToAddIntoChunk(event.getJSONObject("event"), apparentSize);
            if (!isAdded) {
                chunkify(start + i);
                break;
            }
        }
    }

    public void append(Subscriber obj) throws SuprsendException {
        ArrayList<Subscriber> objList = new ArrayList<Subscriber>();
        objList.add(obj);
        append(objList);
    }

    public void append(List<Subscriber> objList) throws SuprsendException {
        if (objList.isEmpty()) {
            throw new SuprsendException("users list empty. must pass one or more users");
        }
        for (Subscriber obj : objList) {
            if (obj == null) {
                throw new SuprsendException("null/empty element found in bulk instance");
            }
            // TODO - test Clone objList
            _subscribers.add(obj);
        }
    }

    public BulkResponse trigger() throws SuprsendException, UnsupportedEncodingException {
        return save();
    }

    public BulkResponse save() throws SuprsendException, UnsupportedEncodingException {
        validateSubscribers();
        chunkify(0);
        for (int i = 0; i < chunks.size(); i++) {
            BulkSubscribersChunk chunk = chunks.get(i);
            logger.log(Level.INFO, "triggering api call for chunk:" + i);
            chunk.trigger();
            bulkResponse.mergeChunkResponse(chunk.response);
        }
        return bulkResponse;

    }

}