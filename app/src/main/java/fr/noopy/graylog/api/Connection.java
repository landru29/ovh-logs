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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import fr.noopy.graylog.filter.Filter;
import fr.noopy.graylog.log.Message;
import fr.noopy.graylog.task.TaskReport;

/**
 * Created by cyrille on 31/01/18.
 */

public class Connection {
    public String name;
    public Map<String, String> tokens = new HashMap<String, String>();
    public URL url;
    public String tokenType = "";
    public StreamDescriptor currentStream;
    public String username = "";
    public String password = "";

    public static final String PREFS_CONNECTION = "connection";
    public static final String JSON_FIELD_TOKENS = "tokens";
    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_URL = "url";
    public static final String JSON_FIELD_USERNAME = "username";
    public static final String JSON_FIELD_PASSWORD = "password";
    public static final String JSON_FIELD_TOKEN_TYPE = "tokenType";


    private static final List<String>fieldExclude=Arrays.asList("ProcessName","cs-method","LogonGuid","InstanceID","ident","source_geolocation","TargetLogonGuid","path","sc-win32-status","application_name",
            "CustomLevel","gl2_source_node","TrID","Status","cache","InstanceId","ResultsReturned","module","ray","KeyLength","version","from_apache","size_int","TargetInfo","filename","size","http_referer",
            "domain","EventID","source_country_code","timer_upload_num","RecordNumber","TargetUserSid","PackageName","VirtualAccount","Opcode","TransmittedServices","RestrictedAdminMode","hLastError",
            "source_ip_country_code","set_cookie","WorkstationName","SubStatus","Path","XPath","SourceModuleType","ActivityID","SocketError","elastic","Event","IpAddress","http_status","dc","agent",
            "TargetOutboundDomainName","cs-uri-stem","NewStateName","cs-uri-query","response_time_int","Port","csUser-Agent","project","IpPort","source","gl2_source_input","ua","LmPackageName","Name",
            "container_region","http_method","restartRequired","Version","MethodName","namespaceName","req","ConnectionType","IdleSeconds2","IdleSeconds","IdleSeconds1","TunnelID","EventReceivedTime",
            "PreviousState","CallerProcessId","IpAddress_country_code","c-ip_geolocation","TaskName","FailureReason","subdomain","ImpersonationLevel","TargetUserName","Domain","ipaddress","Keywords","ENV",
            "TargetServerName","ElevatedToken","company","TargetDomainName","ip_geolocation","c-ip_country_code","LogonType","length","Severity","userName","ipaddress_geolocation","status_int","TargetLogonId",
            "Workstation","LastError","ProviderGuid","TargetLinkedLogonId","full_message","s-ip","EnginePID","service","step","ErrorCode","time","c-ip","source_ip_city_name","ip_city_name","wmiClassName",
            "AuthenticationPackageName","fileuuid","source_ip_geolocation","ConnType","Count","program","type","source_ip","Verb","action","ID","id","ipaddress_city_name","cookie","method","reqtime","ip",
            "source_city_name","streams","priority","Namespace","ActionName","MachineName","Payload","http_request","SubjectUserSid","AccountName","monitoring_long","requestGuid","status","server",
            "IpAddress_city_name","log","server_country_code","serverName","sc-substatus","sd_id","ConnectionName","file","PreviousStateName","UserContext","EventName","SubjectLogonId","uValue",
            "TargetOutboundUserName","server_geolocation","TaskInstanceId","ThreadID","s-port","IPString","http_status_orig","facility","ResultCode","date","referer","Task","instance","EventType","TargetSid",
            "pid","request_size_byte","TotalXPaths","duration_sec","Bytes","protocol","filepath","forwardedfor","IpAddress_geolocation","ProcessID","tag","hResult","server_city_name","RefreshTriggerSource",
            "OpcodeValue","Priority","Channel","StateTransition","runtime","ChannelsExist","ClientIP","ReasonCode","time-taken","SourceModuleName","accept_lang","vhost","ChannelName","NewState","Value",
            "process_id","Category","DeviceId","line","latency","error","CallerProcessName","IPString_country_code","SeverityValue","UserID","IPString_geolocation","SubjectDomainName","LogonProcessName","value",
            "PrivilegeList","user_agent","ip_country_code","ipaddress_country_code","duration_usec","Error","SourceName","methodName","thread","transport","AccountType","param1","param2","SubjectUserName",
            "http_request_path","IPString_city_name","c-ip_city_name","Machines","user","TaskEngineName","SerializedException","domain_geolocation","Minutes","sc-status","LowestFreeSpaceInBytes",
            "NumberOfGPOsDownloaded","OperationElaspedTimeInMilliSeconds","OperationDescription","PathID","requestFullUrl","IoCountArray","NumberOfGPOsApplicable","MajorType","data1","Parameter",
            "domain_city_name","loggedUser","IsBootVolume","remoteAddress","VolumeName","VolumeGuid","PolicyDownloadTimeElapsedInMilliseconds","child","VolumeNameLength","LinkDescription",
            "NetworkBandwidthInKbps","AppDomain","MinorType","IoLatencyBuckets","UserName","IoCountArrayLength","BandwidthInkbps","IsBackgroundProcessing","ReasonString","AppPoolID","threadName",
            "GPODownloadTimeElapsedInMilliseconds","IsAsyncProcessing","domain_country_code","HighestFreeSpaceInBytes","hrError","TargetID","remoteAddress_country_code","SecondsSinceLastSentPacket","FolderId",
            "size_int_fixit","levelName","IsMachine","TransportType","loggerName","SessionId","remoteAddress_city_name","PortNumber","ovh_warn_mismatch_num_type","SessionID","Reason","Function",
            "MaxOutstandingCount","requestPath","LUN","IsSlowLink","PolicyApplicationMode","ThresholdInkbps","BucketIoCount5","remoteAddress_geolocation","BucketIoCount3","PolicyProcessingMode","BucketIoCount4",
            "BucketIoCount1","BucketIoCount2","MaxDeviceQueueCount","InfoDescription","ClassDeviceGuid", "Expires", "totalCount", "Created", "RequestStatus", "user-agent", "ResourceURI", "HasEndAuthUrl",
            "TokenType", "elapsedTime", "HasAuthUrl", "AuthRequired", "HasFlowUrl", "removedCount", "QueueTileCleanups", "NameLength", "QueueTileCloses", "PolicyElapsedTimeInSeconds", "IsDomainJoined", "SessionName",
            "MonitorWidth", "WinlogonReturnTimeElapsedInMilliseconds", "ComponentName", "NewPhysicalMediumType", "DescriptionString", "KaValue", "KeyType", "ClientMode", "AlgorithmName", "KaMinLimit",
            "HiveNameLength");

