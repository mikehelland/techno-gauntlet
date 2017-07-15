package com.mikehelland.omgtechnogauntlet;

class HipDrumChannel {

    static String getDefaultSoundSetJson() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": false, \"name\": \"");
        sb.append("Hip Hop Drum Kit\", \"url\": \"PRESET_HIPKIT\", \"data\": [");

        sb.append("{\"url\": \"PRESET_HH_KICK\", \"name\": \"kick\", \"preset_id\": ");
        sb.append(R.raw.hh_kick);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_HH_CLAP\", \"name\": \"clap\", \"preset_id\": ");
        sb.append(R.raw.hh_clap);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_ROCK_HIHAT_CLOSED\", \"name\": \"closed hi-hat\", \"preset_id\": ");
        sb.append(R.raw.rock_hithat_closed);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_HH_HIHAT\", \"name\": \"open hi-hat\", \"preset_id\": ");
        sb.append(R.raw.hh_hihat);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_HH_TAMB\", \"name\": \"tambourine\", \"preset_id\": ");
        sb.append(R.raw.hh_tamb);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_HH_TOM_MH\", \"name\": \"h tom\", \"preset_id\": ");
        sb.append(R.raw.hh_tom_mh);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_HH_TOM_ML\", \"name\": \"m tom\", \"preset_id\": ");
        sb.append(R.raw.hh_tom_ml);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_HH_TOM_L\", \"name\": \"l tom\", \"preset_id\": ");
        sb.append(R.raw.hh_tom_l);
        sb.append("} ");

        sb.append("]}");

        return sb.toString();
    }

}
