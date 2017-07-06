package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class KeyFragment extends OMGFragment {

    private Jam mJam;
    private View mView;
    private MainFragment mainFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choosekey,
                container, false);

        if (mJam != null)
            setup();

        return mView;
    }

    public void setJam(Jam jam, MainFragment main) {
        mJam = jam;
        mainFragment = main;

        if (mView != null)
            setup();
    }

    public void setup() {

        String[] roots = getResources().getStringArray(R.array.keys_captions);
        String[] scales = getResources().getStringArray(R.array.quantizer_entries);

        ListView rootsList = (ListView)mView.findViewById(R.id.roots_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_single_choice, roots);
        rootsList.setAdapter(adapter);

        rootsList.setItemChecked(mJam.getKey(), true);

        ListView scalesList = (ListView)mView.findViewById(R.id.scales_list);
        ArrayAdapter<String> scaleAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_single_choice, scales);
        scalesList.setAdapter(scaleAdapter);

        scalesList.setItemChecked(mJam.getScaleIndex(), true);


        rootsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mJam.setKey(i);

                mainFragment.updateKeyUI();
            }

        });

        scalesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("MGH Item Selected", Integer.toString(i));
                mJam.setScale(i);

                mainFragment.updateKeyUI();
            }
        });


    }
}
