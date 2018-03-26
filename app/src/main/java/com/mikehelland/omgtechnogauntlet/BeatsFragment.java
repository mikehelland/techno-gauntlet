package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class BeatsFragment extends OMGFragment {

    private _OldJam mJam;
    private MainFragment mMainFragment;

    private View mView;
    private TextView bpmText;
    private SeekBar bpmSeekBar;
    private TextView shuffleText;
    private SeekBar shuffleSeekBar;
    private TextView measuresText;
    private SeekBar measuresSeekBar;
    private TextView beatsText;
    private SeekBar beatsSeekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beats_fragment,
                container, false);

        mView = view;
        bpmText = (TextView)view.findViewById(R.id.bpm_caption);
        bpmSeekBar = (SeekBar)view.findViewById(R.id.bpm_seekbar);
        bpmSeekBar.setMax(200);

        shuffleText = (TextView)view.findViewById(R.id.shuffle_caption);
        shuffleSeekBar = (SeekBar)view.findViewById(R.id.shuffle_seekbar);
        measuresText = (TextView)view.findViewById(R.id.measures);
        measuresSeekBar = (SeekBar)view.findViewById(R.id.measures_seekbar);
        beatsText = (TextView)view.findViewById(R.id.timesig);
        beatsSeekBar = (SeekBar)view.findViewById(R.id.beats_seekbar);

        if (mJam != null)
            setup();

        return view;
    }

    public void setJam(_OldJam jam, MainFragment mainFragment) {
        mJam = jam;
        mMainFragment = mainFragment;

        if (mView != null)
            setup();
    }

    private void setup() {

        int bpm = mJam.getBPM();
        bpmText.setText(Integer.toString(bpm) + " bpm");
        bpmSeekBar.setProgress(bpm - 20);

        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    int newbpm = 20 + i;
                    bpmText.setText(Integer.toString(newbpm) + " bpm");
                    mJam.setBPM(newbpm);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMainFragment.updateBPMUI();
            }
        });

        shuffleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    shuffleText.setText(Integer.toString(i) + "% shuffle");
                    mJam.setShuffle(i / 100.0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMainFragment.updateBPMUI();
            }
        });
        int shuffle = (int)(mJam.getShuffle() * 100);
        shuffleText.setText(Integer.toString(shuffle) + "% shuffle");
        shuffleSeekBar.setProgress(shuffle);

        int measures = mJam.getMeasures();
        measuresText.setText(measures + " Measures");
        measuresSeekBar.setProgress(measures);
        measuresSeekBar.setMax(8);

        measuresSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (i > 0) {
                        measuresText.setText(i + " Measures");
                        mJam.setMeasures(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMainFragment.updateBPMUI();
            }
        });

        int beats = mJam.getBeats();
        beatsText.setText(beats + " Beats");
        beatsSeekBar.setProgress(beats);
        beatsSeekBar.setMax(8);

        beatsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (i > 0) {
                        beatsText.setText(i + " Beats");
                        mJam.setBeats(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMainFragment.updateBPMUI();
            }
        });

    }


}
