package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class GuitarFragment extends OMGFragment {

    private Jam mJam;
    private GuitarView guitarView;
    private Channel mChannel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guitar_fragment,
                container, false);

        guitarView = (GuitarView)view.findViewById(R.id.guitarfrets);
        if (mJam != null)
            getPreferredFretboard();


        return view;
    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        mChannel = channel;

        if (guitarView != null) {
            getPreferredFretboard();
        }
    }

    private Fretboard getPreferredFretboard() {

        String sound = mChannel.getMainSound();

        String surfaceURL = mChannel.getSurfaceURL();

        Fretboard fretboard;
        if ("PRESET_VERTICAL".equals(surfaceURL)) {
            fretboard = null;
        }
        else {
            String fretboardJson = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(sound + "_DEFAULT_FRETBOARD_JSON",
                            getString(R.string.default_fretboard_json));
            fretboard = new Fretboard(mChannel, mJam, fretboardJson);
        }


        guitarView.setJam(mJam, mChannel, fretboard);
        return null;
    }

    public void onPause() {
        super.onPause();
        mJam.removeInvalidateOnBeatListener(guitarView);
    }
}
