package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.jam.Part;

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


    static String getNewPartCommand(Part channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("NEW_CHANNEL=");
        getPartInfo(sb, channel);
        return sb.toString();
    }

    static void getPartInfo(StringBuilder sb, Part channel) {

        String surfaceURL = channel.getSurfaceURL();
        String surface = "0";
        if ("PRESET_SEQUENCER".equals(surfaceURL))
            surface = "0";
        if ("PRESET_VERTICAL".equals(surfaceURL))
            surface = "1";
        if ("PRESET_FRETBOARD".equals(surfaceURL))
            surface = "2";

        sb.append(channel.getId());
        sb.append(",");
        sb.append(channel.getMute() ? "0," : "1,");
        sb.append(channel.getSoundSet().isChromatic() ? "1," : "0,");
        sb.append(surface);
        sb.append(",");
        sb.append(channel.getName());
        sb.append(",");
        sb.append(channel.getVolume());
        sb.append(",");
        sb.append(channel.getPan());
        sb.append(",");
        sb.append(channel.getSpeed());
    }

}
