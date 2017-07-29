package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

class KeyboardSamplerChannel {

    static String getDefaultSoundSetJSON(Context context) {

        int[] rids = new int[61];
        int i = 0;

        rids[i++] = R.raw.kb1_a1;
        rids[i++] = R.raw.kb1_bf1;
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
        rids[i++] = R.raw.kb1_a6;




        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"");
        sb.append("Keyboard\", \"url\": \"PRESET_KEYBOARD\", ");
        sb.append("\"highNote\": 93, \"lowNote\": 33, \"octave\": 5, ");
        sb.append("\"data\": [");

        Resources res = context.getResources();
        String resourceName;
        for (int j = 0; j < i; j++) {

            resourceName = res.getResourceName(rids[j]);
            resourceName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            sb.append("{\"url\": \"PRESET_" + resourceName + "\", \"preset_id\": ");
            sb.append(rids[j]);
            sb.append("}");

            if (j < i - 1) {
                sb.append(", \n");
            }

        }

        sb.append("]}");
        Log.d("MGH", sb.toString());
        return sb.toString();

    }



}
