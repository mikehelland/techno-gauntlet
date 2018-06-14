package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mikehelland.omgtechnogauntlet.jam.OnBeatChangeListener;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class BeatsFragment extends OMGFragment {

    private TextView bpmText;
    private SeekBar bpmSeekBar;
    private TextView shuffleText;
    private SeekBar shuffleSeekBar;
    private TextView measuresText;
    private SeekBar measuresSeekBar;
    private TextView beatsText;
    private SeekBar beatsSeekBar;

    private OnBeatChangeListener mJamListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beats_fragment,
                container, false);

        bpmText = (TextView)view.findViewById(R.id.bpm_caption);
        bpmSeekBar = (SeekBar)view.findViewById(R.id.bpm_seekbar);
        bpmSeekBar.setMax(200);

        shuffleText = (TextView)view.findViewById(R.id.shuffle_caption);
        shuffleSeekBar = (SeekBar)view.findViewById(R.id.shuffle_seekbar);
        measuresText = (TextView)view.findViewById(R.id.measures);
        measuresSeekBar = (SeekBar)view.findViewById(R.id.measures_seekbar);
        beatsText = (TextView)view.findViewById(R.id.timesig);
        beatsSeekBar = (SeekBar)view.findViewById(R.id.beats_seekbar);

        setup();
        refresh();
        setupListener();

        return view;
    }

    private void setup() {

        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    int newbpm = 20 + i;
                    bpmText.setText(Integer.toString(newbpm) + " bpm");
                    getJam().setBPM(newbpm);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        shuffleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    shuffleText.setText(Integer.toString(i) + "% shuffle");
                    getJam().setShuffle(i / 100.0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        measuresSeekBar.setMax(8);

        measuresSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (i > 0) {
                        measuresText.setText(i + " Measures");
                        getJam().setMeasures(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        beatsSeekBar.setMax(8);

        beatsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (i > 0) {
                        beatsText.setText(i + " Beats");
                        getJam().setBeats(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void refresh() {

        int bpm = getJam().getBPM();
        bpmText.setText(Integer.toString(bpm) + " bpm");
        bpmSeekBar.setProgress(bpm - 20);

        int shuffle = (int)(getJam().getShuffle() * 100);
        shuffleText.setText(Integer.toString(shuffle) + "% shuffle");
        shuffleSeekBar.setProgress(shuffle);

        int measures = getJam().getMeasures();
        measuresText.setText(measures + " Measures");
        measuresSeekBar.setProgress(measures);

        int beats = getJam().getBeats();
        beatsText.setText(beats + " Beats");
        beatsSeekBar.setProgress(beats);

    }

    private void setupListener() {
        mJamListener = new OnBeatChangeListener() {
            @Override public void onSubbeatLengthChange(int length, String source) {
                onChange();
            }

            @Override
            public void onBeatsChange(int length, String source) {
                onChange();
            }

            @Override
            public void onMeasuresChange(int length, String source) {
                onChange();
            }

            @Override
            public void onShuffleChange(float length, String source) {
                onChange();
            }
        };
        getJam().addOnBeatChangeListener(mJamListener);
    }

    private void onChange() {
        FragmentActivity activity = getActivity(); if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getJam().removeOnBeatChangeListener(mJamListener);
    }

}
