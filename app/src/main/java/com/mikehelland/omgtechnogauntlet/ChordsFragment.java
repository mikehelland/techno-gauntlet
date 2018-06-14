package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class ChordsFragment extends OMGFragment {

    private View mView;

    ChordsView mChordsView;

    private boolean recordChords = false;

    private OnSubbeatListener onSubbeatListener = new OnSubbeatListener() {
        @Override
        public void onSubbeat(int subbeat) {
            if (mChordsView != null && subbeat == 0) {
                mChordsView.postInvalidate();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choosechords,
                container, false);

        setup();

        return mView;
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
                getJam().setProgression(new int[]{0});
                mChordsView.invalidate();
            }
        });

        String[] chords = getResources().getStringArray(R.array.chords);

        ListView chordsList = (ListView)mView.findViewById(R.id.chords_list);
        ChordsAdapter soundSetsAdapter = new ChordsAdapter(activity, R.layout.chordoption,
                                            chords, getJam().getScale());
        chordsList.setAdapter(soundSetsAdapter);

        chordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int[] chords = ((ChordsView)view.findViewById(R.id.chords_option)).getChords();
                if (recordChords) {
                    int[] newProgression = new int[getJam().getProgression().length + 1];
                    System.arraycopy(getJam().getProgression(), 0,
                            newProgression, 0, getJam().getProgression().length);
                    newProgression[newProgression.length - 1] = chords[0];
                    getJam().setProgression(newProgression);
                }
                else {
                    getJam().setProgression(chords);
                }
                mChordsView.invalidate();
            }
        });

        mChordsView = (ChordsView)mView.findViewById(R.id.chords_view);
        mChordsView.setJam(getJam());

        getJam().addOnSubbeatListener(onSubbeatListener);
        getJam().addOnJamChangeListener(onJamChangeListener);
    }

    private OnJamChangeListener onJamChangeListener = new OnJamChangeListener() {
        @Override
        public void onChordProgressionChange(int[] chords, String source) {
            mChordsView.postInvalidate();
        }

        @Override public void onNewPart(JamPart part) { }
        @Override public void onPlay(String source) { }
        @Override public void onStop(String source) { }
        @Override public void onNewLoop(String source) { }
        @Override public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) { }
        @Override public void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source) { }
        @Override public void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) { }
        @Override public void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source) { }
        @Override public void onPartEndLiveNotes(JamPart jamPart, String source) { }
    };

    @Override
    public void onPause() {
        super.onPause();
        getJam().removeOnSubbeatListener(onSubbeatListener);
        getJam().removeOnJamChangeListener(onJamChangeListener);
    }
}
