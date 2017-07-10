package com.mikehelland.omgtechnogauntlet;

public class SamplerChannel {


    public static String getDefaultSoundSetJson() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\" : \"SOUNDSET\", \"chromatic\": false, \"name\": \"");
        sb.append("Percussion Sampler\", \"url\": \"PRESET_PERCUSSION_SAMPLER\", \"data\": [");

        sb.append("{\"url\": \"PRESET_bongol\", \"name\": \"bongo l\", \"preset_id\": ");
        sb.append(R.raw.sampler_8_bongol);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_bongoh\", \"name\": \"bongo l\", \"preset_id\": ");
        sb.append(R.raw.sampler_7_bongoh);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_clickl\", \"name\": \"click l\", \"preset_id\": ");
        sb.append(R.raw.sampler_1_click);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_clickh\", \"name\": \"click h\", \"preset_id\": ");
        sb.append(R.raw.sampler_2_click);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_shhk\", \"name\": \"shhk\", \"preset_id\": ");
        sb.append(R.raw.sampler_6_shhk);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_scrape\", \"name\": \"scrape\", \"preset_id\": ");
        sb.append(R.raw.sampler_4_scrape);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_whoop\", \"name\": \"whoop\", \"preset_id\": ");
        sb.append(R.raw.sampler_5_whoop);
        sb.append("}, ");

        sb.append("{\"url\": \"PRESET_chimes\", \"name\": \"chimes\", \"preset_id\": ");
        sb.append(R.raw.sampler_3_chimes);
        sb.append("} ");

        sb.append("]}");

        return sb.toString();
    }


}
