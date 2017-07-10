package com.mikehelland.omgtechnogauntlet;

public class RockDrumChannel {

    public static String getDefaultSoundSetJson() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": false, \"name\": \"");
        sb.append("Rock Drum Kit\", \"url\": \"PRESET_ROCKKIT\", \"data\": [");

        sb.append("{\"url\": \"PRESET_ROCK_KICK\", \"name\": \"kick\", \"preset_id\": ");
        sb.append(R.raw.rock_kick);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_SNARE\", \"name\": \"snare\", \"preset_id\": ");
        sb.append(R.raw.rock_snare);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_HIHAT_MED\", \"name\": \"hi-hat\", \"preset_id\": ");
        sb.append(R.raw.rock_hithat_med);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_HIHAT_OPEN\", \"name\": \"open hi-hat\", \"preset_id\": ");
        sb.append(R.raw.rock_hihat_open);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_CRASH\", \"name\": \"crash\", \"preset_id\": ");
        sb.append(R.raw.rock_crash);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_TOM_MH\", \"name\": \"h tom\", \"preset_id\": ");
        sb.append(R.raw.rock_tom_mh);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_TOM_ML\", \"name\": \"m tom\", \"preset_id\": ");
        sb.append(R.raw.rock_tom_ml);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_TOM_L\", \"name\": \"l tom\", \"preset_id\": ");
        sb.append(R.raw.rock_tom_l);
        sb.append("} ");

        sb.append("]}");

        return sb.toString();
    }

}
