package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class DrumFragment extends OMGFragment {

    private Jam mJam;
    //private DrumChannel mChannel;
    private Channel mChannel;
    private DrumView drumMachine;
    //private DrumDownBeatView drumMachine;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drum_fragment,
                container, false);

        drumMachine = (DrumView)view.findViewById(R.id.drummachine);
        //drumMachine = (DrumDownBeatView)view.findViewById(R.id.drummachine);
        if (mJam != null)
            drumMachine.setJam(mJam, mChannel);

        return view;
    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        mChannel = channel;

        if (drumMachine != null)
            drumMachine.setJam(mJam, channel);
    }

}
