package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.media.SoundPool;

import com.mikehelland.omgtechnogauntlet.dsp.Dac;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by m on 6/4/14.
 */
public class OMGSoundPool extends SoundPool {

    private boolean isLoaded = false;
    private boolean cancelLoading = false;

    private OscillatorThread mDspThread = null;
    private boolean mIsDspRunning = false;
    private ArrayList<Dac> mDacs = new ArrayList<Dac>();

    private HashMap<String, Integer> loadedUrls = new HashMap();

    public OMGSoundPool(int i1, int i2, int i3) {
        super(i1, i2, i3);

    }

    public int load(String url, Context context, int resource, int p) {
        if (loadedUrls.containsKey(url))
            return loadedUrls.get(url);

        int newId = super.load(context, resource, p);
        loadedUrls.put(url, newId);
        return newId;
    }

    public int load(String url, String path, int p) {

        if (loadedUrls.containsKey(url))
            return loadedUrls.get(url);

        int newId = super.load(path, p);
        loadedUrls.put(url, newId);
        return newId;
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
            mDspThread = new OscillatorThread(mDacs);
            mDspThread.start();
            mIsDspRunning = true;
        }
    }

    public void addDac(Dac dac) {
        mDacs.add(dac);
    }
}
