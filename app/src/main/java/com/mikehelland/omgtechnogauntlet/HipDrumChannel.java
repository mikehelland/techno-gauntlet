package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

public class HipDrumChannel extends DrumChannel {

    public HipDrumChannel(Context context, Jam jam, OMGSoundPool pool) {
        super(context, jam, pool);

        isAScale = false;
        highNote = 7;
        lowNote = 0;
        rids = new int[8];

        presetNames = new String[] {
                "PRESET_HH_KICK",
                "PRESET_HH_CLAP",
                "PRESET_ROCK_HIHAT_CLOSED",
                "PRESET_HH_HIHAT",
                "PRESET_HH_TAMB",
                "PRESET_HH_TOM_MH",
                "PRESET_HH_TOM_ML",
                "PRESET_HH_TOM_L",
        };

        kitName = "PRESET_HIPKIT";
    }

    public int loadPool() {

        String defaultSounds = getDefaultSoundSetJson();
        loadSoundSet(defaultSounds, 0);
        return 1;
    }


    public static String getDefaultSoundSetJson() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": false, \"name\": \"");
        sb.append("Hip Hop Drum Kit\", \"omg_id\": \"PRESET_HIPKIT\", \"data\": [");

        sb.append("{\"caption\": \"kick\", \"preset_id\": ");
        sb.append(R.raw.hh_kick);
        sb.append("}, ");

        sb.append("{\"caption\": \"clap\", \"preset_id\": ");
        sb.append(R.raw.hh_clap);
        sb.append("}, ");

        sb.append("{\"caption\": \"closed hi-hat\", \"preset_id\": ");
        sb.append(R.raw.rock_hithat_closed);
        sb.append("}, ");

        sb.append("{\"caption\": \"open hi-hat\", \"preset_id\": ");
        sb.append(R.raw.hh_hihat);
        sb.append("}, ");

        sb.append("{\"caption\": \"tambourine\", \"preset_id\": ");
        sb.append(R.raw.hh_tamb);
        sb.append("}, ");

        sb.append("{\"caption\": \"h tom\", \"preset_id\": ");
        sb.append(R.raw.hh_tom_mh);
        sb.append("}, ");

        sb.append("{\"caption\": \"m tom\", \"preset_id\": ");
        sb.append(R.raw.hh_tom_ml);
        sb.append("}, ");

        sb.append("{\"caption\": \"l tom\", \"preset_id\": ");
        sb.append(R.raw.hh_tom_l);
        sb.append("} ");

        sb.append("]}");

        return sb.toString();
    }

}
