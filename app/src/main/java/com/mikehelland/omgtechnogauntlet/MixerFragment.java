package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnMixerChangeListener;

import java.util.HashMap;

public class MixerFragment extends OMGFragment {

    private View mView;

    private HashMap<String, View> mPanels = new HashMap<>();

    private OnMixerChangeListener onMixerChangeListener;
    private OnJamChangeListener onJamChangeListener;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
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

        onJamChangeListener = new OnJamChangeListener() {

            @Override
            public void onNewJam(Jam jam, String source) {
                Activity activity = getActivity();
                if (activity == null) return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupPanels(inflater);
                    }
                });
            }

            @Override public void onChordProgressionChange(int[] chords, String source) { }
            @Override public void onNewPart(JamPart part, String source) { }
            @Override public void onPlay(String source) { }
            @Override public void onStop(String source) { }
            @Override public void onNewLoop(String source) { }
            @Override public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) { }
            @Override public void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source) { }
            @Override public void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) { }
            @Override public void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source) { }
            @Override public void onPartEndLiveNotes(JamPart jamPart, String source) { }
            @Override public void onPartClear(JamPart jamPart, String source) { }
        };

        getJam().addOnMixerChangeListener(onMixerChangeListener);
        getJam().addOnJamChangeListener(onJamChangeListener);

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {

        mPanels.clear();
        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        container.removeAllViews();
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
        getJam().removeOnJamChangeListener(onJamChangeListener);
    }

}

