package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.content.res.Resources;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
class BassSamplerChannel {


    static String getDefaultSoundSetJSON(Context context) {

        int i = 0;
        int[] rids = new int[21];

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


        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"");
        sb.append("Electric Bass\", \"url\": \"PRESET_BASS\", ");
        sb.append("\"highNote\": 48, \"lowNote\": 28, \"octave\": 2, ");
        sb.append("\"data\": [");

        Resources res = context.getResources();
        String resourceName;
        for (int j = 0; j < i; j++) {

            resourceName = res.getResourceName(rids[j]);
            resourceName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            sb.append("{\"url\": \"PRESET_").append(resourceName).append("\", \"preset_id\": ");
            sb.append(rids[j]);
            sb.append("}");

            if (j < i - 1) {
                sb.append(", \n");
            }

        }

        sb.append("]}");
        return sb.toString();
    }

    static String getSlapSoundSetJSON(Context context) {

        int i = 0;
        int[] rids = new int[25];

        rids[i++] = R.raw.slapa1;
        rids[i++] = R.raw.slapbf1;
        rids[i++] = R.raw.slapb1;
        rids[i++] = R.raw.slapc1;
        rids[i++] = R.raw.slapcs1;
        rids[i++] = R.raw.slapd1;
        rids[i++] = R.raw.slapds1;
        rids[i++] = R.raw.slape1;
        rids[i++] = R.raw.slapf1;
        rids[i++] = R.raw.slapfs1;
        rids[i++] = R.raw.slapg1;
        rids[i++] = R.raw.slapgs1;
        rids[i++] = R.raw.slapa2;
        rids[i++] = R.raw.slapbf2;
        rids[i++] = R.raw.slapb2;
        rids[i++] = R.raw.slapc2;
        rids[i++] = R.raw.slapcs2;
        rids[i++] = R.raw.slapd2;
        rids[i++] = R.raw.slapds2;
        rids[i++] = R.raw.slape2;
        rids[i++] = R.raw.slapf2;
        rids[i++] = R.raw.slapfs2;
        rids[i++] = R.raw.slapg2;
        rids[i++] = R.raw.slapgs2;
        rids[i++] = R.raw.slapa3;


        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"");
        sb.append("Slap Bass\", \"url\": \"http://openmusic.gallery/data/413\", ");
        sb.append("\"highNote\": 45, \"lowNote\": 21, \"octave\": 2, ");
        sb.append("\"data\": [");

        Resources res = context.getResources();
        String resourceName;
        for (int j = 0; j < i; j++) {

            resourceName = res.getResourceName(rids[j]);
            resourceName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            sb.append("{\"url\": \"PRESET_").append(resourceName).append("\", \"preset_id\": ");
            sb.append(rids[j]);
            sb.append("}");

            if (j < i - 1) {
                sb.append(", \n");
            }

        }

        sb.append("]}");
        return sb.toString();
    }
}
