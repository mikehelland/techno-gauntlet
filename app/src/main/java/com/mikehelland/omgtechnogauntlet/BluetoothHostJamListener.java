package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothHostJamListener extends OnJamChangeListener {
    
    private BluetoothManager bluetoothManager;

    BluetoothHostJamListener(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }
    
    @Override
    public void onSubbeatLengthChange(int length, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_SUBBEATLENGTH,
                Integer.toString(length), source);
    }

    @Override
    public void onKeyChange(int key, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_KEY,
                Integer.toString(key), source);
    }

    @Override
    public void onScaleChange(int[] scale, String source) {
        StringBuilder sb = new StringBuilder();
        if (scale.length > 0) {
            sb.append(scale[0]);
        }
        for (int i = 1; i < scale.length; i++) {
            sb.append(",");
            sb.append(scale[i]);
        }
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_SCALE,
                sb.toString(), source);
    }

    @Override
    public void onChordProgressionChange(int[] chords) {

    }

    @Override
    public void onNewPart(JamPart part) {
        bluetoothManager.sendCommandToDevices(CommandHelper.getNewPartCommand(part), null);
    }

    @Override
    public void onPartEnabledChanged(JamPart part, boolean enabled, String source) {
        bluetoothManager.sendCommandToDevices(
                CommandHelper.getPartEnabledCommand(part.getId(), enabled), source);
    }

    @Override
    public void onPartVolumeChanged(JamPart part, float volume, String source) {
        bluetoothManager.sendCommandToDevices(
                CommandHelper.getPartVolumeCommand(part.getId(), volume), source);
    }

    @Override
    public void onPartPanChanged(JamPart part, float pan, String source) {
        bluetoothManager.sendCommandToDevices(
                CommandHelper.getPartPanCommand(part.getId(), pan), source);
    }

    @Override
    public void onPlay(String source) {
        bluetoothManager.sendCommandToDevices(CommandProcessor.SET_PLAY, source);
    }
    @Override
    public void onStop(String source) {
        bluetoothManager.sendCommandToDevices(CommandProcessor.SET_STOP, source);
    }
    @Override
    public void onNewLoop(String source) {
        bluetoothManager.sendCommandToDevices("ON_NEW_LOOP", source);
    }

    @Override
    public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) {
        bluetoothManager.sendNameValuePairToDevices("SET_PART_TRACK_VALUE",
                jamPart.getId() + "," + track + "," + subbeat + "," + (value ? 1 : 0), source);
    }

    @Override
    public void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source) {

    }

    @Override
    public void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) {

    }

    @Override
    public void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source) {

    }

    @Override
    public void onPartEndLiveNotes(JamPart jamPart, String source) {
        //todo broad cast new notes
    }
}
