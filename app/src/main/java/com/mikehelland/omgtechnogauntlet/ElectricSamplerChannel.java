package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
public class ElectricSamplerChannel extends Channel {


    public ElectricSamplerChannel(Context context, Jam jam, OMGSoundPool pool, String type, String sound) {
        super(context, jam, pool, type, sound);

        octave = 5;

        highNote = 85;
        lowNote = 40;

        volume = 0.10f;
        //rids = new int[46];
        rids = new int[1];

        int i= 0;

        rids[i++] = R.raw.electric_e;
        /*rids[i++] = R.raw.electric_f;
        rids[i++] = R.raw.electric_fs;
        rids[i++] = R.raw.electric_g;
        rids[i++] = R.raw.electric_gs;
        rids[i++] = R.raw.electric_a;
        rids[i++] = R.raw.electric_bf;
        rids[i++] = R.raw.electric_b;
        rids[i++] = R.raw.electric_c;
        rids[i++] = R.raw.electric_cs;
        rids[i++] = R.raw.electric_d;
        rids[i++] = R.raw.electric_ds;
        rids[i++] = R.raw.electric_e2;
        rids[i++] = R.raw.electric_f2;
        rids[i++] = R.raw.electric_fs2;
        rids[i++] = R.raw.electric_g2;
        rids[i++] = R.raw.electric_gs2;
        rids[i++] = R.raw.electric_a2;
        rids[i++] = R.raw.electric_bf2;
        rids[i++] = R.raw.electric_b2;
        rids[i++] = R.raw.electric_c2;
        rids[i++] = R.raw.electric_cs2;
        rids[i++] = R.raw.electric_d2;
        rids[i++] = R.raw.electric_ds2;
        rids[i++] = R.raw.electric_e3;
        rids[i++] = R.raw.electric_f3;
        rids[i++] = R.raw.electric_fs3;
        rids[i++] = R.raw.electric_g3;
        rids[i++] = R.raw.electric_gs3;
        rids[i++] = R.raw.electric_a3;
        rids[i++] = R.raw.electric_bf3;
        rids[i++] = R.raw.electric_b3;
        rids[i++] = R.raw.electric_c3;
        rids[i++] = R.raw.electric_cs3;
        rids[i++] = R.raw.electric_d3;
        rids[i++] = R.raw.electric_ds3;
        rids[i++] = R.raw.electric_e4;
        rids[i++] = R.raw.electric_f4;
        rids[i++] = R.raw.electric_fs4;
        rids[i++] = R.raw.electric_g4;
        rids[i++] = R.raw.electric_gs4;
        rids[i++] = R.raw.electric_a4;
        rids[i++] = R.raw.electric_bf4;
        rids[i++] = R.raw.electric_b4;
        rids[i++] = R.raw.electric_c4;
        rids[i++] = R.raw.electric_cs4;*/

    }

}
