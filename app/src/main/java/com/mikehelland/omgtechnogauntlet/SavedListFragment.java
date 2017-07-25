package com.mikehelland.omgtechnogauntlet;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SavedListFragment extends ListFragment
{
    private Cursor cursor;
    private int page;
    //private boolean noMoreToDownload = false;

    private TextView foot;
    private TextView head;

    private ArrayList<String> jsonArray = new ArrayList<String>();

    private int headerOffset = 0;

    private View mView;

    private Jam mJam;

    private MainFragment mMainFragment;

    public void setJam(MainFragment mainFragment, Jam jam) {
        mJam = jam;
        mMainFragment = mainFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mView = inflater.inflate(R.layout.saved_fragment,
                container, false);

        page = 1;

        Context context = getActivity();

        cursor = new SavedDataOpenHelper(context).getSavedCursor();

        SimpleCursorAdapter curA = new SavedDataAdapter(context,
                R.layout.saved_row,
                cursor, new String[]{"tags", "time"},
                new int[]{R.id.saved_data_tags, R.id.saved_data_date});
        setListAdapter(curA);

        head = new TextView(context);
        head.setText(getString(R.string.saved_list_title));
        head.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        head.setPadding(20, 20, 20, 20);
        head.setGravity(0x11);
        ((ListView)mView.findViewById(android.R.id.list)).addHeaderView(head, -7, true);

        headerOffset = 1;

        return mView;
    }


    public void onListItemClick(ListView l, View v, int position, long id){

        if (v == head) {
            return;
        }


        cursor.moveToPosition(position - headerOffset);
        String json = cursor.getString(cursor.getColumnIndex("data"));

        Jam jam = new Jam(getActivity(), mMainFragment.mPool, mMainFragment.mJamCallback);
        jam.load(json, true);

        mJam.finish();
        mJam = jam;

        ((Main)getActivity()).mJam = jam;


        if (!mJam.isPlaying())
            mJam.kickIt();

        mMainFragment.updateUI();

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            cursor.close();
        } catch (Exception e) {}

        //if (!isFinishing())
        //    finish();
    }



}
