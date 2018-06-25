package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;

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

        final Button resetButton = (Button)mView.findViewById(R.id.reset_sample_speed);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButton.setText("100%");
                getJam().setPartWarp(part, 1, null);
            }
        });

        SeekBar speedBar = (SeekBar)mView.findViewById(R.id.channel_speed_seekbar);
        speedBar.setMax(200);
        float speed = 100 * part.getSpeed();
        speedBar.setProgress((int)speed);
        resetButton.setText(Math.round(part.getSpeed() * 100) + "% - Press to Reset");

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    float newSpeed = i / 100.0f;
                    resetButton.setText(Math.round(newSpeed * 100) + "% - Press to Reset");
                    if (newSpeed > 0) {
                        getJam().setPartWarp(part, newSpeed, null);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

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
        Activity activity = getActivity();
        if (activity == null)
            return;
        activity.getFragmentManager().popBackStack();
    }

    private void setPartSurface(JamPart part, Surface surface) {
        getJam().setPartSurface(part, surface);
    }
}
