package fr.noopy.graylog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cyrille on 29/01/18.
 */

public class Message {

    public String id;
    public Date timestamp;

    Message(JSONObject data) throws JSONException, ParseException {
        JSONObject msg = data.getJSONObject("message");
        id = msg.getString("_id");
        String dateStr = msg.getString("timestamp");
        SimpleDateFormat formater=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //2018-01-29T20:53:42.000Z
        timestamp = formater.parse(dateStr);
    }
}
