package fr.noopy.graylog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cyrille on 29/01/18.
 */

public class Message {

    public String id;
    public Date timestamp;
    public Map<String, String> map = new HashMap<String, String>();

    Message(JSONObject data) throws JSONException, ParseException {
        JSONObject msg = data.getJSONObject("message");
        id = msg.getString("_id");
        String dateStr = msg.getString("timestamp");
        SimpleDateFormat formater=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //2018-01-29T20:53:42.000Z
        timestamp = formater.parse(dateStr);

        Iterator<String> keysItr = msg.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            if (key != "timestamp" && key != "_id") {
                Object value = msg.get(key);
                if (value instanceof String) {
                    map.put(key, (String) value);
                }
                if (value instanceof Number) {
                    map.put(key, "" + (Number) value);
                }
            }
        }
    }

    public String get(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return "";
    }

    public List<String> keys() {
        return new ArrayList<String>(map.keySet());
    }
}
