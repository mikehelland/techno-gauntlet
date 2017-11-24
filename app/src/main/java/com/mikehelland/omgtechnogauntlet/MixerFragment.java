package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MixerFragment extends OMGFragment {

    private View mView;

    private List<View> mPanels = new ArrayList<>();

    private Jam.StateChangeCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        setupPanels(inflater);


        mCallback = new Jam.StateChangeCallback() {
            @Override void newState(String state, Object... args) {}
            @Override void onSubbeatLengthChange(int length, String source) {}
            @Override void onKeyChange(int key, String source) {}
            @Override void onScaleChange(String scale, String source) {}
            @Override void onChordProgressionChange(int[] chords) {}
            @Override void onNewChannel(Channel channel) {}

            @Override
            void onChannelEnabledChanged(int channelNumber, boolean enabled, String source) {
                for (View panel : mPanels)
                    panel.postInvalidate();
            }
        };
        mJam.addStateChangeListener(mCallback);

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {


        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        for (final Channel channel : mJam.getChannels()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
            mixerView.setJam(mJam, channel, channel.getSoundSetName());

            mPanels.add(mixerView);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        mJam.removeStateChangeListener(mCallback);
    }

}

