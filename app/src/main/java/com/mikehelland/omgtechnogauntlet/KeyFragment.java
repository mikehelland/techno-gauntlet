package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
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

    private View mView;
    private MainFragment mainFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choosekey,
                container, false);

        setup();

        return mView;
    }

    public void setup() {
        Activity activity = getActivity(); if (activity == null)  return;

        String[] roots = getResources().getStringArray(R.array.keys_captions);
        String[] scales = getResources().getStringArray(R.array.quantizer_entries);

        ListView rootsList = (ListView)mView.findViewById(R.id.roots_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_single_choice, roots);
        rootsList.setAdapter(adapter);

        rootsList.setItemChecked(getJam().getKey(), true);

        ListView scalesList = (ListView)mView.findViewById(R.id.scales_list);
        ArrayAdapter<String> scaleAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_single_choice, scales);
        scalesList.setAdapter(scaleAdapter);

        //todo make a keyHelper or just scan the list and manually set it
        //scalesList.setItemChecked(getJam().getScaleIndex(), true);


        rootsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //todo getJam().setKey(i);

                //todo this doesn't look right: mainFragment.updateKeyUI();
            }

        });

        scalesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //todo getJam().setScale();

                //todo this doesn't look right: mainFragment.updateKeyUI();
            }
        });


    }
}
