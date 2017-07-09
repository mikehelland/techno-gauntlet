package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

public class RockDrumChannel extends DrumChannel {

    public RockDrumChannel(Context context, Jam jam, OMGSoundPool pool) {
        super(context, jam, pool);

        isAScale = false;
        highNote = 7;
        lowNote = 0;

        presetNames = new String[]{"PRESET_ROCK_KICK",
                "PRESET_ROCK_SNARE",
                "PRESET_ROCK_HIHAT_MED",
                "PRESET_ROCK_HIHAT_OPEN",
                "PRESET_ROCK_CRASH",
                "PRESET_ROCK_TOM_MH",
                "PRESET_ROCK_TOM_ML",
                "PRESET_ROCK_TOM_L"};
        kitName = "PRESET_ROCKKIT";

    }

    public int loadPool() {

        String defaultSounds = getDefaultSoundSetJson();
        loadSoundSet(defaultSounds, 0);
        return 1;
    }

    public static String getDefaultSoundSetJson() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": false, \"name\": \"");
        sb.append("Rock Drum Kit\", \"omg_id\": \"PRESET_ROCKKIT\", \"data\": [");

        sb.append("{\"caption\": \"kick\", \"preset_id\": ");
        sb.append(R.raw.rock_kick);
        sb.append("}, ");

        sb.append("{\"caption\": \"snare\", \"preset_id\": ");
        sb.append(R.raw.rock_snare);
        sb.append("}, ");

        sb.append("{\"caption\": \"hi-hat\", \"preset_id\": ");
        sb.append(R.raw.rock_hithat_med);
        sb.append("}, ");

        sb.append("{\"caption\": \"open hi-hat\", \"preset_id\": ");
        sb.append(R.raw.rock_hihat_open);
        sb.append("}, ");

        sb.append("{\"caption\": \"crash\", \"preset_id\": ");
        sb.append(R.raw.rock_crash);
        sb.append("}, ");

        sb.append("{\"caption\": \"h tom\", \"preset_id\": ");
        sb.append(R.raw.rock_tom_mh);
        sb.append("}, ");

        sb.append("{\"caption\": \"m tom\", \"preset_id\": ");
        sb.append(R.raw.rock_tom_ml);
        sb.append("}, ");

        sb.append("{\"caption\": \"l tom\", \"preset_id\": ");
        sb.append(R.raw.rock_tom_l);
        sb.append("} ");

        sb.append("]}");

        return sb.toString();
    }

}
