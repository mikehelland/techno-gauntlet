package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnMixerChangeListener;

public class BluetoothHostMixerListener extends OnMixerChangeListener {

    private BluetoothManager bluetoothManager;

    BluetoothHostMixerListener(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }


    @Override
    public void onPartMuteChanged(JamPart part, boolean mute, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_MUTE,
                part.getId() + "," + (mute ? "1" : "0"), source);
    }

    @Override
    public void onPartVolumeChanged(JamPart part, float volume, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_VOLUME,
                part.getId() + "," + volume, source);
    }

    @Override
    public void onPartPanChanged(JamPart part, float pan, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_PAN,
                part.getId() + "," + pan, source);
    }

    @Override
    public void onPartWarpChanged(JamPart part, float speed, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_WARP,
                part.getId() + "," + speed, source);
    }

    @Override
    public void onPartTrackMuteChanged(JamPart part, int track, boolean mute, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_TRACK_MUTE,
                part.getId() + "," + track + "," + (mute ? "1" : "0"), source);
    }

    @Override
    public void onPartTrackVolumeChanged(JamPart part, int track, float volume, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_TRACK_VOLUME,
                part.getId() + "," + track + "," + volume, source);
    }

    @Override
    public void onPartTrackPanChanged(JamPart part, int track, float pan, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_TRACK_PAN,
                part.getId() + "," + track + "," + pan, source);
    }

    @Override
    public void onPartTrackWarpChanged(JamPart part, int track, float speed, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_PART_TRACK_WARP,
                part.getId() + "," + track + "," + speed, source);
    }
}
