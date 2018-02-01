package fr.noopy.graylog.api;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by cyrille on 29/01/18.
 */

public class StreamDescriptor {

    public String id;
    public String title;

    public static final String PREFS_STREAM = "stream";

    public StreamDescriptor(JSONObject desc) throws JSONException {
        id = desc.getString("id");
        title = desc.getString("description");

    }

    public StreamDescriptor(String data) throws  JSONException {
        this(new JSONObject(data));
    }


    @Override
    public String toString() {
        return title;
    }
}
