package fr.noopy.graylog;

import android.net.Uri;

import com.loopj.android.http.AsyncHttpClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cmeichel on 29/01/18.
 */

public class Stream {

    public static String token = "";
    public static URL url;

    public static Uri.Builder builder() {
        Uri.Builder builder = new Uri.Builder();
        return builder.scheme(url.getProtocol())
                .authority(url.getHost())
                .appendPath(url.getPath());
    }

    public static Uri.Builder builder(String urlStr) {
        try {
            url = new URL(urlStr);
            return builder();

        } catch (MalformedURLException e) {
            return new Uri.Builder();
        }
    }

    public static String streamsUrl(String urlStr) {
        return Stream.builder(urlStr)
                .appendPath("streams").toString();
    }

    public static String relativeSearchUrl(String urlStr) {
        return Stream.builder(urlStr)
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
