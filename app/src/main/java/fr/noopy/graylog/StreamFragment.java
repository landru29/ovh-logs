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

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by cmeichel on 29/01/18.
 */

public class StreamFragment extends Fragment {

    private Context currentContext;

    private EditText urlEdit;
    private EditText tokenEdit;
    private Spinner spinner;
    private Button scanButton;
    private Button selectStreams;
    private View rootView;
    private List<StreamDescriptor> streams = new ArrayList<StreamDescriptor>();
    private StreamDescriptor currentStream;
    private Connection currentConnection;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.stream_fragment, container, false);

        currentContext = rootView.getContext();

        Bundle bundle = this.getArguments();
        readDataFromBundle(bundle);

        return rootView;
    }

    private void readDataFromBundle(Bundle bundle) {
        if (bundle != null) {
            currentConnection = Connection.fromBundle(bundle);
        } else {
            Log.i("Data", "No bundle");
        }

        if (currentConnection == null) {
            Log.i("Data", "No connection in the bundle");
            currentConnection = new Connection();
        }

        if (tokenEdit != null) {
            tokenEdit.setText(currentConnection.token);
        }
        if (urlEdit != null) {
            urlEdit.setText(currentConnection.getUrl());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        scanButton = rootView.findViewById(R.id.scanStreams);
        selectStreams = rootView.findViewById(R.id.selectStreams);
        tokenEdit = ((Activity) currentContext).findViewById(R.id.readToken);
        urlEdit = ((Activity) currentContext).findViewById(R.id.url);
        spinner = ((Activity) currentContext).findViewById(R.id.stream);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    currentConnection.setUrl(urlEdit.getText().toString());
                    currentConnection.setToken(tokenEdit.getText().toString());
                    currentConnection.saveAsPreference(((MainActivity)getActivity()).getSettings());
                    listStreams();
                } catch(MalformedURLException e) {
                    Log.w("User Entry", e.toString());
                }

            }
        });

        selectStreams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentConnection.saveAsPreference(((MainActivity)getActivity()).getSettings());
                currentStream.saveAsPreference(((MainActivity)getActivity()).getSettings());
                Bundle args = currentConnection.saveAsBundle();
                args.putString("stream", currentStream.id);
                ((MainActivity)getActivity()).gotoLogs(args);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentStream = streams.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (currentConnection != null) {
            tokenEdit.setText(currentConnection.token);
            urlEdit.setText(currentConnection.getUrl());

            if (currentConnection.isConsistent()) {
                listStreams();
            }
        }



    }

    private void listStreams() {
        if (this.currentConnection == null || !this.currentConnection.isConsistent()) {
            Log.i("OMG", "Connection is unconsistent");
            return;
        }
        scanButton.setEnabled(false);
        AsyncHttpClient client = currentConnection.client();
        streams.clear();
        client.get(currentContext, currentConnection.streamsUrl(), new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("graylog", "starting " + currentConnection.streamsUrl());
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
