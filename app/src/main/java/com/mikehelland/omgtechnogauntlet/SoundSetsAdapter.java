package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

import java.util.ArrayList;

public class SoundSetsAdapter extends ArrayAdapter<SoundSet> {
    private final Context context;

    SoundSetsAdapter(Context context, int layout, ArrayList<SoundSet> objects){
        super(context, layout, objects);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final View rowView;
        final ViewHolder holder;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.saved_soundset_row, parent, false);
            holder = new ViewHolder();

            holder.textView = (TextView) rowView.findViewById(R.id.saved_data_tags);
            rowView.setTag(holder);

        }
        else {
            rowView = convertView;
            holder = (ViewHolder)convertView.getTag();
        }

        String name = getItem(position).getName();
        holder.textView.setText(name);

        return rowView;
    }

    static class ViewHolder {
        TextView textView;
    }

}

