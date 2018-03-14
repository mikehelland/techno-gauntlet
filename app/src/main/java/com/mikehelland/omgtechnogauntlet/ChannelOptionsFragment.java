package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;

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

        mView.findViewById(R.id.clear_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChannel.clearNotes();
                getActivity().getFragmentManager().popBackStack();
            }
        });

        mView.findViewById(R.id.copy_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJam.copyChannel(mChannel);
                getActivity().getFragmentManager().popBackStack();
            }
        });

        View rbSequencer = mView.findViewById(R.id.radioButton);
        if (mChannel.getSurfaceURL().equals("PRESET_SEQUENCER")) {
            ((RadioButton)rbSequencer).toggle();
        }
        rbSequencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChannel.setSurface(new Surface("PRESET_SEQUENCER"));
                getActivity().getFragmentManager().popBackStack();
            }
        });
        View rbVertical = mView.findViewById(R.id.radioButton2);
        if (mChannel.getSurfaceURL().equals("PRESET_VERTICAL")) {
            ((RadioButton)rbVertical).toggle();
        }
        rbVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChannel.setSurface(new Surface("PRESET_VERTICAL"));
                getActivity().getFragmentManager().popBackStack();
            }
        });
        View rbFretboard = mView.findViewById(R.id.radioButton3);
        if (mChannel.getSurfaceURL().equals("PRESET_FRETBOARD")) {
            ((RadioButton)rbFretboard).toggle();
        }
        rbFretboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChannel.setSurface(new Surface("PRESET_FRETBOARD"));
                getActivity().getFragmentManager().popBackStack();
            }
        });

        final Button resetButton = (Button)mView.findViewById(R.id.reset_sample_speed);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButton.setText("100%");
                mChannel.setSampleSpeed(1);
            }
        });

        SeekBar speedBar = (SeekBar)mView.findViewById(R.id.channel_speed_seekbar);
        speedBar.setMax(200);
        float speed = 100 * mChannel.getSampleSpeed();
        speedBar.setProgress((int)speed);
        resetButton.setText(Math.round(mChannel.getSampleSpeed() * 100) + "% - Press to Reset");

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    float newSpeed = i / 100.0f;
                    resetButton.setText(Math.round(newSpeed * 100) + "% - Press to Reset");
                    if (newSpeed > 0) {
                        mChannel.setSampleSpeed(newSpeed);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        mView.findViewById(R.id.zoom_vertical_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();

                GuitarFragment f = new GuitarFragment();
                f.setJam(mJam, mChannel);
                f.setZoomModeOn();
                showFragmentRight(f);
            }
        });


    }
}
