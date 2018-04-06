package com.mikehelland.omgtechnogauntlet.jam;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.mikehelland.omgtechnogauntlet.jam.dsp.Dac;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SoundManager {

    private static final int LOADED_FILES_LIMIT = 800;

    private volatile boolean isLoading = false;

    private boolean isInitialized = false;
    private boolean isLoaded = false;
    private boolean cancelLoading = false;

    private OscillatorThread mDspThread = null;
    private boolean mIsDspRunning = false;
    private ArrayList<Dac> mDacs = new ArrayList<>();

    private SoundPool soundPool = new SoundPool(32, AudioManager.STREAM_MUSIC, 100);
    private OnSoundLoadedListener onSoundLoadedListener;

    private ConcurrentHashMap<String, Integer> loadedUrls = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, SoundSet.Sound> mSoundsToLoad = new ConcurrentHashMap<>();

    Runnable onAllLoadsFinishedCallback = null;

    private Context mContext;
    private int soundsToLoadThisTime = 0;
    private int soundsLoadedThisTime = 0;

    private boolean mShowedFileLimit = false;

    public SoundManager(Context context, final OnSoundLoadedListener onSoundLoadedListener) {
        mContext = context;
        this.onSoundLoadedListener = onSoundLoadedListener;

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundsLoadedThisTime++;
                onSoundLoadedListener.onSoundLoaded(soundsLoadedThisTime, soundsToLoadThisTime);
            }
        });
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

    void setLoaded() {
        isLoaded = true;
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
    void setInitialized() {
        isInitialized = true;
    }

    void addSoundToLoad(SoundSet.Sound sound) {
        if (!loadedUrls.containsKey(sound.getURL())) {
            if (!mSoundsToLoad.containsKey(sound.getURL())) {
                mSoundsToLoad.put(sound.getURL(), sound);
            }
        }
    }

    void loadSounds() {
        if (isLoading)
            return;

        isLoading = true;

        // used by the onLoadListener
        soundsToLoadThisTime = mSoundsToLoad.size();
        soundsLoadedThisTime = 0;

        if (soundsToLoadThisTime == 0) {
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
        for (SoundSet.Sound sound : mSoundsToLoad.values()) {

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

                    poolId = soundPool.load(mContext, preset_id, 1);
                }
                else {
                    path = mContext.getFilesDir() + "/" + Long.toString(sound.getSoundSetId()) + "/";
                    poolId = soundPool.load(path + Integer.toString(sound.getSoundSetIndex()), 1);
                }
                loadedUrls.put(sound.getURL(), poolId);
            }
        }
        mSoundsToLoad.clear();
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
        soundPool.release();
    }

    int playSound(PlaySoundCommand command) {
        if (command.osc != null && command.note != null) {
            makeSureDspIsRunning();
            return command.osc.playNote(command.note, false);
        }
        else {
            return soundPool.play(command.poolId, command.stereoVolume[0], command.stereoVolume[1],
                    10, 0, command.speed);
        }
    }

    void stopSound(PlaySoundCommand command) {
        if (command.osc != null) {
            command.osc.mute();
        }
        else if (command.note != null) {
            soundPool.stop(command.note.playingHandle);
        }
    }

    void stopSound(int handle) {
        soundPool.stop(handle);
    }
}
