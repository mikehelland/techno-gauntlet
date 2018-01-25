package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

    Jam mJam;
    OMGSoundPool mPool = new OMGSoundPool(this, 32, AudioManager.STREAM_MUSIC, 100);
    BluetoothManager mBtf;
    Jam.StateChangeCallback mJamCallback;
    DatabaseContainer mDatabase;

    private WelcomeFragment mWelcomeFragment;

    private BeatView mBeatView;
    private TextView mBeatTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = new DatabaseContainer(this);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.main);

        mJam = new Jam(this, mPool);

        mBeatView = (BeatView)findViewById(R.id.main_beatview);
        mBeatView.setJam(mJam);
        mJam.addInvalidateOnBeatListener(mBeatView);
        mBeatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mJam.isPlaying()) {
                    mJam.pause();
                }
                else {
                    mJam.kickIt();
                }
            }
        });

        setupBluetooth();

        if (mWelcomeFragment == null) {
            mWelcomeFragment = new WelcomeFragment();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_layout, mWelcomeFragment);
        ft.commit();

        mPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                if (!mBeatView.isShowingLoadProgress()) {
                    mBeatView.showLoadProgress(mPool.soundsToLoad);
                }
                mBeatView.incrementProgress();

                mPool.soundsToLoad--;
                if (mPool.soundsToLoad <= 0) {

                    mPool.setLoaded(true);

                    if (!mPool.isCanceled() && mPool.onAllLoadsFinishedCallback != null)
                        mPool.onAllLoadsFinishedCallback.run();
                }
            }
        });

        Toast.makeText(this, "Press the MONKEY to Change!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Press the BANANA to Save!", Toast.LENGTH_LONG).show();
    }



    @Override
    public void onPause() {
        super.onPause();
        Log.d("MGH Main", "onPause");
        if (!mPool.isLoaded())
            mPool.cancelLoading();
        mJam.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MGH MAIN", "onDestroy");
        mJam.finish();
        mBtf.cleanUp();
        mDatabase.close();
        mPool.cleanUp();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            Toast.makeText(this, "Can't back out now! \nHit the 'EXIT' button.", Toast.LENGTH_SHORT).show();

        } else {
            super.onBackPressed();
        }

    }

    private void setupBluetooth() {
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
            void onNewChannel(Channel channel) {
                mBtf.sendCommandToDevices(CommandProcessor.getNewChannelCommand(channel), null);
            }
            @Override
            void onChannelEnabledChanged(int channelNumber, boolean enabled, String source) {
                mBtf.sendCommandToDevices(
                        CommandProcessor.getChannelEnabledCommand(channelNumber, enabled), source);
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

    void loadJam(String json) {

        int backstack = getFragmentManager().getBackStackEntryCount();
        while (backstack > 0) {
            getFragmentManager().popBackStack();
            backstack--;
        }

        final Jam jam = new Jam(this, mPool);
        jam.load(json);
        if (mJamCallback != null)
            jam.addStateChangeListener(mJamCallback);

        final Runnable callback = new Runnable() {
            @Override
            public void run() {

                Jam oldJam = mJam;
                mJam = jam;
                mJam.addInvalidateOnBeatListener(mBeatView);
                mBeatView.setJam(mJam);

                mPool.loadSounds();
                jam.loadSoundSets();

                //pretty lousy spot for this
                CommandProcessor cp;
                for (BluetoothConnection connection : mBtf.getConnections()) {
                    cp = new CommandProcessor(Main.this);
                    cp.setup(connection, jam, null);
                    connection.setDataCallback(cp);
                }
                oldJam.pause();
                oldJam.finish();
                mJam.kickIt();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.run();
            }
        }).start();

    }

    DatabaseContainer getDatabase() {return mDatabase;}
}
