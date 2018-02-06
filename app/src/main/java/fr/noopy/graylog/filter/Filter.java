package fr.noopy.graylog.filter;

import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyrille on 03/02/18.
 */

public class Filter {
    public List<String> fields = new ArrayList<>();
    public String query = "*";
    public String sort = "timestamp:desc";
    public int limit = 150;
    public int seconds = 300;

    public Filter () {

    }

    public String getFieldList() {
        String data = "";
        for(int i = 0; i<fields.size(); i++) {
            data = data + (i > 0 ? "," : "") + fields.get(i);
        }
        return data;
    }

    public RequestParams getRequest() {
        RequestParams result = new RequestParams();
        if (fields != null && !fields.isEmpty()) {
            result.put("fields", getFieldList());
        }
        result.put("query", query);
        if (limit > 0) {
            result.put("limit", limit);
        }
        if (seconds > 0) {
            result.put("seconds", seconds);
        }
        if (sort != null && !sort.isEmpty()) {
            result.put("sort", sort);
        }
        return result;
    }

}
