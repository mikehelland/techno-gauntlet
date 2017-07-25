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
public class ChannelOptionsFragment extends OMGFragment {

    private Jam mJam;
    private View mView;
    private MainFragment mainFragment;
    private Channel mChannel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.channel_options,
                container, false);

        if (mJam != null)
            setup();

        return mView;
    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        mChannel = channel;

        if (mView != null)
            setup();
    }

    public void setup() {

        mView.findViewById(R.id.remove_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mJam.getChannels().remove(mChannel);

                getActivity().getFragmentManager().popBackStack();
            }
        });

    }
}
