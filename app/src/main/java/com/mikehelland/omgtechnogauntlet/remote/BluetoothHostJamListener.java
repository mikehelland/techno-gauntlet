package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothHostJamListener extends   OnJamChangeListener {
    
    private BluetoothManager bluetoothManager;
    private Jam jam;

    BluetoothHostJamListener(Jam jam, BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
        this.jam = jam;
    }

    @Override
    public void onChordProgressionChange(int[] chords, String source) {
        String output = "";
        for (int i = 0; i < chords.length; i++) {
            if (i > 0) {
                output += ",";
            }
            output += chords[i];
        }
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_CHORDS, output, source);
    }

    @Override
    public void onNewJam(Jam jam, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAM_JSON, jam.getData(), null);
    }

    @Override
    public void onNewPart(JamPart part, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAM_JSON, jam.getData(), null);
        //bluetoothManager.sendCommandToDevices(CommandProcessor.getNewPartCommand(part), null);
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

    @Override
    public void onPartClear(JamPart jamPart, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.PART_CLEAR, jamPart.getId(), source);
    }
}
