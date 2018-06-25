package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnMixerChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.SequencerTrack;

import java.util.ArrayList;

public class TrackSubMixerFragment extends OMGFragment {

    private View mView;

    private ArrayList<View> mPanels = new ArrayList<>();

    private OnMixerChangeListener onMixerChangeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        setupPanels(inflater);

        onMixerChangeListener = new OnMixerChangeListener() {
            @Override
            public void onPartMuteChanged(JamPart part, boolean enabled, String source) { }

            @Override
            public void onPartVolumeChanged(JamPart part, float volume, String source) {}

            @Override
            public void onPartPanChanged(JamPart part, float pan, String source) {}

            @Override public void onPartWarpChanged(JamPart part, float speed, String source) { }

            @Override public void onPartTrackMuteChanged(JamPart part, int track, boolean enabled, String source) {
                View panel = mPanels.get(track);
                if (panel != null)
                    panel.postInvalidate();
            }

            @Override public void onPartTrackVolumeChanged(JamPart part, int track, float volume, String source) {
                View panel = mPanels.get(track);
                if (panel != null)
                    panel.postInvalidate();

            }
            @Override public void onPartTrackPanChanged(JamPart part, int track, float pan, String source) {
                View panel = mPanels.get(track);
                if (panel != null)
                    panel.postInvalidate();

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
        final JamPart part = getPart();
        for (final SequencerTrack track : part.getTracks()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
            mixerView.setJam(track.getName(), new MixerView.MixerViewController() {
                @Override
                void onMuteChange(boolean mute) {
                    getJam().setPartTrackMute(part, track, mute, null);
                }
                @Override
                void onVolumeChange(float volume) {
                    getJam().setPartTrackVolume(part, track, volume, null);
                }
                @Override
                void onPanChange(float pan) {
                    getJam().setPartTrackPan(part, track, pan, null);
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
        getJam().removeOnMixerChangeListener(onMixerChangeListener);
    }
}

