package fr.noopy.graylog;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by cyrille on 30/01/18.
 */

public class LogViewHolder extends RecyclerView.ViewHolder{

    private TextView textViewView;

    public LogViewHolder(View itemView) {
        super(itemView);

        textViewView = (TextView) itemView.findViewById(R.id.text);
    }

    public void bind(Message msg){
        textViewView.setText(msg.id);
    }
}