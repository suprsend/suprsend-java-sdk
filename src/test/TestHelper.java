package test;

import suprsend.Suprsend;
import suprsend.SuprsendException;

public class TestHelper {

    public static Suprsend getClientInstance() throws SuprsendException {
        String apiKey = "apikey";
        String apiSecret = "apiSecret";
        return new Suprsend(apiKey, apiSecret);
    }

}
