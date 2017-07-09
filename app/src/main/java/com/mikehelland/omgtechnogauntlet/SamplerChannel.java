package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

public class SamplerChannel extends DrumChannel {

    public SamplerChannel(Context context, Jam jam, OMGSoundPool pool) {
        super(context, jam, pool);

        isAScale = false;
        highNote = 7;
        lowNote = 0;

        presetNames = new String[] {
                "PRESET_bongol", "PRESET_bongoh", "PRESET_clickl", "PRESET_clickh",
                "PRESET_shhk", "PRESET_scrape", "PRESET_woop", "PRESET_chimes"
        };
        rids = new int[8];

    }

    public int loadPool() {

        String defaultSounds = getDefaultSoundSetJson();
        loadSoundSet(defaultSounds, 0);
        return 1;
    }


    public static String getDefaultSoundSetJson() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": false, \"name\": \"");
        sb.append("Percussion Sampler\", \"omg_id\": \"PRESET_PERCUSSION_SAMPLER\", \"data\": [");

        sb.append("{\"caption\": \"bongo l\", \"preset_id\": ");
        sb.append(R.raw.sampler_8_bongol);
        sb.append("}, ");

        sb.append("{\"caption\": \"bongo l\", \"preset_id\": ");
        sb.append(R.raw.sampler_7_bongoh);
        sb.append("}, ");

        sb.append("{\"caption\": \"click l\", \"preset_id\": ");
        sb.append(R.raw.sampler_1_click);
        sb.append("}, ");

        sb.append("{\"caption\": \"click h\", \"preset_id\": ");
        sb.append(R.raw.sampler_2_click);
        sb.append("}, ");

        sb.append("{\"caption\": \"shhk\", \"preset_id\": ");
        sb.append(R.raw.sampler_6_shhk);
        sb.append("}, ");

        sb.append("{\"caption\": \"scrape\", \"preset_id\": ");
        sb.append(R.raw.sampler_4_scrape);
        sb.append("}, ");

        sb.append("{\"caption\": \"whoop\", \"preset_id\": ");
        sb.append(R.raw.sampler_5_whoop);
        sb.append("}, ");

        sb.append("{\"caption\": \"chimes\", \"preset_id\": ");
        sb.append(R.raw.sampler_3_chimes);
        sb.append("} ");

        sb.append("]}");

        return sb.toString();
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
