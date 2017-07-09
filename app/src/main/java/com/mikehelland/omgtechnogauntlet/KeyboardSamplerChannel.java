package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

public class KeyboardSamplerChannel extends Channel {


    public KeyboardSamplerChannel(Context context, Jam jam, OMGSoundPool pool,
                                  String type, String sound) {
        super(context, jam, pool, type, sound);

        octave = 5;

        highNote = 93;
        lowNote = 33;

        volume = 0.2f;
        //rids = new int[61];
        rids = new int[1];

        int i= 0;

        rids[i++] = R.raw.kb1_a1;
        /*rids[i++] = R.raw.kb1_bf1;
        rids[i++] = R.raw.kb1_b1;
        rids[i++] = R.raw.kb1_c2;
        rids[i++] = R.raw.kb1_cs2;
        rids[i++] = R.raw.kb1_d2;
        rids[i++] = R.raw.kb1_ds2;
        rids[i++] = R.raw.kb1_e2;
        rids[i++] = R.raw.kb1_f2;
        rids[i++] = R.raw.kb1_fs2;
        rids[i++] = R.raw.kb1_g2;
        rids[i++] = R.raw.kb1_gs2;
        rids[i++] = R.raw.kb1_a2;
        rids[i++] = R.raw.kb1_bf2;
        rids[i++] = R.raw.kb1_b2;
        rids[i++] = R.raw.kb1_c3;
        rids[i++] = R.raw.kb1_cs3;
        rids[i++] = R.raw.kb1_d3;
        rids[i++] = R.raw.kb1_ds3;
        rids[i++] = R.raw.kb1_e3;
        rids[i++] = R.raw.kb1_f3;
        rids[i++] = R.raw.kb1_fs3;
        rids[i++] = R.raw.kb1_g3;
        rids[i++] = R.raw.kb1_gs3;
        rids[i++] = R.raw.kb1_a3;
        rids[i++] = R.raw.kb1_bf3;
        rids[i++] = R.raw.kb1_b3;
        rids[i++] = R.raw.kb1_c4;
        rids[i++] = R.raw.kb1_cs4;
        rids[i++] = R.raw.kb1_d4;
        rids[i++] = R.raw.kb1_ds4;
        rids[i++] = R.raw.kb1_e4;
        rids[i++] = R.raw.kb1_f4;
        rids[i++] = R.raw.kb1_fs4;
        rids[i++] = R.raw.kb1_g4;
        rids[i++] = R.raw.kb1_gs4;
        rids[i++] = R.raw.kb1_a4;
        rids[i++] = R.raw.kb1_bf4;
        rids[i++] = R.raw.kb1_b4;
        rids[i++] = R.raw.kb1_c5;
        rids[i++] = R.raw.kb1_cs5;
        rids[i++] = R.raw.kb1_d5;
        rids[i++] = R.raw.kb1_ds5;
        rids[i++] = R.raw.kb1_e5;
        rids[i++] = R.raw.kb1_f5;
        rids[i++] = R.raw.kb1_fs5;
        rids[i++] = R.raw.kb1_g5;
        rids[i++] = R.raw.kb1_gs5;
        rids[i++] = R.raw.kb1_a5;
        rids[i++] = R.raw.kb1_bf5;
        rids[i++] = R.raw.kb1_b5;
        rids[i++] = R.raw.kb1_c6;
        rids[i++] = R.raw.kb1_cs6;
        rids[i++] = R.raw.kb1_d6;
        rids[i++] = R.raw.kb1_ds6;
        rids[i++] = R.raw.kb1_e6;
        rids[i++] = R.raw.kb1_f6;
        rids[i++] = R.raw.kb1_fs6;
        rids[i++] = R.raw.kb1_g6;
        rids[i++] = R.raw.kb1_gs6;
        rids[i++] = R.raw.kb1_a6;*/

    }
}
