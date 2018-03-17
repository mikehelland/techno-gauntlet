package com.mikehelland.omgtechnogauntlet;

/**
 * Created by m on 3/17/18.
 */

class SequencerTrack {
    private String name = "";
    private boolean mute = false;
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
}
