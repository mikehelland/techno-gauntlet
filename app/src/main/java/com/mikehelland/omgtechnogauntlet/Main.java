package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.OnGetSoundSetListener;
import com.mikehelland.omgtechnogauntlet.jam.OnSoundLoadedListener;
import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;
import com.mikehelland.omgtechnogauntlet.jam.SoundManager;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

public class Main extends Activity {

    Jam jam;

    BluetoothManager mBtf;
    DatabaseContainer mDatabase;

    private WelcomeFragment mWelcomeFragment;

    private BeatView mBeatView;

    private ImageLoader mImages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mBeatView = (BeatView)findViewById(R.id.main_beatview);

        mDatabase = new DatabaseContainer(Main.this);
        OnGetSoundSetListener getSoundSetFromDatabase = new OnGetSoundSetListener() {
            @Override
            public SoundSet onGetSoundSet(String url) {
                //todo what if it's not in the database? go online? gonna need a callback
                //although, it should only really affect external jam's loaded by URL (so download them then?)
                return mDatabase.getSoundSetData().getSoundSetByURL(url);
            }
        };

        OnSoundLoadedListener updateBeatViewWithLoadProgress = new OnSoundLoadedListener() {
            @Override
            public void onSoundLoaded(int howManyLoaded, int howManyTotal) {
                mBeatView.setLoadingStatus(howManyLoaded, howManyTotal);
                //todo this shouldn't be here because sounds may not need to be loaded
                FragmentManager fm = getFragmentManager();
                if (howManyLoaded >= howManyTotal && fm != null && fm.getBackStackEntryCount() == 0) {
                    mWelcomeFragment.animateFragment(new MainFragment(), 1);
                }
            }
        };

        SoundManager soundManager = new SoundManager(this, updateBeatViewWithLoadProgress);
        jam = new Jam(soundManager, getSoundSetFromDatabase);

        int defaultJam = BuildConfig.FLAVOR.equals("demo") ? R.string.demo_jam : R.string.default_jam;
        jam.loadFromJSON(getResources().getString(defaultJam));

        //todo does beatView even have to know about Jam?
        mBeatView.setJam(jam);

        jam.addOnSubbeatListener(new OnSubbeatListener() {
            @Override
            public void onSubbeat(int subbeat) {
                mBeatView.postInvalidate();
            }
        });

        //todo whats this jam.addPlayStatusChangeListener
        //jam.addInvalidateOnBeatListener(mBeatView);

        mBeatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jam.isPlaying()) {
                    jam.stop();
                }
                else {
                    jam.play();
                }
                mBeatView.postInvalidate();
            }
        });

        //todo setupBluetooth();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mWelcomeFragment == null) {
                    mWelcomeFragment = new WelcomeFragment();
                    mWelcomeFragment.setJam(jam);
                }
                try {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(R.id.main_layout, mWelcomeFragment);
                    ft.commit();
                } catch (Exception ignore) { }
            }
        }).start();

        mImages = new ImageLoader(this);
    }



    @Override
    public void onPause() {
        super.onPause();
        //todo relocate
        // if (!mPool.isLoaded())
        //    mPool.cancelLoading();
        if (jam.isPlaying()) {
            jam.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        jam.finish();
        //todo mBtf.cleanUp();
        mDatabase.close();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            Toast.makeText(this, "Can't back out now! \nHit the 'EXIT' button.", Toast.LENGTH_SHORT).show();

        } else {
            super.onBackPressed();
        }

    }

    /*private void setupBluetooth() {
        mBtf = new BluetoothManager(this);
        if (mBtf.isBlueToothOn()) {
            mBtf.startAccepting(makeConnectCallback());

            setupBluetoothJamCallback();
        }
    }

    private void setupBluetoothJamCallback() {

        mJamCallback = new Jam.StateChangeCallback() {

            @Override
            void newState(String state, Object... args) {
                if (state.equals("PLAY") || state.equals("STOP"))
                    mBtf.sendCommandToDevices(state, null);

                if (state.equals("ON_NEW_LOOP"))
                    mBtf.sendCommandToDevices(state, null);
            }

            @Override
            void onSubbeatLengthChange(int length, String source) {
                mBtf.sendNameValuePairToDevices(CommandProcessor.JAMINFO_SUBBEATLENGTH,
                        Integer.toString(length), source);
            }

            @Override
            void onKeyChange(int key, String source) {
                mBtf.sendNameValuePairToDevices(CommandProcessor.JAMINFO_KEY,
                        Integer.toString(key), source);
            }

            @Override
            void onScaleChange(String scale, String source) {
                mBtf.sendNameValuePairToDevices(CommandProcessor.JAMINFO_SCALE,
                        scale, source);
            }

            @Override
            void onChordProgressionChange(int[] chords) {

            }

            @Override
            void onNewPart(Part channel) {
                mBtf.sendCommandToDevices(CommandProcessor.getNewPartCommand(channel), null);
            }
            @Override
            void onPartEnabledChanged(Part channel, boolean enabled, String source) {
                mBtf.sendCommandToDevices(
                        CommandProcessor.getPartEnabledCommand(channel.getID(), enabled), source);
            }

            @Override
            void onPartVolumeChanged(Part channel, float volume, String source) {
                mBtf.sendCommandToDevices(
                        CommandProcessor.getPartVolumeCommand(channel.getID(), volume), source);
            }

            @Override
            void onPartPanChanged(Part channel, float pan, String source) {
                mBtf.sendCommandToDevices(
                        CommandProcessor.getPartPanCommand(channel.getID(), pan), source);
            }
        };
        mJam.addStateChangeListener(mJamCallback);
    }

    BluetoothConnectCallback makeConnectCallback() {
        return new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {}
            @Override
            public void onConnected(BluetoothConnection connection) {
                final CommandProcessor cp = new CommandProcessor(Main.this);
                cp.setup(connection, mJam, null);
                connection.setDataCallback(cp);
            }

            public void onDisconnected(final BluetoothConnection connection) {}
        };
    }
    */

    DatabaseContainer getDatabase() {return mDatabase;}
    ImageLoader getImages() {return mImages;}
}
