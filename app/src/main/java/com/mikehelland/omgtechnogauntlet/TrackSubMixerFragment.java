package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TrackSubMixerFragment extends OMGFragment {

    private View mView;

    private List<View> mPanels = new ArrayList<>();

    private Jam.StateChangeCallback mCallback;

    private Channel mChannel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        if (mChannel != null) {
            setupPanels(inflater);
        }
        else {
            Log.d("MGH submixer", "NOT READY!");
        }

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
        for (final SequencerTrack track : mChannel.getPatternInfo().getTracks()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
            mixerView.setJam(track.getName(), new MixerView.MixerViewController() {
                @Override
                void onMuteChange(boolean mute) {
                    track.toggleMute();
                }

                @Override
                void onVolumeChange(float volume) {
                    track.setVolume(volume);
                }

                @Override
                void onPanChange(float pan) {
                    track.setPan(pan);
                }

                @Override
                boolean onGetMute() {
                    return track.isMuted();
                }

                @Override
                float onGetVolume() {
                    return track.getVolume();
                }

                @Override
                float onGetPan() {
                    return track.getPan();
                }
            });

            mPanels.add(mixerView);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        mJam.removeStateChangeListener(mCallback);
    }

    void setChannel(Channel channel) {
        mChannel = channel;
    }
}

