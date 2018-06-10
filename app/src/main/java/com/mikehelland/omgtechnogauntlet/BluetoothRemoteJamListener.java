package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothRemoteJamListener extends OnJamChangeListener {

    private BluetoothConnection connection;

    BluetoothRemoteJamListener(BluetoothConnection connection) {
        this.connection = connection;
    }
    
    @Override
    public void onSubbeatLengthChange(int length, String source) {
        RemoteControlBluetoothHelper.sendNewSubbeatLength(connection, length);
    }

    @Override
    public void onKeyChange(int key, String source) {
        //bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAMINFO_KEY,Integer.toString(key), source);
    }

    @Override
    public void onScaleChange(String scale, String source) {
        //bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAMINFO_SCALE,                scale, source);
    }

    @Override
    public void onChordProgressionChange(int[] chords) {

    }

    @Override
    public void onNewPart(JamPart part) {
        //bluetoothManager.sendCommandToDevices(CommandHelper.getNewPartCommand(part), null);
    }

    @Override
    public void onPartEnabledChanged(JamPart part, boolean enabled, String source) {
        //bluetoothManager.sendCommandToDevices(                CommandHelper.getPartEnabledCommand(part.getId(), enabled), source);
    }

    @Override
    public void onPartVolumeChanged(JamPart part, float volume, String source) {
        //bluetoothManager.sendCommandToDevices(                CommandHelper.getPartVolumeCommand(part.getId(), volume), source);
    }

    @Override
    public void onPartPanChanged(JamPart part, float pan, String source) {
        //bluetoothManager.sendCommandToDevices(                CommandHelper.getPartPanCommand(part.getId(), pan), source);
    }

    @Override
    public void onPlay(String source) {
        if (source == null) {
            RemoteControlBluetoothHelper.setPlay(connection);
        }
    }
    @Override
    public void onStop(String source) {
        if (source == null) {
            RemoteControlBluetoothHelper.setStop(connection);
        }
    }
    @Override
    public void onNewLoop(String source) {
        //RemoteControlBluetoothHelper.setPlay(connection);
    }

    @Override
    public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) {
        if (source == null) {
            connection.sendNameValuePair("SET_PART_TRACK_VALUE",
                    jamPart.getId() + "," + track + "," + subbeat + "," + (value ? 1 : 0));
        }
    }
}
