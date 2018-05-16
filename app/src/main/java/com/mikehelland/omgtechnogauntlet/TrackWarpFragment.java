package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.SequencerTrack;

public class TrackWarpFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        setupPanels(inflater);

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {

        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        final JamPart part = getPart();
        for (final SequencerTrack track : part.getTracks()) {

            controls = inflater.inflate(R.layout.samplespeed_panel, container, false);
            container.addView(controls);

            SampleSpeedView mixerView = (SampleSpeedView) controls.findViewById(R.id.levels_view);
            mixerView.setJam(track.getName(), track.getSpeed(), new SampleSpeedView.LevelViewController() {
                @Override
                void onLevelChange(float level) {
                    getJam().setPartTrackWarp(part, track, level);
                }
            });
        }
    }
}

