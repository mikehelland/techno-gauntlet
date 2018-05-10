package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.jam.JamPart;

class CommandHelper {

    static String getPartEnabledCommand(String id, boolean enabled) {
        return "CHANNEL_ENABLED=" + (enabled?"1,":"0,") + id;
    }
    static String getPartVolumeCommand(String id, float volume) {
        return "CHANNEL_VOLUME=" + volume + "," + id;
    }
    static String getPartPanCommand(String id, float pan) {
        return "CHANNEL_PAN=" + pan + "," + id;
    }


    static String getNewPartCommand(JamPart jamPart) {
        StringBuilder sb = new StringBuilder();
        sb.append("NEW_CHANNEL=");
        getPartInfo(sb, jamPart);
        return sb.toString();
    }

    static void getPartInfo(StringBuilder sb, JamPart jamPart) {

        String surfaceURL = jamPart.getSurfaceURL();
        String surface = "0";
        if ("PRESET_SEQUENCER".equals(surfaceURL))
            surface = "0";
        if ("PRESET_VERTICAL".equals(surfaceURL))
            surface = "1";
        if ("PRESET_FRETBOARD".equals(surfaceURL))
            surface = "2";

        sb.append(jamPart.getId());
        sb.append(",");
        sb.append(jamPart.getMute() ? "0," : "1,");
        sb.append(jamPart.getSoundSet().isChromatic() ? "1," : "0,");
        sb.append(surface);
        sb.append(",");
        sb.append(jamPart.getName());
        sb.append(",");
        sb.append(jamPart.getVolume());
        sb.append(",");
        sb.append(jamPart.getPan());
        sb.append(",");
        sb.append(jamPart.getSpeed());
    }

}
