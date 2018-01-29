package fr.noopy.ovh_logs;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by cmeichel on 29/01/18.
 */

public class LogFragment extends Fragment {

    private Context currentContext;
    private String streamName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.log_fragment, container, false);

        currentContext = rootView.getContext();

        Button okButton = rootView.findViewById(R.id.readLogs);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readTokens();
                listStreams();
                readLogs();
            }
        });

        return rootView;
    }


    private void readTokens() {
        Stream.token = ((EditText)((Activity) currentContext).findViewById(R.id.readToken)).getText().toString();
        streamName = ((EditText)((Activity) currentContext).findViewById(R.id.stream)).getText().toString();
    }

    private void listStreams() {
        AsyncHttpClient client = Stream.client();
        client.get(currentContext, Stream.streamsUrl(), new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("graylog", "ok");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String text, Throwable e) {
                Log.i("graylog", "failure");
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }

    private void readLogs () {
        AsyncHttpClient client = Stream.client();
        RequestParams request = new RequestParams();
        request.put("fields", "title,msg,timestamp");
        request.put("streams", streamName);
        request.put("query", "*");
        request.put("limit", 150);
        request.put("seconds", 300);
        request.put("sort", "timestamp:desc");
        client.get(currentContext, Stream.relativeSearchUrl(), request, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("graylog", "ok");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String text, Throwable e) {
                Log.i("graylog", "failure");
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i("graylog", "retry");
            }
        });
    }
}
