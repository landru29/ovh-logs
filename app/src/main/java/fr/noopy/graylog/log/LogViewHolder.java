package fr.noopy.graylog.log;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import fr.noopy.graylog.R;
import fr.noopy.graylog.component.FieldDisplay;

/**
 * Created by cyrille on 30/01/18.
 */

public class LogViewHolder extends RecyclerView.ViewHolder{

    private TextView timestampView;
    private LinearLayout fieldList;
    private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private View currentView;

    public LogViewHolder(View itemView) {
        super(itemView);
        currentView = itemView;

        timestampView = (TextView) itemView.findViewById(R.id.timestamp);
        fieldList = (LinearLayout) itemView.findViewById(R.id.fieldList);
    }

    public void bind(Message msg){

        for (int i=0; i<msg.fields.size(); i++) {
            FieldDisplay field = new FieldDisplay(currentView.getContext());
            field.setLabel(msg.fields.get(i));
            field.setValue(msg.get(msg.fields.get(i)));
            fieldList.addView(field);
        }
        timestampView.setText(df.format(msg.timestamp));
    }
}