package fr.noopy.graylog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cyrille on 29/01/18.
 */

public class StreamDescriptor {

    public String id;
    public String title;

    StreamDescriptor(JSONObject desc) throws JSONException {
        id = desc.getString("id");
        title = desc.getString("description");

    }

    @Override
    public String toString() {
        return title;
    }
}
