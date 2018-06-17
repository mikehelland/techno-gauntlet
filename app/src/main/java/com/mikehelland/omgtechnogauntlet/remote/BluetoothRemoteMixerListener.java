package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnMixerChangeListener;

public class BluetoothRemoteMixerListener extends OnMixerChangeListener {

    private BluetoothConnection connection;

    BluetoothRemoteMixerListener(BluetoothConnection connection) {
        this.connection = connection;
    }


    @Override
    public void onPartMuteChanged(JamPart part, boolean mute, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_MUTE,
                part.getId() + "," + (mute ? "1" : "0"));
    }

    @Override
    public void onPartVolumeChanged(JamPart part, float volume, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_VOLUME,
                part.getId() + "," + volume);
    }

    @Override
    public void onPartPanChanged(JamPart part, float pan, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_PAN,
                part.getId() + "," + pan);
    }

    @Override
    public void onPartWarpChanged(JamPart part, float speed, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_WARP,
                part.getId() + "," + speed);
    }

    @Override
    public void onPartTrackMuteChanged(JamPart part, int track, boolean mute, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_TRACK_MUTE,
                part.getId() + "," + (mute ? "1" : "0"));
    }

    @Override
    public void onPartTrackVolumeChanged(JamPart part, int track, float volume, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_TRACK_VOLUME,
                part.getId() + "," + volume);
    }

    @Override
    public void onPartTrackPanChanged(JamPart part, int track, float pan, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_TRACK_PAN,
                part.getId() + "," + pan);
    }

    @Override
    public void onPartTrackWarpChanged(JamPart part, int track, float speed, String source) {
        if (source != null) return;
        connection.sendNameValuePair(CommandProcessor.SET_PART_TRACK_WARP,
                part.getId() + "," + speed);
    }
}
