package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikehelland.omgtechnogauntlet.jam.Part;

public class SampleSpeedFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.samplespeed_fragment,
                container, false);

        setupPanels(inflater);

        return mView;
    }

    void setupPanels(LayoutInflater inflater) {


        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        View controls;
        int i = 0;
        for (final Part part : getJam().getParts()) {

            controls = inflater.inflate(R.layout.samplespeed_panel, container, false);
            container.addView(controls);

            SampleSpeedView mixerView = (SampleSpeedView) controls.findViewById(R.id.levels_view);
            mixerView.setJam(getJam(), part, part.getName());
            i++;
        }

    }

}

