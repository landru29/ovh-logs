package fr.noopy.graylog.log;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import fr.noopy.graylog.R;

/**
 * Created by cyrille on 30/01/18.
 */

public class LogViewHolder extends RecyclerView.ViewHolder{

    private TextView textViewView;
    private TextView timestampView;
    private TextView titleView;
    private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public LogViewHolder(View itemView) {
        super(itemView);

        textViewView = (TextView) itemView.findViewById(R.id.text);
        timestampView = (TextView) itemView.findViewById(R.id.timestamp);
        titleView = (TextView) itemView.findViewById(R.id.title);
    }

    public void bind(Message msg){
        textViewView.setText(msg.get("msg"));
        titleView.setText("title:" + msg.get("title"));
        timestampView.setText(df.format(msg.timestamp));
    }
}