package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import com.mikehelland.omgtechnogauntlet.dsp.Dac;

import java.util.ArrayList;
import java.util.HashMap;

class OMGSoundPool extends SoundPool {

    private static final int LOADED_FILES_LIMIT = 800;

    private volatile boolean isLoading = false;

    private boolean isInitialized = false;
    private boolean isLoaded = false;
    private boolean cancelLoading = false;

    private OscillatorThread mDspThread = null;
    private boolean mIsDspRunning = false;
    private ArrayList<Dac> mDacs = new ArrayList<>();

    private HashMap<String, Integer> loadedUrls = new HashMap<>();

    private ArrayList<SoundSet.Sound> mSoundsToLoad = new ArrayList<>();

    Runnable onAllLoadsFinishedCallback = null;

    private Context mContext;
    int soundsToLoad = 0;

    private boolean mShowedFileLimit = false;

    OMGSoundPool(Context context, int i1, int i2, int i3) {
        super(i1, i2, i3);
        mContext = context;

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


    boolean isInitialized() {
        return isInitialized;
    }
    void setInitialized(boolean value) {
        isInitialized = value;
    }

    void addSoundToLoad(SoundSet.Sound sound) {
        if (!loadedUrls.containsKey(sound.getURL())) {
            if (!mSoundsToLoad.contains(sound)) {
                mSoundsToLoad.add(sound);
            }
        }
    }

    void loadSounds() {
        if (isLoading)
            return;

        isLoading = true;

        // used by the onLoadListener
        soundsToLoad = mSoundsToLoad.size();

        if (soundsToLoad == 0) {
            isLoading = false;

            // because the listener won't fire without sounds to load
            if (onAllLoadsFinishedCallback != null) {
                onAllLoadsFinishedCallback.run();
            }
            return;
        }

        String path;
        int preset_id;
        int poolId;
        SoundSet.Sound sound;
        while (mSoundsToLoad.size() > 0) {
            sound = mSoundsToLoad.get(0);

            if (sound == null) {
                break; //something is wrong
            }

            if (loadedUrls.size() > LOADED_FILES_LIMIT) {
                if (!mShowedFileLimit) {
                    Log.e("MGH OMGSoundPool", "file limit!");
                    mShowedFileLimit = true;
                }
                //todo show to user
                //todo release files?
            }
            else if (!loadedUrls.containsKey(sound.getURL())) {
                if (sound.isPreset()) {
                    preset_id = sound.getPresetId();

                    poolId = load(mContext, preset_id, 1);
                }
                else {
                    path = mContext.getFilesDir() + "/" + Long.toString(sound.getSoundSetId()) + "/";
                    poolId = load(path + Integer.toString(sound.getSoundSetIndex()), 1);
                }
                loadedUrls.put(sound.getURL(), poolId);
            }

            mSoundsToLoad.remove(0);
        }
        isLoading = false;
    }

    int getPoolId(String url) {
        if (!loadedUrls.containsKey(url))
            return -1;

        return loadedUrls.get(url);
    }

    void cleanUp() {
        if (mIsDspRunning) {
            mDspThread.interrupt();
        }
        this.release();
    }
}
