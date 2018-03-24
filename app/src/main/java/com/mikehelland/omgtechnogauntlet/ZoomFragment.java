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
public class ZoomFragment extends OMGFragment {

    private Jam mJam;
    private ZoomVerticalView guitarView = null;
    private Channel mChannel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.zoom_fragment,
                container, false);

        guitarView = (ZoomVerticalView)view.findViewById(R.id.guitarfrets);

        if (mJam != null)
            guitarView.setJam(mJam, mChannel);


        return view;
    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        mChannel = channel;

        if (guitarView != null) {
            guitarView.setJam(mJam, mChannel);
        }
    }


    public void onPause() {
        super.onPause();
        //mJam.removeInvalidateOnBeatListener(guitarView);
    }
}
