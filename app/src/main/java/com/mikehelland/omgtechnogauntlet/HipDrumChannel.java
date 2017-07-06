package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

public class HipDrumChannel extends DrumChannel {

    public HipDrumChannel(Context context, Jam jam, OMGSoundPool pool) {
        super(context, jam, pool);

        isAScale = false;
        highNote = 7;
        lowNote = 0;
        rids = new int[8];

        int i= 0;

        rids[i++] = R.raw.hh_kick;
        rids[i++] = R.raw.hh_clap;
        rids[i++] = R.raw.rock_hithat_closed;
        rids[i++] = R.raw.hh_hihat;
        rids[i++] = R.raw.hh_tamb;
        rids[i++] = R.raw.hh_tom_mh;
        rids[i++] = R.raw.hh_tom_ml;
        rids[i++] = R.raw.hh_tom_l;



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

        mCaptions = new String[] {"kick", "clap", "closed hi-hat", "open hi-hat",
            "tambourine", "h tom", "m tom", "l tom"};

        kitName = "PRESET_HIPKIT";
    }

}
