package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;
import com.mikehelland.omgtechnogauntlet.jam.Surface;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class PartOptionsFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.channel_options,
                container, false);

        setup();

        return mView;
    }

    public void setup() {

        final JamPart part = getPart();

        mView.findViewById(R.id.remove_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJam().removePart(part);
                finish();
            }
        });

        mView.findViewById(R.id.clear_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJam().clearPart(part, null);
                finish();
            }
        });

        mView.findViewById(R.id.copy_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJam().copyPart(part);
                finish();
            }
        });

        String surfaceURL = part.getSurfaceURL();
        View rbSequencer = mView.findViewById(R.id.radioButton);
        if (surfaceURL.equals(Surface.PRESET_SEQUENCER)) {
            ((RadioButton)rbSequencer).toggle();
        }
        rbSequencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (part.getPattern() == null) {
                    getJam().setupSequencerPatternForPart(part);
                }
                setPartSurface(part, new Surface(Surface.PRESET_SEQUENCER));
                finish();
            }
        });
        View rbVertical = mView.findViewById(R.id.radioButton2);
        if (surfaceURL.equals(Surface.PRESET_VERTICAL)) {
            ((RadioButton)rbVertical).toggle();
        }
        rbVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPartSurface(part, new Surface(Surface.PRESET_VERTICAL));
                finish();
            }
        });
        View rbFretboard = mView.findViewById(R.id.radioButton3);
        if (surfaceURL.equals(Surface.PRESET_FRETBOARD)) {
            ((RadioButton)rbFretboard).toggle();
        }
        rbFretboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPartSurface(part, new Surface(Surface.PRESET_FRETBOARD));
                finish();
            }
        });
        rbFretboard.setVisibility(View.GONE);

        mView.findViewById(R.id.choose_a_soundset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundSetFragment f = new SoundSetFragment();
                f.setCallback(new SoundSetFragment.ChoiceCallback() {
                    @Override
                    void onChoice(SoundSet soundSet) {

                    }
                });
                animateFragment(f, 0);
            }
        });

        View zoomButton = mView.findViewById(R.id.zoom_vertical_view);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ZoomFragment f = new ZoomFragment();
                GuitarFragment f = new GuitarFragment();
                f.setPart(getPart());
                f.setZoomModeOn();
                animateFragment(f, 0);
            }
        });
        if (!surfaceURL.equals(Surface.PRESET_VERTICAL)) {
            zoomButton.setVisibility(View.GONE);
        }

        View subMixerButton = mView.findViewById(R.id.track_sub_mixer_button);
        subMixerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrackSubMixerFragment f = new TrackSubMixerFragment();
                f.setPart(part);
                animateFragment(f, 0);
            }
        });
        if (!surfaceURL.equals(Surface.PRESET_SEQUENCER)) {
            subMixerButton.setVisibility(View.GONE);
        }
        View warpTracksButton = mView.findViewById(R.id.track_warp_button);
        warpTracksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrackWarpFragment f = new TrackWarpFragment();
                f.setPart(part);
                animateFragment(f, 0);
            }
        });
        if (!surfaceURL.equals(Surface.PRESET_SEQUENCER)) {
            subMixerButton.setVisibility(View.GONE);
        }

    }

    private void finish() {
        FragmentManager fm = getFragmentManager();
        if (fm == null)
            return;
        fm.popBackStack();
    }

    private void setPartSurface(JamPart part, Surface surface) {
        getJam().setPartSurface(part, surface);
    }
}
