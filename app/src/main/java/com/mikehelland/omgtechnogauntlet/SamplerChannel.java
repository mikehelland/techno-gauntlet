package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

public class SamplerChannel extends DrumChannel {

    public SamplerChannel(Context context, Jam jam, OMGSoundPool pool) {
        super(context, jam, pool);

        isAScale = false;
        highNote = 7;
        lowNote = 0;

        mCaptions = new String[] {
                "bongo l", "bongo h", "click l", "click h", "shhk", "scrape", "woop", "chimes"
        };
        presetNames = new String[] {
                "PRESET_bongol", "PRESET_bongoh", "PRESET_clickl", "PRESET_clickh",
                "PRESET_shhk", "PRESET_scrape", "PRESET_woop", "PRESET_chimes"
        };
        rids = new int[8];

        kitName = "PRESET_PERCUSSION_SAMPLER";
    
        rids[0] = R.raw.sampler_8_bongol;
        rids[1] = R.raw.sampler_7_bongoh;
        rids[2] = R.raw.sampler_1_click;
        rids[3] = R.raw.sampler_2_click;
        rids[4] = R.raw.sampler_6_shhk;
        rids[5] = R.raw.sampler_4_scrape;
        rids[6] = R.raw.sampler_5_whoop;
        rids[7] = R.raw.sampler_3_chimes;

    }

    public void makeFill() {

        clearPattern();

        int fillLevel = rand.nextInt(4);

        if (fillLevel == 0)
            return;

        boolean[][] toms = new boolean[][] {pattern[0], pattern[1], pattern[2],
                                            pattern[3], pattern[4]};
        boolean on;
        int tom;
        for (int i = 0; i < 16; i++) {

            on = (fillLevel == 1 && (rand.nextBoolean() && rand.nextBoolean())) ||
                 (fillLevel == 2 && rand.nextBoolean()) ||
                 (fillLevel == 3 && (rand.nextBoolean() || rand.nextBoolean()));
            tom = rand.nextInt(5);

            toms[tom][i] = on;
            toms[tom][i + 16] = on;
        }

    }


}
