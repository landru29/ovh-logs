package fr.noopy.graylog;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
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

public class StreamFragment extends Fragment {

    private Context currentContext;
    private SharedPreferences settings;
    private EditText tokenEdit;
    private Spinner spinner;
    private Button scanButton;
    private Button selectStreams;
    private View rootView;
    private List<StreamDescriptor> streams = new ArrayList<StreamDescriptor>();
    private StreamDescriptor currentStream;
    private String graylogUrl = "https://gra2.logs.ovh.com/api";

    public static final String PREFS_NAME = "graylog";
    public static final String PREFS_TOKEN = "token";
    public static final String GRAYLOG_API_URL = "graylog_url";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.stream_fragment, container, false);

        currentContext = rootView.getContext();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        scanButton = rootView.findViewById(R.id.scanStreams);
        selectStreams = rootView.findViewById(R.id.selectStreams);
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
            }
        });

        selectStreams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("token", tokenEdit.getText().toString());
                args.putString("stream", currentStream.id);
                ((MainActivity)getActivity()).gotoLogs(args);
            }
        });

        readSettings();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentStream = streams.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (readTokens()) {
            listStreams();
        }

    }

    private void readSettings() {
        settings = currentContext.getSharedPreferences(PREFS_NAME, 0);

        String token = settings.getString(PREFS_TOKEN, "");
        if (token!= null) {
            tokenEdit.setText(token);
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(GRAYLOG_API_URL, graylogUrl);
        editor.commit();
    }

    private Boolean readTokens() {
        Stream.token = tokenEdit.getText().toString();
        return Stream.token.length()>0;
    }

    private void listStreams() {
        scanButton.setEnabled(false);
        AsyncHttpClient client = Stream.client();
        streams.clear();
        client.get(currentContext, Stream.streamsUrl(graylogUrl), new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting " + Stream.streamsUrl(graylogUrl));
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


}
