package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
public class BluetoothChannel extends Channel {

    private BluetoothConnection mConnection;

    public BluetoothChannel(Context context, Jam jam, OMGSoundPool pool,
                            BluetoothConnection connection) {
        super(context, jam, pool, "TYPE", "NAME");
        mConnection = connection;

        highNote = 85;
        lowNote = 40;


        volume = 0.15f;

    }

    @Override
    public int playNote(Note note, boolean multiTouch) {
        int instrumentNumber = note.isRest() ? -1 : note.getInstrumentNote();
        int basicNote = note.isRest() ? -1 : note.getBasicNote();

        mConnection.writeString("CHANNEL_PLAY_NOTE=" + basicNote + "," + instrumentNumber + ";");
        return 0;
    }

    public void setLowHigh(int low, int high, int octave) {
        lowNote = low;
        highNote = high;
        this.octave = octave;
    }
}
