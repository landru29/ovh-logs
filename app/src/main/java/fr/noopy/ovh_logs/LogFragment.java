package fr.noopy.ovh_logs;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by cmeichel on 29/01/18.
 */

public class LogFragment extends Fragment {

    private Context currentContext;
    private SharedPreferences settings;
    private EditText tokenEdit;
    private Spinner spinner;
    private Button scanButton;
    private View rootView;
    private List<StreamDescriptor> streams = new ArrayList<StreamDescriptor>();
    private StreamDescriptor currentStream;

    public static final String PREFS_NAME = "graylog";
    public static final String PREFS_TOKEN = "token";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.log_fragment, container, false);

        currentContext = rootView.getContext();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        scanButton = rootView.findViewById(R.id.scanStreams);
        tokenEdit = ((Activity) currentContext).findViewById(R.id.readToken);
        spinner = ((Activity) currentContext).findViewById(R.id.stream);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREFS_TOKEN, ((EditText)((Activity) currentContext).findViewById(R.id.readToken)).getText().toString());
                editor.commit();
                readTokens();
                listStreams();
                //readLogs();
            }
        });

        settings = currentContext.getSharedPreferences(PREFS_NAME, 0);

        String token = settings.getString(PREFS_TOKEN, "");
        if (token!= null) {
            tokenEdit.setText(token);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentStream = streams.get(position);
                Log.i("Select stream", currentStream.title);
                readLogs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void readTokens() {
        Stream.token = tokenEdit.getText().toString();
    }

    private void listStreams() {
        scanButton.setEnabled(false);
        AsyncHttpClient client = Stream.client();
        streams.clear();
        client.get(currentContext, Stream.streamsUrl(), new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting " + Stream.streamsUrl());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray streamsJson = response.getJSONArray("streams");
                    for (int i=0; i<streamsJson.length(); i++) {
                        streams.add(new StreamDescriptor(streamsJson.getJSONObject(i)));
                    }
                } catch (JSONException e) {

                }

                if (streams.size()>0) {
                    ArrayAdapter sailAdapter = new ArrayAdapter<StreamDescriptor>(currentContext, R.layout.spinner, streams);
                    spinner.setAdapter(sailAdapter);
                }

                scanButton.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String resp, Throwable e) {
                Log.i("graylog", "failure");
                scanButton.setEnabled(true);
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }

    private void readLogs () {
        if (currentStream == null) {
            return;
        }
        AsyncHttpClient client = Stream.client();
        RequestParams request = new RequestParams();
        request.put("fields", "title,msg,timestamp");
        request.put("filter", "streams:" + currentStream.id);
        request.put("query", "*");
        request.put("limit", 150);
        request.put("seconds", 300);
        request.put("sort", "timestamp:desc");
        Log.i("request", request.toString());
        client.get(currentContext, Stream.relativeSearchUrl(), request, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting " + Stream.relativeSearchUrl());
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
                Log.i("graylog",  "" + messages.size());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String resp, Throwable e) {
                Log.i("graylog", "failure: " + resp);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject resp) {
                Log.i("graylog", "failure: " + resp.toString());
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }
}
