package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ChordsAdapter extends ArrayAdapter<String> {
    private final Context context;
    private int[] scale;

    public ChordsAdapter(Context context, int layout, String[] objects, int[] scale){
        super(context, layout, objects);
        this.context = context;
        this.scale = scale;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final View rowView;
        final ViewHolder holder;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.chordoption, parent, false);
            holder = new ViewHolder();

            holder.chordsView = (ChordsView)rowView.findViewById(R.id.chords_option);
            rowView.setTag(holder);

        }
        else {
            rowView = convertView;
            holder = (ViewHolder)convertView.getTag();
        }

        String[] sChords = getItem(position).split(",");
        int[] chords = new int[sChords.length];
        for (int ic = 0; ic < sChords.length; ic++)
            chords[ic] = Integer.parseInt(sChords[ic]);
        holder.chordsView.setChords(chords, scale);

        return rowView;
    }

    static class ViewHolder {
        ChordsView chordsView;
    }

}

