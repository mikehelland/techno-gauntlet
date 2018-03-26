package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.SequencerTrack;

public class TrackSubMixerFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.mixer_fragment,
                container, false);

        setupPanels(inflater);

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {

        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        for (final SequencerTrack track : getJam().getChannelTracksgetCurrentPart().getPatternInfo().getTracks()) {

            controls = inflater.inflate(R.layout.mixer_panel, container, false);
            container.addView(controls);

            MixerView mixerView = (MixerView) controls.findViewById(R.id.mixer_view);
            mixerView.setJam(track.getName(), new MixerView.MixerViewController() {
                @Override
                void onMuteChange(boolean mute) {
                    getJam().setPartTrackMute(mute);
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
        }
    }
}

