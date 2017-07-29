package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

class ElectricSamplerChannel {

    static String getDefaultSoundSetJSON(Context context) {

        int[] rids = new int[1];

        int i= 0;

        rids[i++] = R.raw.electric_e;
        rids[i++] = R.raw.electric_f;
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
        rids[i++] = R.raw.electric_cs4;

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"");
        sb.append("Electric Guitar\", \"url\": \"PRESET_GUITAR\", ");
        sb.append("\"highNote\": 85, \"lowNote\": 40, \"octave\": 5, ");
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
