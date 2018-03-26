package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/25/18.
 */

class KeyParameters {
    int[] scale = new int[] {0,2,4,5,7,9,11};
    int rootNote = 0;

    void getData(StringBuilder sb) {
        sb.append("{\"scale\": [");
        int i = 0;
        for (int interval : scale) {
            sb.append(interval);
            i++;
            if (i < scale.length)
                sb.append(",");
        }
        sb.append("], \"rootNote\": ");
        sb.append(rootNote);
        sb.append("}");
    }
}