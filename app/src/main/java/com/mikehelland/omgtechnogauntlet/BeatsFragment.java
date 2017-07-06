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

    private Jam mJam;
    private MainFragment mMainFragment;

    private View mView;
    private TextView bpmText;
    private SeekBar bpmSeekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beats_fragment,
                container, false);

        mView = view;
        bpmText = (TextView)view.findViewById(R.id.bpm_caption);
        bpmSeekBar = (SeekBar)view.findViewById(R.id.bpm_seekbar);
        bpmSeekBar.setMax(200);

        if (mJam != null)
            setup();

        return view;
    }

    public void setJam(Jam jam, MainFragment mainFragment) {
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
    }


}
