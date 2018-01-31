package fr.noopy.graylog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

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

    public String stringify() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", id);
            result.put("title", title);
            return result.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public void saveAsPreference(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_STREAM, this.stringify());
        editor.commit();
    }

    public static StreamDescriptor fromPreference(SharedPreferences settings) {
        try {
            return new StreamDescriptor(settings.getString(PREFS_STREAM, ""));
        } catch (JSONException e) {
            return null;
        }
    }

    public static StreamDescriptor fromBundle(Bundle bundle) {
        try {
            return new StreamDescriptor(bundle.getString("stream", ""));
        } catch (JSONException e) {
            return null;
        }
    }

    public Bundle saveAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("stream", toString());
        return bundle;
    }

    @Override
    public String toString() {
        return title;
    }
}
