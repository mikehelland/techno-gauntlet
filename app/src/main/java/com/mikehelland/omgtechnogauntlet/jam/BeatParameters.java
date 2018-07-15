package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/24/18.
 */

class BeatParameters {
    int subbeatLength = 125;
    int subbeats = 4;
    int beats = 4;
    int measures = 2;
    float shuffle = 0;

    void getData(StringBuilder sb) {
        sb.append("{\"measures\" :");
        sb.append(measures);
        sb.append(", \"beats\" :");
        sb.append(beats);
        sb.append(", \"subbeats\" :");
        sb.append(subbeats);
        sb.append(", \"subbeatMillis\" :");
        sb.append(subbeatLength);
        sb.append(", \"shuffle\" :");
        sb.append(shuffle);
        sb.append("}");
    }

    public static int getTotalSubbeats(BeatParameters beatParameters) {
        return beatParameters.measures * beatParameters.beats * beatParameters.subbeats;
    }
}
