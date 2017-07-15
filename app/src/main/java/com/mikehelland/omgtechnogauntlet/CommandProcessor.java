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
            sendChannelInfo();
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

            Channel drums = getChannel(channelI);
            drums.setPattern(track, subbeat, patternValue);
        }

    }

    Channel getChannel(int i) {
        if (i < mJam.getChannels().size())
            return mJam.getChannels().get(i);

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

        String setChannels = "SET_CHANNELS=";
        for (int i = 0; i < mJam.getChannels().size(); i++) {
            Channel channel = mJam.getChannels().get(i);
            setChannels += channel.getSoundSet().isChromatic() ? "1" : "0";
            setChannels += channel.getSoundSetName();

            if (i < mJam.getChannels().size() - 1) {
                setChannels += ",";
            }
        }

        mConnection.writeString(setChannels + ";");
    }

    void sendChannelInfo() {

        Channel channel = getChannel(channelI);
        if (channel.getSoundSet().isChromatic()) {
            String fretboardInfo = "FRETBOARD_INFO=" + channel.getLowNote() + "," +
                    channel.getHighNote() + "," + channel.getOctave() + ";";
            mConnection.writeString(fretboardInfo);
        }
        else {
            String fretboardInfo = "DRUMBEAT_INFO=" + getDrumbeatInfo(channel) + ";";

            mConnection.writeString(fretboardInfo);
        }
    }

    String getDrumbeatInfo(Channel channel) {
        String info = "";
        for (int i = 0; i < channel.pattern.length; i++) {

            info += channel.getSoundSet().getSounds().get(i).getName() + "|";

            for (int j = 0; j < channel.pattern[i].length; j++) {

                info += channel.pattern[i][j] ? "1" : "0";
                if (j < channel.pattern[i].length - 1) {
                    info += "|";
                }

            }

            if (i < channel.pattern.length - 1) {
                info += ",";
            }
        }
        return info;
    }
}
