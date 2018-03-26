package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/24/18.
 */

class AudioParameters {
    boolean mute = true;
    float volume = 0.75f;
    float pan = 0f;
    float speed = 1;

    public void getData(StringBuilder sb) {
        sb.append("{\"mute\": ");
        sb.append(mute);
        sb.append(", \"volume\": ");
        sb.append(volume);
        sb.append(", \"pan\": ");
        sb.append(pan);
        sb.append(", \"speed\": ");
        sb.append(speed);
        sb.append("}");

    }
}
