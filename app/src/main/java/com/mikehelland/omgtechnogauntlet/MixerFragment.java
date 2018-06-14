package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnMixerChangeListener;

import java.util.HashMap;

public class MixerFragment extends OMGFragment {

    private View mView;

    private HashMap<String, View> mPanels = new HashMap<>();

    private OnMixerChangeListener onMixerChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        setupPanels(inflater);


        onMixerChangeListener = new OnMixerChangeListener() {
            @Override
            public void onPartMuteChanged(JamPart part, boolean enabled, String source) {
                View panel = mPanels.get(part.getId());
                if (panel != null)
                    panel.postInvalidate();
            }

            @Override
            public void onPartVolumeChanged(JamPart part, float volume, String source) {
                View panel = mPanels.get(part.getId());
                if (panel != null)
                    panel.postInvalidate();
            }

            @Override
            public void onPartPanChanged(JamPart part, float pan, String source) {
                View panel = mPanels.get(part.getId());
                if (panel != null)
                    panel.postInvalidate();
            }

            @Override public void onPartWarpChanged(JamPart part, float speed, String source) { }
            @Override public void onPartTrackMuteChanged(JamPart part, int track, boolean enabled, String source) {

            }
            @Override public void onPartTrackVolumeChanged(JamPart part, int track, float volume, String source) {

            }
            @Override public void onPartTrackPanChanged(JamPart part, int track, float pan, String source) {

            }
            @Override public void onPartTrackWarpChanged(JamPart part, int track, float speed, String source) {

            }
        };

        getJam().addOnMixerChangeListener(onMixerChangeListener);

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {

        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        for (final JamPart part : getJam().getParts()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            setupPanel(controls, part);
        }
    }

    private void setupPanel(View controls, final JamPart part) {
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
        getJam().removeOnMixerChangeListener(onMixerChangeListener);
    }

}

