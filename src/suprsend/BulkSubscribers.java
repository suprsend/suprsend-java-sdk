package suprsend;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

class BulkSubscribers{
    private static final Logger logger = Logger.getLogger(BulkSubscribers.class.getName());
    private final Suprsend config;
    private ArrayList<Subscriber> _subscribers = new ArrayList<Subscriber>();
    BulkSubscribers(Suprsend config){
        this.config = config;
    }
}