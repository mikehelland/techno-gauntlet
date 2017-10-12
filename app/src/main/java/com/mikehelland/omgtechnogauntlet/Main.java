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
import android.widget.ProgressBar;
import android.widget.Toast;

public class Main extends Activity {

    Jam mJam;
    OMGSoundPool mPool = new OMGSoundPool(this, 32, AudioManager.STREAM_MUSIC, 100);
    //BluetoothFactory mBtf;
    BluetoothManager mBtf;
    Jam.StateChangeCallback mJamCallback;


    private WelcomeFragment mWelcomeFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO have such a good database I don't need to blow it away every time
        //deleteDatabase("OMG_TECHNO_GAUNTLET");
        //deleteDatabase("OMG_BANANAS");
        //deleteDatabase("OMG_SURFACES");
        //deleteDatabase("OMG_BT_DEVICE");

        //SoundPool.Builder builder = new SoundPool.Builder();
        //AudioAttributes aa = new AudioAttributes();
        //AudioAttributes.

        //mBtf = new BluetoothFactory(this);
        mBtf = new BluetoothManager(this);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.main);

        mJamCallback = new Jam.StateChangeCallback() {

            @Override
            void onPlay() {
                mBtf.sendCommandToDevices("PLAY", null);
            }

            @Override
            void onStop() {
                mBtf.sendCommandToDevices("STOP", null);
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
        };

        mJam = new Jam(this, mPool, mJamCallback);

        if (mWelcomeFragment == null) {
            mWelcomeFragment = new WelcomeFragment();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_layout, mWelcomeFragment);
        ft.commit();

        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.loading_progress);
        mPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                if (progressBar.getVisibility() == View.INVISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(mPool.soundsToLoad);
                }

                progressBar.incrementProgressBy(1);

                mPool.soundsToLoad--;
                if (mPool.soundsToLoad <= 0) {

                    mPool.setLoaded(true);
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.INVISIBLE);

                    if (!mPool.isCanceled() && mPool.onAllLoadsFinishedCallback != null)
                        mPool.onAllLoadsFinishedCallback.run();
                }
            }
        });

    }



    @Override
    public void onPause() {
        super.onPause();
        Log.d("MGH Main", "onPause");
        if (!mPool.isLoaded())
            mPool.cancelLoading();
        mJam.finish();
        mBtf.cleanUp();
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            Toast.makeText(this, "Can't back out now! \nHit the 'OUT' button.", Toast.LENGTH_LONG).show();

        } else {
            super.onBackPressed();
            //getFragmentManager().popBackStack();
        }

    }
}
