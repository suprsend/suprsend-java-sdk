package test;

import org.json.JSONObject;
import suprsend.Suprsend;
import suprsend.SuprsendException;

public class TestHelper {

    private String workspaceKey = "workspaceKey";
    private String workspaceSecret = "workspaceKey";

    public static Suprsend getInstance() throws SuprsendException {
        JSONObject kwargs = new JSONObject();
        return new Suprsend(workspaceKey, workspaceSecret, "", true, kwargs);
    }

}
