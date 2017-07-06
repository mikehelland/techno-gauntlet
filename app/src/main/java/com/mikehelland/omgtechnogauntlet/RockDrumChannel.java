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

        mCaptions = new String[] {"kick", "snare", "hi-hat", "open hi-hat",
                "crash", "h tom", "m tom", "l tom"};

        kitName = "PRESET_ROCKKIT";
        rids = new int[8];

        int i= 0;

        rids[i++] = R.raw.rock_kick;
        rids[i++] = R.raw.rock_snare;
        rids[i++] = R.raw.rock_hithat_med;
        rids[i++] = R.raw.rock_hihat_open;
        rids[i++] = R.raw.rock_crash;
        rids[i++] = R.raw.rock_tom_mh;
        rids[i++] = R.raw.rock_tom_ml;
        rids[i++] = R.raw.rock_tom_l;
    }

}
