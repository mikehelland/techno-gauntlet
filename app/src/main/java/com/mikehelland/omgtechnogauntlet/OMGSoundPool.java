package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.media.SoundPool;

import com.mikehelland.omgtechnogauntlet.dsp.Dac;

import java.util.ArrayList;
import java.util.HashMap;

class OMGSoundPool extends SoundPool {

    private boolean isLoaded = false;
    private boolean cancelLoading = false;

    private OscillatorThread mDspThread = null;
    private boolean mIsDspRunning = false;
    private ArrayList<Dac> mDacs = new ArrayList<>();

    private HashMap<String, Integer> loadedUrls = new HashMap<>();

    OMGSoundPool(int i1, int i2, int i3) {
        super(i1, i2, i3);

    }

    int load(String url, Context context, int resource, int p) {
        if (loadedUrls.containsKey(url))
            return loadedUrls.get(url);

        int newId = super.load(context, resource, p);
        loadedUrls.put(url, newId);
        return newId;
    }

    int load(String url, String path, int p) {

        if (loadedUrls.containsKey(url))
            return loadedUrls.get(url);

        int newId = super.load(path, p);
        loadedUrls.put(url, newId);
        return newId;
    }

    void cancelLoading() {
        cancelLoading = true;
    }

    void allowLoading() {
        cancelLoading = false;
    }

    boolean isCanceled() {
        return cancelLoading;
    }

    boolean isLoaded() {
        return isLoaded;
    }

    void setLoaded(boolean value) {
        isLoaded = value;
    }

    void makeSureDspIsRunning() {
        if (!mIsDspRunning) {
            mDspThread = new OscillatorThread(mDacs);
            mDspThread.start();
            mIsDspRunning = true;
        }
    }

    void addDac(Dac dac) {
        mDacs.add(dac);
    }
}