    public Connection() {
        this.name = "default";
        try {
            this.url = new URL("");
        } catch (MalformedURLException e) {

        }
    }

    public Connection(String name, Map<String, String> tokens, String urlStr, String username, String password, String tokentype) throws MalformedURLException {
        this.name = name;
        this.tokens = tokens;
        this.url = new URL(urlStr);
        this.username = username;
        this.password = password;
        this.tokenType = tokentype;
    }

    public Connection(JSONObject data) throws MalformedURLException, JSONException {
        this(
                data.has(JSON_FIELD_NAME) ? data.getString(JSON_FIELD_NAME) : "default",
                getTokens(data),
                data.has(JSON_FIELD_URL) ? data.getString(JSON_FIELD_URL) : "",
                data.has(JSON_FIELD_USERNAME) ? data.getString(JSON_FIELD_USERNAME) : "",
                data.has(JSON_FIELD_PASSWORD) ? data.getString(JSON_FIELD_PASSWORD) : "",
                data.has(JSON_FIELD_TOKEN_TYPE) ? data.getString(JSON_FIELD_TOKEN_TYPE) : ""
        );
        if (!tokens.isEmpty() && tokenType.isEmpty() ) {
            Set<String> keys = tokens.keySet();
            tokenType = keys.iterator().next();
        }
        if (data.has("stream")) {
            currentStream = new StreamDescriptor(data.getString("stream"));
        }
    }

    public Connection(String data) throws MalformedURLException, JSONException {
        this(new JSONObject(data));
        Log.i("CONNEXION BUILDER", data);
    }

    public static Map<String, String> getTokens(JSONObject data) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();
        if (data.has(JSON_FIELD_TOKENS)) {
            JSONObject object = data.getJSONObject(JSON_FIELD_TOKENS);
            Iterator<String> keysItr = object.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                String value = object.get(key).toString();
                map.put(key, value);
            }
        }
        Log.i("TOKENS", map.toString());
        return map;
    }


    public String toString() {
        JSONObject result = new JSONObject();
        try {
            result.put(JSON_FIELD_NAME, name);
            result.put(JSON_FIELD_TOKENS, new JSONObject(tokens));
            result.put(JSON_FIELD_TOKEN_TYPE, tokenType);
            result.put(JSON_FIELD_URL, url.toString());
            result.put(JSON_FIELD_USERNAME, username);
            result.put(JSON_FIELD_PASSWORD, password);
            if (currentStream != null) {
                result.put("stream", currentStream.stringify());
            }
            return result.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public void saveAsPreference(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        String dataStr = toString();
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

    public String fieldsUrl() {
        return builder()
                .appendPath("system")
                .appendPath("fields")
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
            client.setBasicAuth(tokens.get(tokenType), tokenType);
            Log.i("basic", tokens.get(tokenType) + ":" + tokenType);
        }
        client.addHeader("accept", "application/json");
        return client;
    }

    public AsyncHttpClient client() {
        return client(true);
    }

    public boolean isConsistent() {
        return (url != null) && tokens.containsKey(tokenType);
    }

    public void setToken(String data, String dataType) {
        tokens.put(dataType, data);
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

    public void readLogs (Filter filter, final TaskReport<List<Message>> task) {
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
        RequestParams request = filter.getRequest();
        request.put("filter", "streams:" + this.currentStream.id);
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

    public void login(final String log_username, final String log_password, final TaskReport<String> task) {
        if (url == null) {
            Log.i("OMG", "Connection is unconsistent");
            task.onFailure("Connection is unconsistent");
            task.onComplete();
            return;
        }
        final String urlStr = this.loginUrl();
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put(JSON_FIELD_USERNAME, log_username);
            jsonParams.put(JSON_FIELD_PASSWORD, log_password);
            jsonParams.put("host", url.getHost());
            AsyncHttpClient client = this.client(false);
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
                            username = log_username;
                            password = log_password;
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

    public void getFields(final TaskReport<List<String>> task) {
        if (url == null) {
            Log.i("OMG", "Connection is unconsistent");
            task.onFailure("Connection is unconsistent");
            task.onComplete();
            return;
        }
        final String urlStr = this.fieldsUrl();
        AsyncHttpClient client = this.client();
        client.addHeader("Accept", "application/json");
        client.get(null, urlStr, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i("graylog", "starting " + urlStr);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.has("fields")) {
                    try {
                        List<String> result = new ArrayList<>();
                        JSONArray fields = response.getJSONArray("fields");
                        for( int i=0; i< fields.length(); i++) {
                            if (!fieldExclude.contains(fields.getString(i))) {
                                result.add(fields.getString(i));
                            }
                        }
                        task.onSuccess(result);
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

    }

}
