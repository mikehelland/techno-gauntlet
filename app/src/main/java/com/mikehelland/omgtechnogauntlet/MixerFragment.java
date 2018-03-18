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

            @Override
            void onChannelVolumeChanged(int channelNumber, float v, String source) {
                for (View panel : mPanels)
                    panel.postInvalidate();
            }
            @Override
            void onChannelPanChanged(int channelNumber, float p, String source) {
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
        int i = 0;
        for (final Channel channel : mJam.getChannels()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            setupPanel(controls, channel, i);
            i++;
        }
    }

    private void setupPanel(View controls, final Channel channel, final int i) {
        MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
        mixerView.setJam(channel.getSoundSetName(), new MixerView.MixerViewController() {
            @Override
            void onMuteChange(boolean mute) {
                mJam.setChannelEnabled(i, !mute, null);
            }

            @Override
            void onVolumeChange(float volume) {
                mJam.setChannelVolume(i, volume, null);
            }

            @Override
            void onPanChange(float pan) {
                mJam.setChannelPan(i, pan, null);
            }

            @Override
            boolean onGetMute() {
                return !channel.isEnabled();
            }

            @Override
            float onGetVolume() {
                return channel.getVolume();
            }

            @Override
            float onGetPan() {
                return channel.getPan();
            }
        });

        mPanels.add(mixerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        mJam.removeStateChangeListener(mCallback);
    }

}

