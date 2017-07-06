package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
public class BassSamplerChannel extends Channel {


    public BassSamplerChannel(Context context, Jam jam, OMGSoundPool pool, String type, String sound) {
        super(context, jam, pool, type, sound);

        highNote = 48;
        lowNote = 28;

        octave = 2;

        volume = 0.8f;
        rids = new int[21];

        int i= 0;

        rids[i++] = R.raw.bass_e;
        rids[i++] = R.raw.bass_f;
        rids[i++] = R.raw.bass_fs;
        rids[i++] = R.raw.bass_g;
        rids[i++] = R.raw.bass_gs;
        rids[i++] = R.raw.bass_a;
        rids[i++] = R.raw.bass_bf;
        rids[i++] = R.raw.bass_b;
        rids[i++] = R.raw.bass_c;
        rids[i++] = R.raw.bass_cs;
        rids[i++] = R.raw.bass_d;
        rids[i++] = R.raw.bass_ds;
        rids[i++] = R.raw.bass_e2;
        rids[i++] = R.raw.bass_f2;
        rids[i++] = R.raw.bass_fs2;
        rids[i++] = R.raw.bass_g2;
        rids[i++] = R.raw.bass_gs2;
        rids[i++] = R.raw.bass_a2;
        rids[i++] = R.raw.bass_bf2;
        rids[i++] = R.raw.bass_b2;
        rids[i++] = R.raw.bass_c2;

    }

}
