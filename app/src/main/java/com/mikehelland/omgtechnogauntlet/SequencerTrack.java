package com.mikehelland.omgtechnogauntlet;

/**
 * Created by m on 3/17/18.
 */

class SequencerTrack {
    private String name = "";
    private boolean mute = false;
    private float volume = 1;
    private float pan = 0;
    private boolean[] data = new boolean[512];

    String getName() {return name;}
    boolean isMuted() {return mute;}
    boolean[] getData() {return data;}

    SequencerTrack(String name) {
        this.name = name;
    }

    void setData(boolean[] data) {
        this.data = data;
    }

    void toggleMute() {
        mute = !mute;
    }

    void setMute(boolean b) {
        mute = b;
    }

    void setPan(float pan) {this.pan = pan;}
    float getPan() {return pan;}

    void setVolume(float volume) {this.volume = volume;}
    float getVolume() {return volume;}

}
