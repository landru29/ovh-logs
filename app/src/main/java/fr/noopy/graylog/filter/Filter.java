package fr.noopy.graylog.filter;

import java.util.ArrayList;
import java.util.Date;
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

}
