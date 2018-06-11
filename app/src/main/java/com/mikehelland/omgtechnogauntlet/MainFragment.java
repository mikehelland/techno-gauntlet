package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.ActivityManager;
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

import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnBeatChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnKeyChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

import java.util.HashMap;

public class MainFragment extends OMGFragment {

    private View mView;

    private Button mKeyButton;
    private ChordsView mChordsButton;

    private Button bpmButton;

    private LayoutInflater mInflater;
    private ViewGroup mContainer;

    private OnJamChangeListener mJamListener;
    private OnKeyChangeListener mKeyListener;
    private OnBeatChangeListener mBeatListener;

    private HashMap<JamPart, View> channelViewMap = new HashMap<>();

    private boolean mLeavingNow = false;

    private int mColorRed = Color.argb(128, 255, 0, 0);
    private int mColorGreen = Color.argb(128, 0, 255, 0);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        for (JamPart channel : getJam().getParts()) {
            setupPanel(channel);
        }
    }

    private void setupPanel(final JamPart part) {

        View controls = mInflater.inflate(R.layout.main_panel, mContainer, false);
        channelViewMap.put(part, controls);

        // the -1 keeps the add channel button on the bottom
        mContainer.addView(controls, mContainer.getChildCount() - 1);

        Button button = ((Button)controls.findViewById(R.id.track_button));
        String name = part.getName();
        int paransIndex = name.indexOf(" (");
        if (paransIndex > -1) {
            name = name.substring(0, paransIndex);
        }
        button.setText(name);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (!part.isValid()) {
                return;
            }

            if (part.useSequencer()) {
                DrumFragment f = new DrumFragment();
                f.setPart(part);
                animateFragment(f, 0);
            }
            else {
                GuitarFragment f = new GuitarFragment();
                f.setPart(part);
                animateFragment(f, 0);
            }
            }
        });


        final Button muteButton = (Button)controls.findViewById(R.id.mute_button);
        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean newMute = !part.getMute();
                getJam().setPartMute(part, newMute, null);
                muteButton.setBackgroundColor(newMute ? mColorRed : mColorGreen);
            }
        });
        muteButton.setBackgroundColor(!part.getMute() ?
                mColorGreen : mColorRed);
        muteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                getJam().clearPart(part);
                return true;
            }
        });

        controls.findViewById(R.id.options_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!part.isValid()) {
                    return;
                }

                PartOptionsFragment f = new PartOptionsFragment();
                f.setPart(part);
                animateFragment(f, 0);
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
                animateFragment(fragment, 0);
            }
        });

        bpmButton = (Button)mView.findViewById(R.id.tempo_button);
        bpmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ActivityManager.isUserAMonkey()) return;
                BeatsFragment fragment = new BeatsFragment();
                animateFragment(fragment, 0);
            }
        });

        mChordsButton = (ChordsView)mView.findViewById(R.id.chordprogression_button);
        mChordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (ActivityManager.isUserAMonkey()) return;
                ChordsFragment fragment = new ChordsFragment();
                animateFragment(fragment, 0);
            }
        });

        mChordsButton.setJam(getJam());
        getJam().addOnSubbeatListener(new OnSubbeatListener() { //todo remove it?
            @Override
            public void onSubbeat(int subbeat) {
                mChordsButton.postInvalidate();
            }
        });

        updateUI();
    }

    @Override
    protected void animateFragment(OMGFragment f, int direction) {
        if (mLeavingNow)
            return;

        mLeavingNow = true;
        super.animateFragment(f, direction);
    }

    private void setupMainControls() {


        mView.findViewById(R.id.add_channel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.FLAVOR.equals("demo") && getJam().getParts().size() >= 4) {
                    if (ActivityManager.isUserAMonkey()) return;
                    animateFragment(new UpgradeFragment(), 0);
                    return;
                }

                SoundSetFragment f = new SoundSetFragment();
                f.setCallback(new SoundSetFragment.ChoiceCallback() {
                    void onChoice(SoundSet soundSet) {
                        getJam().newPart(soundSet);
                    }
                });
                animateFragment(f, 0);

            }
        });

        mView.findViewById(R.id.bt_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityManager.isUserAMonkey()) return;

                OMGFragment f = new BluetoothFragment();
                animateFragment(f, 1);
            }
        });


        Button mixerButton = (Button)mView.findViewById(R.id.mixer_button);
        mixerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getJam().getParts().size() == 0) {
                    Activity context = getActivity();
                    if (context != null) {
                        Toast.makeText(context, "Add Parts before you mix them!", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                MixerFragment fragment = new MixerFragment();
                animateFragment(fragment, 0);

            }
        });
        mView.findViewById(R.id.speed_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getJam().getParts().size() == 0) {
                    Activity context = getActivity();
                    if (context != null) {
                        Toast.makeText(context, "Add Parts before you mix them!", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                SampleSpeedFragment fragment = new SampleSpeedFragment();
                animateFragment(fragment, 0);
            }
        });

        setupUploadButton();

    }

    private void setupUploadButton() {

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
                animateFragment(new AddTagsFragment(), 3);
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
        bpmButton.setText(String.format("%s bpm", Integer.toString(getJam().getBPM())));
    }

    public void updateKeyUI() {
        mKeyButton.setText(getJam().getKeyName());
    }

    private void saveJam(boolean share){
        Activity activity = getActivity(); if (activity == null)  return;

        OMGHelper omgHelper = new OMGHelper(activity, getJam());
        omgHelper.submit(share);

    }

    private void setupJamStateListener() {

        mBeatListener = new OnBeatChangeListener() {
            @Override
            public void onSubbeatLengthChange(int length, String source) {
                Activity activity = getActivity();
                if (activity == null) return;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBPMUI();
                    }
                });
            }
        };

        mKeyListener = new OnKeyChangeListener() {
            @Override
            public void onKeyChange(int key, String source) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateKeyUI();
                    }
                });
            }

            @Override
            public void onScaleChange(int[] scale, String source) {
                Activity activity = getActivity(); if (activity == null)  return;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateKeyUI();
                    }
                });
            }
        };

        mJamListener = new OnJamChangeListener() {

            @Override
            public void onChordProgressionChange(int[] chords) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateChordsUI();
                        }
                    });
            }
            @Override
            public void onNewPart(final JamPart channel) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupPanel(channel);
                        }
                    });
            }
            @Override
            public void onPartEnabledChanged(final JamPart channel, final boolean enabled, String source) {
                Activity activity = getActivity(); if (activity == null)  return;
                activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View panel = channelViewMap.get(channel);
                            if (panel != null)
                                panel.findViewById(R.id.mute_button).setBackgroundColor(
                                        enabled ? mColorGreen : mColorRed);
                        }
                    });
            }

            @Override public void onPartVolumeChanged(JamPart channel, float volume, String source) {}
            @Override public void onPartPanChanged(JamPart channel, float pan, String source) {}
            @Override public void onPlay(final String source) { }
            @Override public void onStop(final String source) { }
            @Override public void onNewLoop(final String source) { }
            @Override public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) { }
            @Override public void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source) { }
            @Override public void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) { }
            @Override public void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source) { }
            @Override public void onPartEndLiveNotes(JamPart jamPart, String source) { }

        };

        getJam().addOnJamChangeListener(mJamListener);
        getJam().addOnKeyChangeListener(mKeyListener);
        getJam().addOnBeatChangeListener(mBeatListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getJam().removeOnJamChangeListener(mJamListener);
        getJam().removeOnKeyChangeListener(mKeyListener);
        getJam().removeOnBeatChangeListener(mBeatListener);
    }
}

