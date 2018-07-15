package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.Note;

/**
 * Created by m on 4/14/18.
 */

public class RemoteControlBluetoothHelper {
    static void sendNewSubbeatLength(BluetoothConnection connection, int subbeatLength) {
        connection.sendNameValuePair(CommandProcessor.SET_SUBBEATLENGTH, "" + subbeatLength);
    }
    static void sendNewBeats(BluetoothConnection connection, int beats) {
        connection.sendNameValuePair(CommandProcessor.SET_BEATS, "" + beats);
    }
    static void sendNewMeasures(BluetoothConnection connection, int measures) {
        connection.sendNameValuePair(CommandProcessor.SET_MEASURES, "" + measures);
    }
    static void sendNewShuffle(BluetoothConnection connection, float shuffle) {
        connection.sendNameValuePair(CommandProcessor.SET_SHUFFLE, "" + shuffle);
    }


    static void setChannel(BluetoothConnection connection, String channelID) {
        connection.sendNameValuePair("SET_CHANNEL",  channelID );
    }

    static void setChords(BluetoothConnection connection, int[] chords) {
        String output = "";
        for (int i = 0; i < chords.length; i++) {
            if (i > 0) {
                output += ",";
            }
            output += chords[i];
        }
        connection.sendNameValuePair(CommandProcessor.SET_CHORDS, output);
    }
    static void setKey(BluetoothConnection connection, int value) {
        connection.sendNameValuePair("SET_KEY", value + "");
    }
    static void setScale(BluetoothConnection connection, String value) {
        connection.sendNameValuePair("SET_SCALE", value + "");
    }

    static void playNote(BluetoothConnection connection, Note note) {
        int instrumentNumber = note.isRest() ? -1 : note.getInstrumentNote();
        int basicNote = note.isRest() ? -1 : note.getBasicNote();
        connection.sendNameValuePair("CHANNEL_PLAY_NOTE", basicNote + "," + instrumentNumber + ";");

    }

    public static void requestJam(BluetoothConnection connection) {
        connection.sendCommand(CommandProcessor.GET_JAM);
    }
    public static void requestSavedJams(BluetoothConnection connection) {
        connection.sendCommand(CommandProcessor.GET_SAVED_JAMS);
    }
    public static void requestSoundSets(BluetoothConnection connection) {
        connection.sendCommand(CommandProcessor.GET_SOUNDSETS);
    }

    static void setPlay(BluetoothConnection connection) {
        connection.sendCommand("SET_PLAY");
    }
    static void setStop(BluetoothConnection connection) {
        connection.sendCommand("SET_STOP");
    }


    static void setArpeggiator(BluetoothConnection connection, int arpeggiate) {
        connection.sendNameValuePair("CHANNEL_SET_ARPEGGIATOR", Integer.toString(arpeggiate));
    }
    static void setArpNotes(BluetoothConnection connection, Note[] notes) {
        String output = "";
        for (int i = 0; i < notes.length; i++) {
            output += notes[i].getBasicNote() + "," + notes[i].getInstrumentNote();
            if (i < notes.length - 1) {
                output += "|";
            }
        }
        connection.sendNameValuePair("CHANNEL_SET_ARPNOTES", output + "");
    }

    static void clearChannel(BluetoothConnection connection, String channelID) {
        connection.sendNameValuePair("CLEAR_CHANNEL", channelID + "");
    }

}
