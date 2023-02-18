package suprsend;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SubscriberListBroadcast {

    private JSONObject body;
    private String idempotencyKey;
    private String brandId;

    public SubscriberListBroadcast(JSONObject body,String idempotencyKey, String brandId){
        this.body = body;
        this.idempotencyKey = idempotencyKey;
        this.brandId = brandId;
    }

    JSONObject getFinalJson() throws SuprsendException, UnsupportedEncodingException {
        body.put("$insert_id", UUID.randomUUID().toString());
        body.put("$time", Instant.now().getEpochSecond() * 1000);
        if (!idempotencyKey.isEmpty()) {
            body.put("$idempotency_key", idempotencyKey);
        }
        if (!brandId.isEmpty()) {
            body.put("brand_id", brandId);
        }
        body = validateListBroadcastBodySchema();
        int apparentSize = Utils.getApparentBodySize(body);
        if (apparentSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
            throw new SuprsendException("SubscriberListBroadcast body too big - " + apparentSize + " Bytes, " +
                    "must not cross " + Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE);
        }
        body.put("apparent_size", apparentSize);
        return body;
    }

    private JSONObject validateListBroadcastBodySchema() throws SuprsendException {
        Object data = body.get("data");
        if (data == null) {
            body.put("data", new JSONObject());
        }
        if (!(data instanceof JSONObject)) {
            throw new SuprsendException("data must be a dictionary");
        }
        return Utils.validateListBroadcastBodySchema(this.body);
    }
}

