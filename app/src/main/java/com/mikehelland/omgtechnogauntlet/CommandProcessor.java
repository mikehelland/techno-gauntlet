package com.mikehelland.omgtechnogauntlet;

import android.util.Log;

/**
 * Created by m on 7/31/16.
 * the layer between the bluetooth connection and the jam
 */
class CommandProcessor extends BluetoothDataCallback {

    private BluetoothConnection mConnection;
    private Jam mJam;
    private Channel mChannel = null;

    void setup(BluetoothConnection connection, Jam jam, Channel channel) {
        mJam = jam;
        mConnection = connection;

        //can be null
        mChannel = channel;

        sendJamInfo();
    }

    @Override
    public void newData(String name, String value) {

        Log.d("MGH BT newdata", name);
        Log.d("MGH BT newdata", value);

        if (name.equals("SET_SUBBEATLENGTH")) {
            mJam.setSubbeatLength(Integer.parseInt(value));
            return;
        }

        if (name.equals("SET_CHANNEL")) {
            int channelI = Integer.parseInt(value);
            if (channelI < mJam.getChannels().size()) {
                mChannel = mJam.getChannels().get(channelI);
                sendChannelInfo();
            }
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

            if (mChannel != null) {
                mChannel.setPattern(track, subbeat, patternValue);
            }
        }

    }

    private void channelPlayNote(String value) {
        if (mChannel == null) {
            return;
        }

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

        mChannel.playLiveNote(note);
    }

    private void sendJamInfo() {
        //should this be JSON? Maybe faster this way tho
        mConnection.writeString("SET_KEY=" + mJam.getKey() + ";");
        mConnection.writeString("SET_SCALE=" + mJam.getScaleString() + ";");
        mConnection.writeString("SET_SUBBEATLENGTH=" + mJam.getSubbeatLength() + ";");

        String setChannels = "SET_CHANNELS=";
        String surfaceURL;
        for (int i = 0; i < mJam.getChannels().size(); i++) {
            Channel channel = mJam.getChannels().get(i);
            surfaceURL = channel.getSurfaceURL();
            if ("PRESET_SEQUENCER".equals(surfaceURL))
                setChannels +=  "0";
            if ("PRESET_VERTICAL".equals(surfaceURL))
                setChannels +=  "1";
            if ("PRESET_FRETBOARD".equals(surfaceURL))
                setChannels +=  "2";

            setChannels += channel.getSoundSetName();

            if (i < mJam.getChannels().size() - 1) {
                setChannels += ",";
            }
        }
        mConnection.writeString(setChannels + ";");

        mConnection.writeString(mJam.isPlaying() ? "PLAY;" : "STOP;");
    }

    private void sendChannelInfo() {
        if (mChannel == null)
            return;

        Channel channel = mChannel;
        if (channel.getSoundSet().isChromatic()) {
            String fretboardInfo = "FRETBOARD_INFO=" + channel.getLowNote() + "," +
                    channel.getHighNote() + "," + channel.getOctave() + ";";
            mConnection.writeString(fretboardInfo);

            String noteInfo = "NOTE_INFO=" + getNoteInfo(channel) + ";";
            Log.d("MGH note info", noteInfo);
            mConnection.writeString(noteInfo);
        }
        else {
            String fretboardInfo = "DRUMBEAT_INFO=" + getDrumbeatInfo(channel) + ";";

            mConnection.writeString(fretboardInfo);
        }
    }

    private String getNoteInfo(Channel channel) {

        String info = "";
        int i = 0;
        for (Note note : channel.getNotes()) {
            info += (note.isRest() ? "-" : "") + Double.toString(note.getBeats()) + "|" +
                    Integer.toString(note.getInstrumentNote());
            if (i++ < channel.getNotes().size() - 1) {
                info += ",";
            }
        }

        return info;
    }

    private String getDrumbeatInfo(Channel channel) {
        String info = "";
        int channels = Math.min(channel.pattern.length, channel.getSoundSet().getSounds().size());
        for (int i = 0; i < channels; i++) {

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
