package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class ChordsFragment extends OMGFragment {

    private Jam mJam;
    private View mView;

    ChordsView mChordsView;

    private boolean recordChords = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choosechords,
                container, false);

        if (mJam != null)
            setup();

        return mView;
    }

    public void setJam(Jam jam) {
        mJam = jam;

        if (mView != null)
            setup();
    }

    public void setup() {

        Activity activity = getActivity(); if (activity == null)  return;

        final Button recordChordsButton = (Button)mView.findViewById(R.id.record_chords_button);
        recordChordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordChords = !recordChords;
                if (recordChords)
                    recordChordsButton.setText("o");
                else
                    recordChordsButton.setText("+");
            }
        });
        mView.findViewById(R.id.clear_chords_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJam.setChordProgression(new int[]{0});
                mChordsView.invalidate();
            }
        });

        String[] chords = getResources().getStringArray(R.array.chords);

        ListView chordsList = (ListView)mView.findViewById(R.id.chords_list);
        ChordsAdapter soundSetsAdapter = new ChordsAdapter(activity, R.layout.chordoption,
                                            chords, mJam.getScale());
        chordsList.setAdapter(soundSetsAdapter);

        chordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int[] chords = ((ChordsView)view.findViewById(R.id.chords_option)).getChords();
                if (recordChords) {
                    int[] newProgression = new int[mJam.getChordProgression().length + 1];
                    System.arraycopy(mJam.getChordProgression(), 0,
                            newProgression, 0, mJam.getChordProgression().length);
                    newProgression[newProgression.length - 1] = chords[0];
                    mJam.setChordProgression(newProgression);
                }
                else {
                    mJam.setChordProgression(chords);
                }
                mChordsView.invalidate();
            }
        });

        mChordsView = (ChordsView)mView.findViewById(R.id.chords_view);
        mChordsView.setJam(mJam);
    }
}
