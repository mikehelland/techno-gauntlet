package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/17/18.
 */

public class SequencerTrack {

    private int index = 0;

    private String name = "";
    AudioParameters audioParameters = new AudioParameters();
    private boolean[] data = new boolean[512];
    int poolId = -1;

    public String getName() {return name;}
    public boolean isMuted() {return audioParameters.mute;}
    boolean[] getData() {return data;}

    SequencerTrack(String name, int index) {
        this.name = name;
        this.index = index;
    }

    void setData(boolean[] data) {
        this.data = data;
    }

    void toggleMute() {
        audioParameters.mute = !audioParameters.mute;
    }

    void setMute(boolean b) {
        audioParameters.mute = b;
    }

    void setPan(float pan) {audioParameters.pan = pan;}
    public float getPan() {return audioParameters.pan;}

    void setVolume(float volume) {audioParameters.volume = volume;}
    public float getVolume() {return audioParameters.volume;}

    public float getSpeed() {
        return audioParameters.speed;
    }

    public int getIndex() {
        return index;
    }
}
