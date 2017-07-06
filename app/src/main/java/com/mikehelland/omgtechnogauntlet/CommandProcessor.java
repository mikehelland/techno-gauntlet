package com.mikehelland.omgtechnogauntlet;

import android.util.Log;

/**
 * Created by m on 7/31/16.
 */
public class CommandProcessor extends BluetoothDataCallback {

    private BluetoothConnection mConnection;
    private Jam mJam;

    public CommandProcessor(BluetoothConnection connection, Jam jam) {
        mJam = jam;
        mConnection = connection;
    }

    private int channelI = 10;

    @Override
    public void newData(String name, String value) {

        Log.d("MGH BT newdata", name);
        Log.d("MGH BT newdata", value);

        if (name.equals("SET_SUBBEATLENGTH")) {
            mJam.setSubbeatLength(Integer.parseInt(value));
            return;
        }

        if (name.equals("SET_CHANNEL")) {
            channelI = Integer.parseInt(value);
            sendFretboardInfo();
            return;
        }

        if (name.equals("GET_JAM_INFO")) {
            sendJamInfo();
            return;
        }

        if (name.equals("CHANNEL_PLAY_NOTE")) {
            channelPlayNote(value);
        }

        if (name.equals("CHANNEL_SET_PATTERN")) {

            String[] params = value.split(",");
            int track = Integer.parseInt(params[0]);
            int subbeat = Integer.parseInt(params[1]);
            boolean patternValue = params[2].equals("true");

            DrumChannel drums = (DrumChannel)getChannel(channelI);
            drums.setPattern(track, subbeat, patternValue);
        }

    }

    Channel getChannel(int i) {
        //hard coded, eh
        switch (i) {
            case 10: return mJam.getSamplerChannel();
            case 11: return mJam.getGuitarChannel();
            case 12: return mJam.getDialpadChannel();
            case 13: return mJam.getSynthChannel();
            case 14: return mJam.getBassChannel();
            case 15: return mJam.getDrumChannel();
        }
        return null;
    }

    void channelPlayNote(String value) {
        Note note = new Note();
        String[] noteInfo = value.split(",");
        int basicNoteNumber = Integer.parseInt(noteInfo[0]);
        int instrumentNoteNumber = Integer.parseInt(noteInfo[1]);

        note.setInstrumentNote(instrumentNoteNumber);
        note.setBasicNote(basicNoteNumber);
        if (instrumentNoteNumber == -1) {
            note.setRest(true);
        }

        Log.d("MGH connection playnote", Integer.toString(basicNoteNumber));

        getChannel(channelI).playLiveNote(note);
    }

    void sendJamInfo() {
        //TODO should be JSON? Maybe faster this way tho
        mConnection.writeString("SET_KEY=" + mJam.getKey() + ";");
        mConnection.writeString("SET_SCALE=" + mJam.getScaleString() + ";");
        mConnection.writeString("SET_SUBBEATLENGTH=" + mJam.getSubbeatLength() + ";");
    }

    void sendFretboardInfo() {
        //hard coded for non drum channels
        if (channelI > 10 && channelI < 15) {
            Channel channel = getChannel(channelI);
            String fretboardInfo = "FRETBOARD_INFO=" + channel.getLowNote() + "," +
                    channel.getHighNote() + "," + channel.getOctave() + ";";
            mConnection.writeString(fretboardInfo);
        }
    }
}
