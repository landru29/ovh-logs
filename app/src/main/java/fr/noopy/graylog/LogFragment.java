package fr.noopy.graylog;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

import fr.noopy.graylog.api.Connection;
import fr.noopy.graylog.log.LogAdapter;
import fr.noopy.graylog.log.Message;
import fr.noopy.graylog.task.TaskReport;

/**
 * Created by cmeichel on 30/01/18.
 */

public class LogFragment extends Fragment {

    private Context currentContext;
    private View rootView;
    private RecyclerView recyclerView;
    private List<Message> messages = new ArrayList<Message>();
    private EditText filter;
    private Button launchFilter;
    private Connection currentConnection;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.log_fragment, container, false);

        currentContext = rootView.getContext();

        filter = rootView.findViewById(R.id.filter);
        launchFilter = rootView.findViewById(R.id.launchFilter);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(currentContext));
        recyclerView.setAdapter(new LogAdapter(messages));


        Bundle bundle = this.getArguments();
        readDataFromBundle(bundle);

        launchFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readLogs();
            }
        });

        return rootView;
    }

    public void readDataFromBundle(Bundle bundle) {
        if (bundle != null) {
            currentConnection = Connection.fromBundle(bundle);
        } else {
            Log.i("Data", "No bundle");
        }

        if (currentConnection == null) {
            Log.i("Data", "No connection in the bundle");
            currentConnection = new Connection();
        }

        Log.i("url", currentConnection.getUrl());
    }

    @Override
    public void onResume() {
        super.onResume();
        readLogs();

    }

    private String getFilter() {
        return filter.getText().toString();
    }

    private void readLogs () {
        if (currentConnection!= null) {
            launchFilter.setEnabled(false);
            messages.clear();
            currentConnection.readLogs(getFilter(), new TaskReport<List<Message>>() {
                @Override
                public void onFailure(String reason) {

                }

                @Override
                public void onSuccess(List<Message> data) {
                    messages = data;
                    recyclerView.setAdapter(new LogAdapter(messages));
                }

                @Override
                public void onComplete() {
                    launchFilter.setEnabled(true);
                }
            });
        }
    }


}
