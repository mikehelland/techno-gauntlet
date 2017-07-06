package com.mikehelland.omgtechnogauntlet;

import android.media.SoundPool;

import com.mikehelland.omgtechnogauntlet.dsp.Dac;

import java.util.ArrayList;

/**
 * Created by m on 6/4/14.
 */
public class OMGSoundPool extends SoundPool {

    private boolean isLoaded = false;
    private boolean cancelLoading = false;

    private DialpadThread mDspThread = null;
    private boolean mIsDspRunning = false;
    private ArrayList<Dac> mDacs = new ArrayList<Dac>();

    public OMGSoundPool(int i1, int i2, int i3) {
        super(i1, i2, i3);

    }

    public void cancelLoading() {
        cancelLoading = true;
    }

    public void allowLoading() {
        cancelLoading = false;
    }

    public boolean isCanceled() {
        return cancelLoading;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean value) {
        isLoaded = value;
    }

    public void makeSureDspIsRunning() {
        if (!mIsDspRunning) {
            mDspThread = new DialpadThread(mDacs);
            mDspThread.start();
            mIsDspRunning = true;
        }
    }

    public void addDac(Dac dac) {
        mDacs.add(dac);
    }
}
