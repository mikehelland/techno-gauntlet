package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class DrumFragment extends OMGFragment {

    private DrumView drumMachine;
    private OnSubbeatListener onSubbeatListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drum_fragment,
                container, false);

        drumMachine = (DrumView)view.findViewById(R.id.drummachine);

        drumMachine.setJam(getJam(), getJam().getCurrentPart());

        onSubbeatListener = new OnSubbeatListener() {
            @Override
            public void onSubbeat(int subbeat) {
                drumMachine.postInvalidate();
            }
        };

        getJam().addOnSubbeatListener(onSubbeatListener);

        return view;
    }

    public void onPause() {
        super.onPause();
        getJam().removeOnSubbeatListener(onSubbeatListener);
    }
}
