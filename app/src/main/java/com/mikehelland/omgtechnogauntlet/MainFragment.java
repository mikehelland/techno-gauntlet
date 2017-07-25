package com.mikehelland.omgtechnogauntlet;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainFragment extends OMGFragment {

    private View mView;

    private Button playButton;

    private Button dspMuteButton;

    private Button mKeyButton;
    private ChordsView mChordsButton;

    private ImageView mainLibenizHead;


    private View dspMonkeyHead;


    private Button bpmButton;

    private OMGHelper mOMGHelper;

    private LayoutInflater mInflater;

    private ArrayList<View> monkeyHeads = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.main_fragment,
                container, false);

        mInflater = inflater;

        setupPanels();

        setupDspPanel();

        setupSectionInfoPanel();

        setupMainControls();

        return mView;
    }

    private void setupPanels() {

        ViewGroup container = (ViewGroup)mView.findViewById(R.id.channel_list);
        //View controls;
        for (final Channel channel : mJam.getChannels()) {

            View controls = mInflater.inflate(R.layout.main_panel, container, false);
            container.addView(controls);

            Button button = ((Button)controls.findViewById(R.id.track_button));
            button.setText(channel.getSoundSetName());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SoundSetFragment f = new SoundSetFragment();
                    f.setJam(mJam, channel);
                    showFragmentRight(f);

                }
            });

            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    ChannelOptionsFragment f = new ChannelOptionsFragment();
                    f.setJam(mJam, channel);
                    showFragmentRight(f);

                    return false;
                }
            });

            final Button muteButton = (Button)controls.findViewById(R.id.mute_button);
            muteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    muteButton.setBackgroundColor(channel.toggleEnabled() ?
                            Color.GREEN : Color.RED);
                }
            });
            muteButton.setBackgroundColor(channel.enabled ?
                    Color.GREEN : Color.RED);
            muteButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    channel.clearNotes();
                    return true;
                }
            });

            View monkeyHead = controls.findViewById(R.id.libeniz_head);
            monkeyHeads.add(monkeyHead);
            monkeyHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    play();

                    mJam.monkeyWithChannel(channel);
                    Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                    view.startAnimation(turnin);

                }
            });

            controls.findViewById(R.id.open_fretboard_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String surfaceURL = channel.getSurfaceURL();

                    if (surfaceURL.equals("PRESET_SEQUENCER")) {
                        DrumFragment f = new DrumFragment();
                        f.setJam(mJam, channel);
                        showFragmentRight(f);
                    }
                    else {

                        GuitarFragment f = new GuitarFragment();
                        f.setJam(mJam, channel);
                        showFragmentRight(f);
                    }
                }
            });

            controls.findViewById(R.id.open_fretboard_button).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    SurfaceFragment f = new SurfaceFragment();
                    f.setJam(mJam, channel);
                    showFragmentRight(f);

                    return false;
                }
            });


        }
    }




    public void setupDspPanel() {

        View controls = mView.findViewById(R.id.dsp_controls);
        ((Button)(controls.findViewById(R.id.track_button))).setText("Oscillator");


        dspMuteButton = (Button) controls.findViewById(R.id.mute_button);
        dspMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(mJam.toggleMuteDsp() ?
                        Color.GREEN : Color.RED);
            }
        });

        dspMuteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mJam.getDialpadChannel().clearNotes();
                return true;
            }
        });

        dspMonkeyHead = controls.findViewById(R.id.libeniz_head);
        dspMonkeyHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
                //mJam.monkeyWithDsp();
                Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                view.startAnimation(turnin);
            }
        });

        controls.findViewById(R.id.track_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GuitarFragment f = new GuitarFragment();
                f.setJam(mJam, mJam.getDialpadChannel());
                showFragmentRight(f);
            }
        });

    }


    public void setupSectionInfoPanel() {

        mKeyButton = (Button)mView.findViewById(R.id.key_button);
        mKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                KeyFragment fragment = new KeyFragment();
                fragment.setJam(mJam, MainFragment.this);
                showFragmentRight(fragment);


            }
        });

        bpmButton = (Button)mView.findViewById(R.id.tempo_button);
        bpmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeatsFragment fragment = new BeatsFragment();
                fragment.setJam(mJam, MainFragment.this);
                showFragmentRight(fragment);

            }
        });

        mChordsButton = (ChordsView)mView.findViewById(R.id.chordprogression_button);
        mChordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChordsFragment fragment = new ChordsFragment();
                fragment.setJam(mJam, MainFragment.this);
                showFragmentRight(fragment);


            }
        });

        mChordsButton.setJam((Main)getActivity(), mJam);
        mJam.addInvalidateOnNewMeasureListener(mChordsButton);


    }

    public void showFragmentRight(Fragment f) {


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_right,
                R.animator.slide_out_left,
                R.animator.slide_in_left,
                R.animator.slide_out_right
        );
        ft.replace(R.id.main_layout, f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void showFragmentDown(Fragment f) {


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_down,
                R.animator.slide_out_up,
                R.animator.slide_in_up,
                R.animator.slide_out_down
                //R.anim.slide_in_up,
                //R.anim.slide_out_up,
                //R.anim.slide_in_down,
                //R.anim.slide_out_down
        );
        //ft.remove(MainFragment.this);
        ft.replace(R.id.main_layout, f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

    }

    public void showFragmentUp(Fragment f) {


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_left,
                R.animator.slide_out_right,
                R.animator.slide_in_right,
                R.animator.slide_out_left
                //R.anim.slide_in_down,
                //R.anim.slide_out_down,
                //R.anim.slide_in_up,
                //R.anim.slide_out_up
        );
        //ft.remove(MainFragment.this);
        ft.replace(R.id.main_layout, f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

    }

    private void setupMainControls() {


        mView.findViewById(R.id.add_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Channel channel = new Channel(getActivity(), mJam, mPool);

                mJam.addChannel(channel);

                SoundSetFragment f = new SoundSetFragment();
                f.setJam(mJam, channel);
                showFragmentRight(f);

            }
        });

        mView.findViewById(R.id.bt_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothConnectFragment f = new BluetoothConnectFragment();

                showFragmentDown(f);

            }
        });

        playButton = (Button)mView.findViewById(R.id.play_button);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mJam.isPlaying()) {
                    mJam.finish();
                    playButton.setText("Play");
                }
                else {
                    play();
                }
            }
        });

        Button mixerButton = (Button)mView.findViewById(R.id.mixer_button);
        mixerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixerFragment fragment = new MixerFragment();
                //fragment.setJam(mJam, MainFragment.this);
                showFragmentRight(fragment);

            }
        });

        mainLibenizHead = (ImageView)mView.findViewById(R.id.libeniz_head);
        mainLibenizHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
                mJam.monkeyWithEverything();
                Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                view.startAnimation(turnin);

                for (View monkeyhead : monkeyHeads) {
                    monkeyhead.startAnimation(turnin);
                }

                dspMonkeyHead.startAnimation(turnin);

                updateUI();
            }
        });

        setupMainBanana();

    }

    private void setupMainBanana() {

        final Button pointsButton = (Button) mView.findViewById(R.id.points_button);
        String pointsText = Integer.toString(PreferenceHelper.getPointCount(getActivity()));
        pointsButton.setText(pointsText);
        pointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedListFragment f = new SavedListFragment();
                f.setJam(MainFragment.this, mJam);
                showFragmentUp(f);
            }
        });

        final View savedPanel = mView.findViewById(R.id.saved_panel);

        final Button doneButton = (Button)savedPanel.findViewById(R.id.saved_done);
        final Button tagsButton = (Button)savedPanel.findViewById(R.id.saved_add_tags);
        final Button shareButton = (Button)savedPanel.findViewById(R.id.saved_share);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneButton.setVisibility(View.GONE);
                tagsButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);

                mOMGHelper.shareLastSaved();
            }
        });

        tagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTagsFragment f = new AddTagsFragment();
                f.setOMGHelper(mOMGHelper);
                showFragmentUp(f);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                doneButton.setVisibility(View.GONE);
                tagsButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);

            }
        });

        final ImageView mainBanana = (ImageView) mView.findViewById(R.id.main_banana);
        mainBanana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mOMGHelper = new OMGHelper(getActivity(), OMGHelper.Type.SECTION,
                        mJam.getData());
                mOMGHelper.submit();

                Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                view.startAnimation(turnin);

                String pointsText = Integer.toString(PreferenceHelper.dingPointCount(getActivity()));
                pointsButton.setText(pointsText);

                doneButton.setVisibility(View.VISIBLE);
                tagsButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);


            }
        });

    }


    private void play() {
        if (!mJam.isPlaying()) {
            mJam.kickIt();
            playButton.setText("Stop");
        }
    }

    public void onViewStateRestored(Bundle bundle) {
        super.onViewStateRestored(bundle);
        updateUI();

    }

    public void updateUI() {
        if (mChordsButton != null) {
            updateKeyUI();
            updateBPMUI();
            mChordsButton.invalidate();

            //todo make mute buttons work
            //bassMuteButton.setBackgroundColor(mJam.getBassChannel().enabled ?
            //        Color.GREEN : Color.RED);

            dspMuteButton.setBackgroundColor(mJam.getDialpadChannel().enabled ?
                    Color.GREEN : Color.RED);

            playButton.setText(mJam.isPlaying() ? "Stop" : "Play");

        }

    }

    public void updateBPMUI() {
        bpmButton.setText(Integer.toString(mJam.getBPM()) + " bpm");
    }

    public void updateKeyUI() {
        mKeyButton.setText(mJam.getKeyName());
    }
}

