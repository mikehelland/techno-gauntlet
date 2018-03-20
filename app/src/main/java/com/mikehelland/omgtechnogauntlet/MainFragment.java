package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainFragment extends OMGFragment {

    private View mView;

    private Button mKeyButton;
    private ChordsView mChordsButton;

    private Button bpmButton;

    private LayoutInflater mInflater;
    private ViewGroup mContainer;

    private List<View> monkeyHeads = new ArrayList<>();

    private Jam.StateChangeCallback mJamListener;

    private HashMap<Channel, View> channelViewMap = new HashMap<>();

    private boolean mLeavingNow = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mLeavingNow = false;

        mView = inflater.inflate(R.layout.main_fragment,
                container, false);

        mInflater = inflater;

        setupPanels();
        setupSectionInfoPanel();
        setupMainControls();
        setupJamStateListener();

        return mView;
    }

    private void setupPanels() {

        mContainer = (ViewGroup) mView.findViewById(R.id.channel_list);
        //View controls;
        for (Channel channel : mJam.getChannels()) {
            setupPanel(channel);
        }
    }

    private void setupPanel(final Channel channel) {

        View controls = mInflater.inflate(R.layout.main_panel, mContainer, false);
        channelViewMap.put(channel, controls);

        // the -1 keeps the add channel button on the bottom
        mContainer.addView(controls, mContainer.getChildCount() - 1);

        Button button = ((Button)controls.findViewById(R.id.track_button));
        String name = channel.getSoundSetName();
        int paransIndex = name.indexOf(" (");
        if (paransIndex > -1) {
            name = name.substring(0, paransIndex);
        }
        button.setText(name);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (channel.getSoundSet().getURL().length() == 0 ||
                    !channel.getSoundSet().isValid()) {
                return;
            }

            if (channel.useSequencer()) {
                DrumFragment f = new DrumFragment();
                f.setJam(mJam, channel);
                animateFragment(f, 0);
            }
            else {
                GuitarFragment f = new GuitarFragment();
                f.setJam(mJam, channel);
                animateFragment(f, 0);
            }
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (channel.getSoundSet().getURL().length() == 0 ||
                        !channel.getSoundSet().isValid()) {
                    return false;
                }

                ChannelOptionsFragment f = new ChannelOptionsFragment();
                f.setJam(mJam, channel);
                animateFragment(f, 0);

                return true;
            }
        });

        final Button muteButton = (Button)controls.findViewById(R.id.mute_button);
        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                muteButton.setBackgroundColor(mJam.toggleChannelEnabled(channel) ?
                        Color.GREEN : Color.RED);
            }
        });
        muteButton.setBackgroundColor(channel.isEnabled() ?
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

                Activity activity = getActivity(); if (activity == null)  return;
                Animation turnin = AnimationUtils.loadAnimation(activity, R.anim.rotate);
                view.startAnimation(turnin);

            }
        });

        controls.findViewById(R.id.options_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SoundSetFragment f = new SoundSetFragment();
                f.setJam(mJam, channel);
                animateFragment(f, 0);

            }
        });

        controls.findViewById(R.id.options_button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (channel.getSoundSet().getURL().length() == 0 ||
                        !channel.getSoundSet().isValid()) {
                    return false;
                }

                ChannelOptionsFragment f = new ChannelOptionsFragment();
                f.setJam(mJam, channel);
                animateFragment(f, 0);

                return true;

            }
        });


    }

    public void setupSectionInfoPanel() {

        mKeyButton = (Button)mView.findViewById(R.id.key_button);
        mKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ActivityManager.isUserAMonkey()) return;

                KeyFragment fragment = new KeyFragment();
                fragment.setJam(mJam, MainFragment.this);
                animateFragment(fragment, 0);
            }
        });

        bpmButton = (Button)mView.findViewById(R.id.tempo_button);
        bpmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ActivityManager.isUserAMonkey()) return;

                BeatsFragment fragment = new BeatsFragment();
                fragment.setJam(mJam, MainFragment.this);
                animateFragment(fragment, 0);
            }
        });

        mChordsButton = (ChordsView)mView.findViewById(R.id.chordprogression_button);
        mChordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ActivityManager.isUserAMonkey()) return;

                ChordsFragment fragment = new ChordsFragment();
                fragment.setJam(mJam);
                animateFragment(fragment, 0);
            }
        });

        mChordsButton.setJam(mJam);
        mJam.addInvalidateOnNewMeasureListener(mChordsButton);

        updateUI();
    }

    @Override
    protected void animateFragment(Fragment f, int direction) {
        if (mLeavingNow)
            return;

        mLeavingNow = true;
        super.animateFragment(f, direction);
    }

    private void setupMainControls() {


        mView.findViewById(R.id.add_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.FLAVOR.equals("demo") && mJam.getChannels().size() >= 4) {
                    if (ActivityManager.isUserAMonkey()) return;
                    Fragment f = new UpgradeFragment();
                    animateFragment(f, 0);
                    return;
                }

                final Channel channel = new Channel( mJam, mPool);

                SoundSetFragment f = new SoundSetFragment();
                f.setJam(mJam, channel);
                f.setCallback(new SoundSetFragment.ChoiceCallback() {
                    void onChoice(SoundSet soundSet) {
                        mJam.addChannel(channel);
                        mBtf.sendCommandToDevices(CommandProcessor.getNewChannelCommand(channel), null);
                    }
                });
                animateFragment(f, 0);

            }
        });

        mView.findViewById(R.id.bt_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityManager.isUserAMonkey()) return;

                Fragment f = new BluetoothBrainFragment();
                animateFragment(f, 1);
            }
        });


        Button mixerButton = (Button)mView.findViewById(R.id.mixer_button);
        mixerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mJam.getChannels().size() == 0) {
                    Activity context = getActivity();
                    if (context != null) {
                        Toast.makeText(context, "Add Channels before you mix them!", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                MixerFragment fragment = new MixerFragment();
                //fragment.setJam(mJam, MainFragment.this);
                animateFragment(fragment, 0);

            }
        });
        mixerButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mJam.getChannels().size() == 0) {
                    Activity context = getActivity();
                    if (context != null) {
                        Toast.makeText(context, "Add Channels before you mix them!", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                SampleSpeedFragment fragment = new SampleSpeedFragment();
                //fragment.setJam(mJam, MainFragment.this);
                animateFragment(fragment, 0);
                return true;
            }
        });

        ImageView mainLibenizHead = (ImageView)mView.findViewById(R.id.libeniz_head);
        mainLibenizHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
                mJam.monkeyWithEverything();

                Activity activity = getActivity(); if (activity == null)  return;
                Animation turnin = AnimationUtils.loadAnimation(activity, R.anim.rotate);
                view.startAnimation(turnin);

                for (View monkeyhead : monkeyHeads) {
                    monkeyhead.startAnimation(turnin);
                }

                //updateUI();
            }
        });

        setupMainBanana();

    }

    private void setupMainBanana() {

        final View savedPanel = mView.findViewById(R.id.saved_panel);

        final Button doneButton = (Button)savedPanel.findViewById(R.id.saved_done);
        final Button tagsButton = (Button)savedPanel.findViewById(R.id.saved_add_tags);
        final Button shareButton = (Button)savedPanel.findViewById(R.id.saved_share);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveJam(true);

                doneButton.setVisibility(View.GONE);
                tagsButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);

            }
        });

        tagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f = new AddTagsFragment();
                animateFragment(f, 3);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveJam(false);

                doneButton.setVisibility(View.GONE);
                tagsButton.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);

            }
        });

        final ImageView mainBanana = (ImageView) mView.findViewById(R.id.main_banana);
        mainBanana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }

                Animation turnin = AnimationUtils.loadAnimation(activity, R.anim.rotate);
                view.startAnimation(turnin);

                doneButton.setVisibility(View.VISIBLE);
                tagsButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);


            }
        });

    }


    private void play() {
        if (!mJam.isPlaying()) {
            mJam.kickIt();
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
            updateChordsUI();
        }
    }

    private void updateChordsUI() {
        mChordsButton.invalidate();
    }

    public void updateBPMUI() {
        bpmButton.setText(String.format("%s bpm", Integer.toString(mJam.getBPM())));
    }

    public void updateKeyUI() {
        mKeyButton.setText(mJam.getKeyName());
    }

    private void saveJam(boolean share){
        Activity activity = getActivity(); if (activity == null)  return;

        OMGHelper omgHelper = new OMGHelper(activity, mJam);
        omgHelper.submit(share);

    }

    private void setupJamStateListener() {
        mJamListener = new Jam.StateChangeCallback() {

            @Override
            void newState(final String state, Object... args) {
            }


            @Override
            void onSubbeatLengthChange(int length, String source) {
                Activity activity = getActivity(); if (activity == null)  return;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBPMUI();
                    }
                });
            }

            @Override
            void onKeyChange(int key, String source) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateKeyUI();
                        }
                    });
            }

            @Override
            void onScaleChange(String scale, String source) {
                Activity activity = getActivity(); if (activity == null)  return;

                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateKeyUI();
                        }
                    });
            }

            @Override
            void onChordProgressionChange(int[] chords) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateChordsUI();
                        }
                    });
            }
            @Override
            void onNewChannel(final Channel channel) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupPanel(channel);
                        }
                    });
            }
            @Override
            void onChannelEnabledChanged(final Channel channel, final boolean enabled, String source) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View panel = channelViewMap.get(channel);
                            if (panel != null)
                                panel.findViewById(R.id.mute_button).setBackgroundColor(
                                        enabled ? Color.GREEN : Color.RED);
                        }
                    });
            }

            @Override
            void onChannelVolumeChanged(Channel channel, float volume, String source) {}
            @Override
            void onChannelPanChanged(Channel channel, float pan, String source) {}
        };

        mJam.addStateChangeListener(mJamListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mJam.removeStateChangeListener(mJamListener);
    }
}

