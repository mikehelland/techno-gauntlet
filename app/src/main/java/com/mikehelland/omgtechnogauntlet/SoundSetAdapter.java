package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SoundSetAdapter extends SimpleCursorAdapter {
    private final Context context;
    private int[] scale;

    public SoundSetAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context = context;
        this.scale = scale;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View rowView;
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.saved_soundset_row, parent, false);
            holder = new ViewHolder();

            holder.name = (TextView) rowView.findViewById(R.id.saved_data_tags);
            //holder.data = (TextView)rowView.findViewById(R.id.saved_data_date);
            rowView.setTag(holder);

        } else {
            rowView = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        getCursor().moveToPosition(position);
        holder.name.setText(getCursor().getString(getCursor().getColumnIndex("name")));


        return rowView;
    }

    static class ViewHolder {
        TextView name;
    }

}

