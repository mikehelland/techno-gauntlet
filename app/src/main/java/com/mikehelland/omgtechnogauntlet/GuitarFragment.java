package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class GuitarFragment extends OMGFragment {

    private VerticalView guitarView;
    private OnSubbeatListener onSubbeatListener;

    private boolean mZoomMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guitar_fragment,
                container, false);

        guitarView = (VerticalView) view.findViewById(R.id.guitarfrets);

        VerticalView.OnGestureListener onGestureListener = makeOnGestureListener();

        guitarView.setJam(getJam(), getPart(), onGestureListener);

        onSubbeatListener = new OnSubbeatListener() {
            @Override
            public void onSubbeat(int subbeat) {
                guitarView.postInvalidate();
            }
        };

        getJam().addOnSubbeatListener(onSubbeatListener);

        if (mZoomMode) {
            guitarView.setZoomModeOn();
        }

        return view;
    }

    private VerticalView.OnGestureListener makeOnGestureListener() {
        if (!mZoomMode) {
            return new VerticalView.OnGestureListener() {
                @Override
                void onStart(Note note, int autoBeat) {
                    getJam().startPartLiveNotes(getPart(), note, getAutoBeat(autoBeat));
                }

                @Override
                void onUpdate(Note[] notes, int autoBeat) {
                    getJam().updatePartLiveNotes(getPart(), notes, getAutoBeat(autoBeat));
                }

                @Override
                void onRemove(Note note, Note[] notes) {
                    getJam().removeFromPartLiveNotes(getPart(), note, notes);
                }

                @Override
                void onEnd() {
                    getJam().endPartLiveNotes(getPart());
                }
            };
        }
        else {
            return new VerticalView.OnGestureListener() {
                @Override
                void onStart(Note note, int autoBeat) { }

                @Override
                void onUpdate(Note[] notes, int autoBeat) { }

                @Override
                void onRemove(Note note, Note[] notes) { }

                @Override
                void onEnd() {
                    getJam().setPartZoom(getPart(), guitarView.getSkipBottom(), guitarView.getSkipTop());
                }
            };
        }
    }

    void setZoomModeOn() {
        mZoomMode = true;
    }

    /*private Fretboard getPreferredFretboard() {

        String surfaceURL = getPart().getSurfaceURL();
        String surfaceJSON = getPart().getSurfaceJSON();

        Fretboard fretboard;
        if ("PRESET_VERTICAL".equals(surfaceURL)) {
            fretboard = null;
        }
        else {
            if (surfaceJSON == null) {
                surfaceJSON = getString(R.string.default_fretboard_json);
            }
            fretboard = new Fretboard(mPart, mJam, surfaceJSON);
        }

        if (mZoomMode) {
            guitarView.setZoomModeOn();
        }
        guitarView.setJam(mJam, mPart, fretboard);
        return null;
    }*/

    public void onPause() {
        super.onPause();
        getJam().removeOnSubbeatListener(onSubbeatListener);
    }

    private int getAutoBeat(int column) {
        if (column == 3) {
            return 1;
        }
        if (column == 1) {
            return 4;
        }

        return column;
    }
}
