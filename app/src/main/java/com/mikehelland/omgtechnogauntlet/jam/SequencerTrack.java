package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/17/18.
 */

public class SequencerTrack {
    private String name = "";
    AudioParameters audioParameters;
    private boolean[] data = new boolean[512];

    String getName() {return name;}
    boolean isMuted() {return audioParameters.mute;}
    boolean[] getData() {return data;}

    SequencerTrack(String name) {
        this.name = name;
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
    float getPan() {return audioParameters.pan;}

    void setVolume(float volume) {audioParameters.volume = volume;}
    float getVolume() {return audioParameters.volume;}

}