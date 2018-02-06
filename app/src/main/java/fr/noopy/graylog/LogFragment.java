package fr.noopy.graylog;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import fr.noopy.graylog.api.Connection;
import fr.noopy.graylog.api.StreamDescriptor;
import fr.noopy.graylog.api.TimeDescriptor;
import fr.noopy.graylog.filter.Filter;
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
    private Filter currentFilter;
    private boolean loading = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLayoutManager;
    private Spinner spinner;
    public List<TimeDescriptor> timeList = new ArrayList<>();
    public List<String> availableFields = new ArrayList<>();
    public LinearLayout fieldList;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.log_fragment, container, false);

        currentContext = rootView.getContext();

        filter = rootView.findViewById(R.id.filter);
        launchFilter = rootView.findViewById(R.id.launchFilter);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(currentContext);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(new LogAdapter(messages));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = true;
                            Log.v("...", "Last Item Wow !");
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });

        currentFilter = new Filter();
        /*currentFilter.fields.add("timestamp");
        currentFilter.fields.add("msg");
        currentFilter.fields.add("title");*/


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

        fieldList = ((Activity) currentContext).findViewById(R.id.fieldSelector);


        spinner = ((Activity) currentContext).findViewById(R.id.time);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TimeDescriptor current = timeList.get(position);
                currentFilter.seconds = current.duration;
                readLogs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        timeList.clear();
        timeList.add(new TimeDescriptor(5 * 60, currentContext.getString(R.string.last_5_minutes)));
        timeList.add(new TimeDescriptor(10 * 60, currentContext.getString(R.string.last_10_minutes)));
        timeList.add(new TimeDescriptor(15 * 60, currentContext.getString(R.string.last_15_minutes)));
        timeList.add(new TimeDescriptor(60 * 60, currentContext.getString(R.string.last_1_hour)));
        ArrayAdapter streamAdapter = new ArrayAdapter<TimeDescriptor>(currentContext, R.layout.spinner, timeList);
        spinner.setAdapter(streamAdapter);

        readFields();

    }

    private void readLogs () {
        currentFilter.query = filter.getText().toString();
        if (currentConnection!= null) {
            launchFilter.setEnabled(false);
            messages.clear();
            currentConnection.readLogs(currentFilter, new TaskReport<List<Message>>() {
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

    private void readFields() {
        if (currentConnection != null) {
            currentConnection.getFields(new TaskReport<List<String>> () {
                @Override
                public void onFailure(String reason) {

                }

                @Override
                public void onSuccess(List<String> data) {
                    availableFields = data;
                    //fieldList.removeAllViews();
                    for (int i=0; i<availableFields.size(); i++) {
                        CheckBox field = new CheckBox(currentContext);
                        field.setText(availableFields.get(i));
                        fieldList.addView(field);
                        if (currentFilter.fields.contains(availableFields.get(i))) {
                            field.setChecked(true);
                        }
                        field.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                String fieldName = compoundButton.getText().toString();
                                if (isChecked) {
                                    currentFilter.fields.add(fieldName);
                                } else {
                                    currentFilter.fields.remove(fieldName);
                                }
                            }
                        });
                    }
                    Log.i("Fields", availableFields.toString());
                }

                @Override
                public void onComplete() {
                }
            });
        }
    }


}
