package fr.noopy.graylog;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import fr.noopy.graylog.api.Connection;
import fr.noopy.graylog.api.StreamDescriptor;
import fr.noopy.graylog.task.TaskReport;

import static fr.noopy.graylog.StreamFragment.AccessMethod.LOGIN;
import static fr.noopy.graylog.StreamFragment.AccessMethod.TOKEN;

/**
 * Created by cmeichel on 29/01/18.
 */

public class StreamFragment extends Fragment {

    private Context currentContext;

    private EditText urlEdit;
    private EditText tokenEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private Spinner spinner;
    private Button usernameLogin;
    private Button tokenLogin;
    private Button selectStreams;
    private RadioGroup radioMethodGroup;
    private View rootView;
    private List<StreamDescriptor> streams = new ArrayList<StreamDescriptor>();
    private Connection currentConnection;
    public enum AccessMethod { LOGIN, TOKEN}


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

        if (tokenEdit != null &&currentConnection.tokens.containsKey("token")) {
            tokenEdit.setText(currentConnection.tokens.get("token"));
        }
        if (urlEdit != null) {
            urlEdit.setText(currentConnection.getUrl());
        }
    }

    private void setFeaturesEnabled(boolean state) {
        selectStreams.setEnabled(state);
        tokenEdit.setEnabled(state);
        urlEdit.setEnabled(state);
        spinner.setEnabled(state);
        radioMethodGroup.setEnabled(state);
        usernameLogin.setEnabled(state);
        tokenLogin.setEnabled(state);
        usernameEdit.setEnabled(state);
        passwordEdit.setEnabled(state);
    }

    private void setMethod(AccessMethod method) {
        LinearLayout loginMethod = ((Activity) currentContext).findViewById(R.id.loginMethod);
        LinearLayout tokenMethod = ((Activity) currentContext).findViewById(R.id.tokenMethod);
        switch (method) {
            case LOGIN:
                tokenMethod.setVisibility(View.GONE);
                loginMethod.setVisibility(View.VISIBLE);
                break;
            case TOKEN:
                tokenMethod.setVisibility(View.VISIBLE);
                loginMethod.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        selectStreams = rootView.findViewById(R.id.selectStreams);
        tokenEdit = ((Activity) currentContext).findViewById(R.id.readToken);
        urlEdit = ((Activity) currentContext).findViewById(R.id.url);
        spinner = ((Activity) currentContext).findViewById(R.id.stream);
        radioMethodGroup = ((Activity) currentContext).findViewById(R.id.method);
        usernameLogin = ((Activity) currentContext).findViewById(R.id.loginUserPassword);
        tokenLogin = ((Activity) currentContext).findViewById(R.id.loginReadToken);

        usernameEdit = ((Activity) currentContext).findViewById(R.id.username);
        passwordEdit = ((Activity) currentContext).findViewById(R.id.password);


        RadioButton byLogin = ((Activity) currentContext).findViewById(R.id.radioButtonLogin);
        RadioButton byToken = ((Activity) currentContext).findViewById(R.id.radioButtonToken);


        radioMethodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioButtonLogin:
                        setMethod(LOGIN);
                        break;
                    case R.id.radioButtonToken:
                        setMethod(TOKEN);
                        break;
                }
            }
        });

        usernameLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                postLogin(username, password);

            }
        });

        tokenLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentConnection.setToken(tokenEdit.getText().toString(), "token");
                listStreams();
            }
        });

        selectStreams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("STREAM", currentConnection.currentStream.toString());
                currentConnection.saveAsPreference(((MainActivity)getActivity()).getSettings());
                Bundle args = currentConnection.saveAsBundle();
                ((MainActivity)getActivity()).gotoLogs(args);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentConnection.currentStream = streams.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (currentConnection != null) {
            urlEdit.setText(currentConnection.getUrl());
            usernameEdit.setText(currentConnection.username);
            passwordEdit.setText(currentConnection.password);

            if (currentConnection.tokens.containsKey("token")) {
                tokenEdit.setText(currentConnection.tokens.get("token"));

                if (currentConnection.isConsistent()) {
                    listStreams();
                }
            }
        }

        if (currentConnection == null || currentConnection.tokenType != "token") {
            byLogin.toggle();
        } else {
            byToken.toggle();
        }

    }

    private void listStreams() {
        if (currentConnection != null) {
            try {
                setFeaturesEnabled(false);
                currentConnection.setUrl(urlEdit.getText().toString());
                currentConnection.saveAsPreference(((MainActivity) getActivity()).getSettings());
                streams.clear();
                this.currentConnection.listStreams(new TaskReport<List<StreamDescriptor>>() {
                    @Override
                    public void onFailure(String reason) {
                        Toast.makeText(getActivity(), ((MainActivity) currentContext).getText(R.string.server_error),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(List<StreamDescriptor> data) {
                        streams = data;
                        if (streams.size()>0) {
                            ArrayAdapter streamAdapter = new ArrayAdapter<StreamDescriptor>(currentContext, R.layout.spinner, streams);
                            spinner.setAdapter(streamAdapter);
                        }
                    }

                    @Override
                    public void onComplete() {
                        setFeaturesEnabled(true);
                    }
                });

            } catch (MalformedURLException e) {
                Toast.makeText(getActivity(), ((MainActivity) currentContext).getText(R.string.malformed_url),
                        Toast.LENGTH_LONG).show();
                setFeaturesEnabled(true);
            }
        }
    }

    private void postLogin(String username, String password) {
        if (currentConnection != null) {
            setFeaturesEnabled(false);
            try {
                currentConnection.setUrl(urlEdit.getText().toString());
                currentConnection.saveAsPreference(((MainActivity) getActivity()).getSettings());
                currentConnection.login(username, password, new TaskReport<String>() {
                    @Override
                    public void onFailure(String reason) {
                        streams = new ArrayList<StreamDescriptor>();
                        ArrayAdapter streamAdapter = new ArrayAdapter<StreamDescriptor>(currentContext, R.layout.spinner, streams);
                        spinner.setAdapter(streamAdapter);
                        Toast.makeText(getActivity(), ((MainActivity) currentContext).getText(R.string.bad_user_pass),
                                Toast.LENGTH_LONG).show();
                        setFeaturesEnabled(true);
                    }

                    @Override
                    public void onSuccess(String data) {
                        Log.i("SESSION", data);
                        currentConnection.setToken(data, "session");
                        listStreams();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            } catch (MalformedURLException e) {
                Toast.makeText(getActivity(), ((MainActivity) currentContext).getText(R.string.malformed_url),
                        Toast.LENGTH_LONG).show();
                setFeaturesEnabled(true);
            }
        }
    }


}
