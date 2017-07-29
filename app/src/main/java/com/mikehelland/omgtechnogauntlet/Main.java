package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Main extends Activity {

    Jam mJam;
    OMGSoundPool mPool = new OMGSoundPool(8, AudioManager.STREAM_MUSIC, 0);
    BluetoothFactory mBtf;
    Jam.StateChangeCallback mJamCallback;


    private WelcomeFragment mWelcomeFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO have such a good database I don't need to blow it away every time
        //deleteDatabase("OMG_TECHNO_GAUNTLET");
        //deleteDatabase("OMG_BANANAS");


        mBtf = new BluetoothFactory(this);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.main);

        mJamCallback = new Jam.StateChangeCallback() {

            @Override
            void onPlay() {
                mBtf.sendCommandToDevices("PLAY");
            }

            @Override
            void onStop() {
                mBtf.sendCommandToDevices("STOP");
            }
        };

        mJam = new Jam(this, mPool, mJamCallback);

        if (mWelcomeFragment == null) {
            mWelcomeFragment = new WelcomeFragment();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_layout, mWelcomeFragment);
        ft.commit();


        //headbob = new HeadBob((ImageView)mLibenizView.findViewById(R.id.libeniz_head));
        //headbob.start(500);

    }



    @Override
    public void onPause() {
        super.onPause();
        if (!mPool.isLoaded())
            mPool.cancelLoading();
        mJam.finish();
        mBtf.cleanUp();
    }


}
