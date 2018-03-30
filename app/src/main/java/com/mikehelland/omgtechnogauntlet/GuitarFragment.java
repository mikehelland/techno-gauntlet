package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class GuitarFragment extends OMGFragment {

    private GuitarView guitarView;
    private OnSubbeatListener onSubbeatListener;

    private boolean mZoomMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guitar_fragment,
                container, false);

        guitarView = (GuitarView)view.findViewById(R.id.guitarfrets);

        onSubbeatListener = new OnSubbeatListener() {
            @Override
            public void onSubbeat(int subbeat) {
                guitarView.postInvalidate();
            }
        };

        getJam().addOnSubbeatListener(onSubbeatListener);

        return view;
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
}
