package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnMixerChangeListener;

import java.util.ArrayList;

public class SampleSpeedFragment extends OMGFragment {

    private View mView;

    private OnMixerChangeListener onMixerChangeListener;

    private ArrayList<SampleSpeedView> mPanels = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.samplespeed_fragment,
                container, false);

        setupPanels(inflater);
        setupListener();

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {


        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        int i = 0;
        for (final JamPart part : getJam().getParts()) {

            controls = inflater.inflate(R.layout.samplespeed_panel, container, false);
            container.addView(controls);

            SampleSpeedView mixerView = (SampleSpeedView) controls.findViewById(R.id.levels_view);
            mixerView.setJam(part.getName(), new SampleSpeedView.LevelViewController() {
                @Override
                void onLevelChange(float level) {
                    getJam().setPartWarp(part, level, null);
                }
                @Override
                float onGetLevel() {
                    return part.getSpeed();
                }
            });
            i++;

            mPanels.add(mixerView);
        }

    }

    private void setupListener() {
        onMixerChangeListener = new OnMixerChangeListener() {
            @Override public void onPartWarpChanged(JamPart part, float speed, String source) {
                for (SampleSpeedView panel : mPanels) {
                    panel.postInvalidate();
                }
            }

            @Override public void onPartMuteChanged(JamPart part, boolean enabled, String source) { }
            @Override public void onPartVolumeChanged(JamPart part, float volume, String source) { }
            @Override public void onPartPanChanged(JamPart part, float pan, String source) { }
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
    }

    @Override
    public void onPause() {
        super.onPause();
        getJam().removeOnMixerChangeListener(onMixerChangeListener);
    }
}

