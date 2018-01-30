package fr.noopy.graylog;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by cmeichel on 30/01/18.
 */

public class LogFragment extends Fragment {

    private Context currentContext;
    private View rootView;
    private String token;
    private String streamId;
    private RecyclerView recyclerView;
    private List<Message> messages = new ArrayList<Message>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.log_fragment, container, false);

        currentContext = rootView.getContext();

        Bundle bundle = this.getArguments();
        readDataFromBundle(bundle);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(currentContext));

        readLogs();

        return rootView;
    }

    public void readDataFromBundle(Bundle bundle) {
        if (bundle != null) {
            token = bundle.getString("token");
            streamId = bundle.getString("stream");
        } else {
            Log.i("Data", "No bundle");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void readLogs () {
        if (streamId == null) {
            return;
        }
        AsyncHttpClient client = Stream.client();
        RequestParams request = new RequestParams();
        request.put("fields", "title,msg,timestamp");
        request.put("filter", "streams:" + streamId);
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
                try {
                    JSONArray msgList = response.getJSONArray("messages");
                    for (int i=0; i< msgList.length(); i++) {
                        messages.add(new Message(msgList.getJSONObject(i)));
                    }
                } catch (JSONException e) {

                } catch (ParseException e) {

                }
                recyclerView.setAdapter(new LogAdapter(messages));
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
