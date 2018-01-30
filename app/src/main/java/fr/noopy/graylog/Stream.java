package fr.noopy.graylog;

import android.net.Uri;

import com.loopj.android.http.AsyncHttpClient;

/**
 * Created by cmeichel on 29/01/18.
 */

public class Stream {

    public static String domain = "gra2.logs.ovh.com";
    public static String scheme = "https";
    public static String path = "api";
    public static String token = "";

    public static Uri.Builder builder() {
        Uri.Builder builder = new Uri.Builder();
        return builder.scheme(scheme)
                .authority(domain)
                .appendPath(path);
    }

    public static String streamsUrl() {
        return Stream.builder()
                .appendPath("streams").toString();
    }

    public static String relativeSearchUrl() {
        return Stream.builder()
                .appendPath("search")
                .appendPath("universal")
                .appendPath("relative")
                .toString();
    }

    public static AsyncHttpClient client() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(token, "token");
        return client;
    }
}
