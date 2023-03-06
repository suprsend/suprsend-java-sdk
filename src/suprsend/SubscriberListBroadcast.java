package suprsend;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.UUID;

public class SubscriberListBroadcast {

    private JSONObject body;
    private String idempotencyKey;
    private String brandId;

    public SubscriberListBroadcast(JSONObject body) throws SuprsendException{
        this(body, null, null);
    }

    public SubscriberListBroadcast(JSONObject body, String idempotencyKey) throws SuprsendException{
        this(body, idempotencyKey, null);
    }

    public SubscriberListBroadcast(JSONObject body, String idempotencyKey, String brandId) throws SuprsendException{
        if (body == null) {
            throw new SuprsendException("broadcast body must be a passed");
        }
        this.body = body;
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            this.idempotencyKey = idempotencyKey.trim();
        }
        if (brandId != null && !brandId.trim().isEmpty()) {
            this.brandId = brandId.trim();
        }
    }

    JSONObject getFinalJson() throws SuprsendException, UnsupportedEncodingException {
        this.body.put("$insert_id", UUID.randomUUID().toString());
        this.body.put("$time", Instant.now().getEpochSecond() * 1000);
        if (null != this.idempotencyKey) {
			this.body.put("$idempotency_key", this.idempotencyKey);
		}
		if (null != this.brandId) {
			this.body.put("brand_id", this.brandId);
		}
        JSONObject validatedBody = Utils.validateListBroadcastBodySchema(this.body);
        this.body = validatedBody;

        int apparentSize = Utils.getApparentListBroadcastBodySize(this.body);
        if (apparentSize > Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES) {
            throw new SuprsendException(
                    String.format("SubscriberListBroadcast body too big - %d Bytes, must not cross %s",
                            apparentSize, Constants.SINGLE_EVENT_MAX_APPARENT_SIZE_IN_BYTES_READABLE));
        }
        return new JSONObject().put("event", this.body).put("apparent_size", apparentSize);
    }
}
