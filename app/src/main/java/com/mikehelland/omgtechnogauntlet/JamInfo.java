package com.mikehelland.omgtechnogauntlet;

import java.util.ArrayList;

class JamInfo {

    private static String[] keyCaptions = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
    private static String[] scaleCaptions = {"Major", "Minor", "Pentatonic", "Blues"};
    private static String[] scales = {"0,2,4,5,7,9,11", "0,2,3,5,7,8,10", "0,2,4,7,9", "0,3,5,6,7,10"};

    private int subbeats = 4;
    private int beats = 8;
    private int totalsubbeats = subbeats * beats;
    private int subbeatLength = 125; //70 + rand.nextInt(125); // 125;

    private float shuffle = 0;

    private ArrayList<Channel> mChannels = new ArrayList<>();

    private int key = 0;
    private int[] scale = {0, 2, 4, 5, 7, 9, 11};
    private int scaleI = 0;

    void setKey(int key) {
        this.key = key;
    }
    void setScale(String scale) {

        for (int i = 0; i < scales.length; i++) {
            if (scales[i].equals(scale)) {
                scaleI = i;
            }
        }

        String[] intervals = scale.split(",");
        int[] ascale = new int[intervals.length];
        for (int i = 0; i < intervals.length; i++) {
            ascale[i] = Integer.parseInt(intervals[i]);
        }

        this.scale = ascale;
    }
    void setSubbeatLength(int subbeatLength) {
        this.subbeatLength = subbeatLength;
    }


    static String getKeyName(JamInfo jam) {
        return keyCaptions[jam.key] + " " + scaleCaptions[jam.scaleI];
    }

    static int getBPM(JamInfo jam) {
        return 60000 / (jam.subbeatLength * jam.subbeats);
    }

}
