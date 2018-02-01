package fr.noopy.graylog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cyrille on 31/01/18.
 */

public class Connection {
    public String name;
    public String token;
    public URL url;

    public static final String PREFS_CONNECTION = "connection";

    public Connection() {
        this.name = "default";
        this.token = "";
        try {
            this.url = new URL("");
        } catch (MalformedURLException e) {

        }
    }

    public Connection(String name, String token, String urlStr) throws MalformedURLException {
        this.name = name;
        this.token = token;
        this.url = new URL(urlStr);
    }

    public Connection(JSONObject data) throws MalformedURLException, JSONException {
        this(data.getString("name"), data.getString("token"), data.getString("url"));
    }

    public Connection(String data) throws MalformedURLException, JSONException {
        this(new JSONObject(data));
    }

    public String toString() {
        JSONObject result = new JSONObject();
        try {
            result.put("name", name);
            result.put("token", token);
            result.put("url", url.toString());
            return result.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public void saveAsPreference(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_CONNECTION, this.toString());
        editor.commit();
        Log.i("Preferences", "saving connection " + getUrl());
    }

    public Bundle saveAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("connection", toString());
        return bundle;
    }

    public static Connection fromPreference(SharedPreferences settings) {
        try {
            return new Connection(settings.getString(PREFS_CONNECTION, ""));
        } catch (JSONException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static Connection fromBundle(Bundle bundle) {
        try {
            return new Connection(bundle.getString("connection", ""));
        } catch (JSONException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public Uri.Builder builder() {
        Uri.Builder builder = new Uri.Builder();
        return builder.scheme(url.getProtocol())
                .authority(url.getHost())
                .appendPath(url.getPath());
    }

    public String streamsUrl() {
        return builder()
                .appendPath("streams").toString();
    }

    public String relativeSearchUrl() {
        return builder()
                .appendPath("search")
                .appendPath("universal")
                .appendPath("relative")
                .toString();
    }

    public AsyncHttpClient client() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(token, "token");
        return client;
    }

    public boolean isConsistent() {
        return (url != null) && !token.isEmpty();
    }

    public void setToken(String data) {
        token = data;
    }

    public void setUrl(String data) throws MalformedURLException {
        url = new URL(data);
    }

    public String getUrl() {
        if (url != null) {
            return url.toString();
        }
        return "";
    }

    // Get session ID
    // curl 'https://gra2.logs.ovh.com/api/system/sessions' -H 'Content-Type: application/json' -H 'Accept: application/json'  --data-binary '{"username":"my_username","password":"my_password","host":url.getHost()}'
    // Response
    // {"valid_until":"2018-02-01T15:27:26.162+0000","session_id":"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}
}
