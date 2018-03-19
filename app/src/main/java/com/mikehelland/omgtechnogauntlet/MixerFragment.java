package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public class MixerFragment extends OMGFragment {

    private View mView;

    private HashMap<Channel, View> mPanels = new HashMap<>();

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
            void onChannelEnabledChanged(Channel channel, boolean enabled, String source) {
                View panel = mPanels.get(channel);
                if (panel != null)
                    panel.postInvalidate();
            }

            @Override
            void onChannelVolumeChanged(Channel channel, float v, String source) {
                View panel = mPanels.get(channel);
                if (panel != null)
                    panel.postInvalidate();
            }
            @Override
            void onChannelPanChanged(Channel channel, float p, String source) {
                View panel = mPanels.get(channel);
                if (panel != null)
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

            setupPanel(controls, channel);
        }
    }

    private void setupPanel(View controls, final Channel channel) {
        MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
        mixerView.setJam(channel.getSoundSetName(), new MixerView.MixerViewController() {
            @Override
            void onMuteChange(boolean mute) {
                mJam.setChannelEnabled(channel, !mute, null);
            }

            @Override
            void onVolumeChange(float volume) {
                mJam.setChannelVolume(channel, volume, null);
            }

            @Override
            void onPanChange(float pan) {
                mJam.setChannelPan(channel, pan, null);
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

        mPanels.put(channel, mixerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        mJam.removeStateChangeListener(mCallback);
    }

}

