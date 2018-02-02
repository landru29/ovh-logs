package fr.noopy.graylog.api;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by cyrille on 29/01/18.
 */

public class StreamDescriptor {

    public String id;
    public String description;

    public static final String PREFS_STREAM = "stream";

    public StreamDescriptor(JSONObject desc) throws JSONException {
        id = desc.getString("id");
        description = desc.getString("description");

    }

    public StreamDescriptor(String data) throws  JSONException {
        this(new JSONObject(data));
    }

    public String stringify() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", id);
            result.put("description", description);
            return result.toString();
        } catch (JSONException e) {
            return "";
        }
    }


    @Override
    public String toString() {
        return description;
    }
}
