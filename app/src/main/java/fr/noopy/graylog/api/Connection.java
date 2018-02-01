package fr.noopy.graylog.api;


import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import fr.noopy.graylog.log.Message;
import fr.noopy.graylog.task.TaskReport;

/**
 * Created by cyrille on 31/01/18.
 */

public class Connection {
    public String name;
    public String token;
    public URL url;
    public StreamDescriptor currentStream;

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

    public static Connection fromPreference(SharedPreferences settings) {
        try {
            return new Connection(settings.getString(PREFS_CONNECTION, ""));
        } catch (JSONException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public Bundle saveAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("connection", toString());
        return bundle;
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

    private Uri.Builder builder() {
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

    public void listStreams(final TaskReport<List<StreamDescriptor>> task) {
        if (!this.isConsistent()) {
            Log.i("OMG", "Connection is unconsistent");
            return;
        }
        final String urlStr = this.streamsUrl();
        this.client().get(urlStr, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting " + urlStr);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                List streams = new ArrayList<StreamDescriptor>();
                try {
                    JSONArray streamsJson = response.getJSONArray("streams");
                    for (int i=0; i<streamsJson.length(); i++) {
                        streams.add(new StreamDescriptor(streamsJson.getJSONObject(i)));
                    }
                } catch (JSONException e) {

                }

                task.onSuccess(streams);
                task.onComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String resp, Throwable e) {
                Log.i("graylog", "failure");
                task.onFailure(resp);
                task.onComplete();
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }

    public void readLogs (String filter, final TaskReport<List<Message>> task) {
        if ( !this.isConsistent()) {
            Log.i("OMG", "Connection is unconsistent");
            return;
        }
        if (this.currentStream == null) {
            Log.i("OMG", "No stream ID");
            return;
        }
        RequestParams request = new RequestParams();
        request.put("fields", "title,msg,timestamp");
        request.put("filter", "streams:" + this.currentStream.id);
        request.put("query", filter);
        request.put("limit", 150);
        request.put("seconds", 300);
        request.put("sort", "timestamp:desc");
        Log.i("request", request.toString());

        final String urlStr = relativeSearchUrl();
        this.client().get(urlStr, request, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting " + urlStr);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                List<Message> messages = new ArrayList<Message>();
                try {
                    JSONArray msgList = response.getJSONArray("messages");
                    for (int i=0; i< msgList.length(); i++) {
                        messages.add(new Message(msgList.getJSONObject(i)));
                    }
                } catch (JSONException e) {

                } catch (ParseException e) {

                }
                task.onSuccess(messages);
                task.onComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String resp, Throwable e) {
                Log.i("graylog", "failure: " + resp);
                task.onFailure(resp);
                task.onComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject resp) {
                Log.i("graylog", "failure: " + resp.toString());
                task.onFailure(resp.toString());
                task.onComplete();
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }

    // Get session ID
    // curl 'https://gra2.logs.ovh.com/api/system/sessions' -H 'Content-Type: application/json' -H 'Accept: application/json'  --data-binary '{"username":"my_username","password":"my_password","host":url.getHost()}'
    // Response
    // {"valid_until":"2018-02-01T15:27:26.162+0000","session_id":"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}
}
