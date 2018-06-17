package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
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
    public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) {
        if (source == null) {
            connection.sendNameValuePair(CommandProcessor.SET_PART_TRACK_VALUE,
                    jamPart.getId() + "," + track + "," + subbeat + "," + (value ? 1 : 0));
        }
    }

    @Override
    public void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source) {
        if (source == null) {
            connection.sendNameValuePair(CommandProcessor.SET_PART_LIVE_START,
                    jamPart.getId() + "," + autoBeat + "," +
                            note.getBasicNote() + "," + note.getInstrumentNote());
        }
    }

    @Override
    public void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) {
        if (source == null) {
            StringBuilder sb = new StringBuilder();
            for (Note note : notes) {
                sb.append(",");
                sb.append(note.getBasicNote());
                sb.append(",");
                sb.append(note.getInstrumentNote());
            }
            connection.sendNameValuePair(CommandProcessor.SET_PART_LIVE_UPDATE,
                    jamPart.getId() + "," + autoBeat + sb.toString());
        }
    }

    @Override
    public void onPartRemoveLiveNotes(JamPart jamPart, Note noteToRemove, Note[] notes, String source) {
        if (source == null) {
            StringBuilder sb = new StringBuilder();
            for (Note note : notes) {
                sb.append(",");
                sb.append(note.getBasicNote());
                sb.append(",");
                sb.append(note.getInstrumentNote());
            }
            connection.sendNameValuePair(CommandProcessor.SET_PART_LIVE_REMOVE,
                    jamPart.getId() + "," +
                            noteToRemove.getBasicNote() + "," +
                            noteToRemove.getInstrumentNote() + sb.toString());
        }
    }

    @Override
    public void onPartEndLiveNotes(JamPart jamPart, String source) {
        if (source == null) {
            connection.sendNameValuePair(CommandProcessor.SET_PART_LIVE_END,
                    jamPart.getId());
        }
    }

    @Override
    public void onChordProgressionChange(int[] chords, String source) {
        if (source == null) {
            RemoteControlBluetoothHelper.setChords(connection, chords);
        }
    }

    @Override
    public void onNewPart(JamPart part) {
        //bluetoothManager.sendCommandToDevices(CommandHelper.getNewPartCommand(part), null);
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
    public void onNewLoop(String source) { }


}
