package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class ChordsFragment extends OMGFragment {

    private Jam mJam;
    private View mView;
    private MainFragment mainFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choosechords,
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

        String[] chords = getResources().getStringArray(R.array.chord_progressions);

        ListView chordsList = (ListView)mView.findViewById(R.id.chords_list);
        ChordsAdapter soundSetsAdapter = new ChordsAdapter(getActivity(), R.layout.chordoption,
                                            chords, mJam.getScale());
        chordsList.setAdapter(soundSetsAdapter);

        chordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mJam.setChordProgression(((ChordsView)view.findViewById(R.id.chords_option)).getChords());
            }
        });


    }
}
