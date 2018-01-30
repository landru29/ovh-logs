package fr.noopy.graylog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by cyrille on 30/01/18.
 */

public class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {

    List<Message> list;

    //ajouter un constructeur prenant en entrée une liste
    public LogAdapter(List<Message> list) {
        this.list = list;
    }

    //cette fonction permet de créer les viewHolder
    //et par la même indiquer la vue à inflater (à partir des layout xml)
    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.log_cell,viewGroup,false);
        return new LogViewHolder(view);
    }

    //c'est ici que nous allons remplir notre cellule avec le texte/image de chaque MyObjects
    @Override
    public void onBindViewHolder(LogViewHolder myViewHolder, int position) {
        Message myObject = list.get(position);
        myViewHolder.bind(myObject);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}