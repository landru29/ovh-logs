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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import fr.noopy.graylog.log.Message;
import fr.noopy.graylog.task.TaskReport;

/**
 * Created by cyrille on 31/01/18.
 */

public class Connection {
    public String name;
    public String token;
    public URL url;
    public String tokenType;
    public StreamDescriptor currentStream;

    public static final String PREFS_CONNECTION = "connection";

    public Connection() {
        this.name = "default";
        this.token = "";
        this.tokenType = "";
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
        this(data.getString("name"), data.has("token") ? data.getString("token") : "", data.getString("url"));
        if (data.has("tokenType")) {
            tokenType = data.getString("tokenType");
        } else if (!token.isEmpty() ) {
            tokenType = "token";
        }
        if (data.has("stream")) {
            currentStream = new StreamDescriptor(data.getString("stream"));
        }
    }

    public Connection(String data) throws MalformedURLException, JSONException {
        this(new JSONObject(data));
    }


    public String toString(boolean all) {
        JSONObject result = new JSONObject();
        try {
            result.put("name", name);
            if (tokenType == "token" || all) {
                result.put("token", token);
            }
            if (all) {
                result.put("tokenType", tokenType);
            }
            result.put("url", url.toString());
            if (currentStream != null) {
                result.put("stream", currentStream.stringify());
            }
            return result.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public String toString() {
        return toString(false);
    }

    public void saveAsPreference(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        String dataStr = toString(false);
        editor.putString(PREFS_CONNECTION, dataStr);
        editor.commit();
        Log.i("Preferences", "saving connection " + getUrl());
    }

    public static Connection fromPreference(SharedPreferences settings) {
        try {
            return new Connection(settings.getString(PREFS_CONNECTION, ""));
        } catch (JSONException e) {
            Log.i("JSONException", e.toString());
            return null;
        } catch (MalformedURLException e) {
            Log.i("MalformedURLException", e.toString());
            return null;
        }
    }

    public Bundle saveAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("connection", toString(true));
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
        List<String> pathElements = Arrays.asList(url.getPath().replaceAll("^/+", "").split("/"));
        Uri.Builder builder = new Uri.Builder();
        Uri.Builder builderResult =  builder.scheme(url.getProtocol())
                .authority(url.getHost());
        for (int i=0; i< pathElements.size(); i++) {
            builderResult = builderResult.appendPath(pathElements.get(i));
        }
        return builderResult;
    }

    public String streamsUrl() {
        return builder()
                .appendPath("streams")
                .toString();
    }

    public String loginUrl() {
        return builder()
                .appendPath("system")
                .appendPath("sessions")
                .toString();
    }

    public String relativeSearchUrl() {
        return builder()
                .appendPath("search")
                .appendPath("universal")
                .appendPath("relative")
                .toString();
    }

    public AsyncHttpClient client(boolean withAuth) {
        AsyncHttpClient client = new AsyncHttpClient(true,80,443);
        if (withAuth) {
            client.setBasicAuth(token, tokenType);
            Log.i("basic", token + ":" + tokenType);
        }
        return client;
    }

    public AsyncHttpClient client() {
        return client(true);
    }

    public boolean isConsistent() {
        return (url != null) && !token.isEmpty();
    }

    public void setToken(String data, String dataType) {
        token = data;
        tokenType = dataType;
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
            task.onFailure("Connection is unconsistent");
            task.onComplete();
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
                Log.i("graylog", "failure: " + resp);
                task.onFailure(resp);
                task.onComplete();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject resp) {
                Log.i("graylog", "failure: " + resp);
                task.onFailure(resp != null ? resp.toString() : "unknown");
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
            task.onFailure("Connection is unconsistent");
            task.onComplete();
            return;
        }
        if (this.currentStream == null) {
            Log.i("OMG", "No stream ID");
            task.onFailure("No stream ID");
            task.onComplete();
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
                task.onFailure(resp != null ? resp.toString() : "unknown");
                task.onComplete();
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }

    public void login(String username, String password, final TaskReport<String> task) {
        if (url == null) {
            Log.i("OMG", "Connection is unconsistent");
            task.onFailure("Connection is unconsistent");
            task.onComplete();
            return;
        }
        final String urlStr = this.loginUrl();
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("username", username);
            jsonParams.put("password", password);
            jsonParams.put("host", url.getHost());
            AsyncHttpClient client = this.client(false);
            //client.addHeader("Host", url.getHost());
            client.addHeader("Accept", "application/json");
            client.post(null, urlStr, new StringEntity(jsonParams.toString()), "application/json", new JsonHttpResponseHandler() {

                @Override
                public void onStart() {
                    Log.i("graylog", "starting " + urlStr);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response.has("session_id")) {
                        try {
                            task.onSuccess(response.getString("session_id"));
                        } catch (JSONException e) {
                            task.onFailure(e.toString());
                        }
                    } else {
                        task.onFailure("No session id");
                    }

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
                    Log.i("graylog", "failure: " + resp);
                    task.onFailure(resp != null ? resp.toString() : "unknown");
                    task.onComplete();
                }

                @Override
                public void onRetry(int retryNo) {
                    Log.i("graylog", "retry");
                }
            });
        } catch (JSONException e) {
            task.onFailure(e.toString());
            task.onComplete();
        } catch (UnsupportedEncodingException e) {
            task.onFailure(e.toString());
            task.onComplete();
        }
    }

    // Get session ID
    // curl 'https://gra2.logs.ovh.com/api/system/sessions' -H 'Content-Type: application/json' -H 'Accept: application/json'  --data-binary '{"username":"my_username","password":"my_password","host":url.getHost()}'
    // Response
    // {"valid_until":"2018-02-01T15:27:26.162+0000","session_id":"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}
}
