package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.Part;

import java.util.HashMap;

public class MixerFragment extends OMGFragment {

    private View mView;

    private HashMap<String, View> mPanels = new HashMap<>();

    //private _OldJam.StateChangeCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        setupPanels(inflater);


        /*mCallback = new _OldJam.StateChangeCallback() {
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
        */

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {

        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        for (final Part part : getJam().getParts()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            setupPanel(controls, part);
        }
    }

    private void setupPanel(View controls, final Part part) {
        MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
        mixerView.setJam(part.getName(), new MixerView.MixerViewController() {
            @Override
            void onMuteChange(boolean mute) {
                getJam().setPartMute(part, mute, null);
            }

            @Override
            void onVolumeChange(float volume) {
                getJam().setPartVolume(part, volume, null);
            }

            @Override
            void onPanChange(float pan) {
                getJam().setPartPan(part, pan, null);
            }

            @Override
            boolean onGetMute() {
                return getJam().getPartMute(part);
            }

            @Override
            float onGetVolume() {
                return getJam().getPartVolume(part);
            }

            @Override
            float onGetPan() {
                return getJam().getPartPan(part);
            }
        });

        mPanels.put(part.getId(), mixerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        //mJam.removeStateChangeListener(mCallback);
    }

}

