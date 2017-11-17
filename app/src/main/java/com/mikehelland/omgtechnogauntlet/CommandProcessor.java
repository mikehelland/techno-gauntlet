package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Created by m on 7/31/16.
 * the layer between the bluetooth connection and the jam
 */
class CommandProcessor extends BluetoothDataCallback {

    static String JAMINFO_SUBBEATLENGTH = "JAMINFO_SUBBEATLENGTH";
    static String JAMINFO_KEY = "JAMINFO_KEY";
    static String JAMINFO_SCALE = "JAMINFO_SCALE";

    private BluetoothConnection mConnection;
    private Jam mJam;
    private Channel mChannel = null;

    private JamInfo mPeerJam;
    private OnPeerChangeListener mOnPeerChangeListener;

    private Context mContext;

    void setup(Context context, BluetoothConnection connection, Jam jam, Channel channel) {
        mContext = context;
        mJam = jam;
        mConnection = connection;

        //can be null
        mChannel = channel;

        sendJamInfo();
    }

    @Override
    public void newData(String name, String value) {

        Log.d("MGH BT newdata", name);
        Log.d("MGH BT newdata", value != null ? value : "");

        if (name.equals("SET_SUBBEATLENGTH")) {
            mJam.setSubbeatLength(Integer.parseInt(value), mConnection.getDevice().getAddress());
            return;
        }
        if (name.equals("SET_PLAY")) {
            mJam.kickIt();
            return;
        }
        if (name.equals("SET_STOP")) {
            mJam.finish();
            return;
        }
        if (name.equals("SET_KEY")) {
            mJam.setKey(Integer.parseInt(value), mConnection.getDevice().getAddress());
            return;
        }
        if (name.equals("SET_SCALE")) {
            mJam.setScale(value, mConnection.getDevice().getAddress());
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

        if (name.equals("SET_CHORD")) {
            int chordI = Integer.parseInt(value);
            int[] chords = {chordI};
            mJam.setChordProgression(chords);
            return;
        }

        if (name.equals("GET_JAM_INFO")) {
            sendJamInfo();
            return;
        }

        if (name.equals("CHANNEL_PLAY_NOTE")) {
            channelPlayNote(value);
            return;
        }

        if (name.equals("CHANNEL_SET_PATTERN")) {

            if (value != null) {
                String[] params = value.split(",");
                int track = Integer.parseInt(params[0]);
                int subbeat = Integer.parseInt(params[1]);
                boolean patternValue = params[2].equals("true");

                if (mChannel != null) {
                    mChannel.setPattern(track, subbeat, patternValue);
                }
            }
            return;
        }

        if (name.equals(JAMINFO_KEY)) onSetKey(value);
        if (name.equals(JAMINFO_SCALE)) onSetScale(value);
        if (name.equals(JAMINFO_SUBBEATLENGTH)) onSetSubbeatLength(value);
        if (name.equals("JAMINFO_CHANNELS")) onSetChannels(value);

        if (name.equals("GET_SAVED_JAMS")) sendSavedJams();
        if (name.equals("GET_SOUNDSETS")) sendSoundSets();

        if (name.equals("ADD_CHANNEL")) addChannel(Long.parseLong(value));
        if (name.equals("LOAD_JAM")) loadJam(Long.parseLong(value));
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
        mConnection.sendNameValuePair(JAMINFO_KEY, Integer.toString(mJam.getKey()));
        mConnection.sendNameValuePair(JAMINFO_SCALE, mJam.getScaleString());
        mConnection.sendNameValuePair(JAMINFO_SUBBEATLENGTH,
                Integer.toString(mJam.getSubbeatLength()));

        String setChannels = getChannelsInfo(mJam);
        mConnection.sendNameValuePair("JAMINFO_CHANNELS", setChannels);

        mConnection.sendCommand(mJam.isPlaying() ? "PLAY" : "STOP");
    }



    static private String getChannelsInfo(Jam jam) {
        String setChannels = "";
        for (int i = 0; i < jam.getChannels().size(); i++) {
            Channel channel = jam.getChannels().get(i);

            setChannels += getChannelInfo(channel);

            if (i < jam.getChannels().size() - 1) {
                setChannels += ",";
            }
        }
        return setChannels;
    }

    static String getNewChannelCommand(Channel channel) {
        return "NEW_CHANNEL=" + getChannelInfo(channel);
    }

    static String getChannelsInfoCommand(Jam jam) {
        return "SET_CHANNELS=" + getChannelsInfo(jam);
    }

    static private String getChannelInfo(Channel channel) {

        String chromatic = channel.getSoundSet().isChromatic() ? "1" : "0";

        String surfaceURL = channel.getSurfaceURL();
        if ("PRESET_SEQUENCER".equals(surfaceURL))
            return chromatic + "0" + channel.getSoundSetName();
        if ("PRESET_VERTICAL".equals(surfaceURL))
            return chromatic + "1" + channel.getSoundSetName();
        if ("PRESET_FRETBOARD".equals(surfaceURL))
            return chromatic + "2" + channel.getSoundSetName();

        return chromatic + "0" + channel.getSoundSetName();
    }

    private void sendChannelInfo() {
        if (mChannel == null)
            return;

        Channel channel = mChannel;
        if (channel.getSurfaceURL().equals("PRESET_SEQUENCER")) {
            String fretboardInfo = getDrumbeatInfo(channel) + ";";

            mConnection.sendNameValuePair("DRUMBEAT_INFO", fretboardInfo);
        }
        else {
            String fretboardInfo = channel.getLowNote() + "," +
                    channel.getHighNote() + "," + channel.getOctave() + ";";
            mConnection.sendNameValuePair("FRETBOARD_INFO", fretboardInfo);

            String noteInfo = getNoteInfo(channel) + ";";
            Log.d("MGH note info", noteInfo);
            mConnection.sendNameValuePair("NOTE_INFO", noteInfo);
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

    private void onSetKey(String key) {
        assurePeerJam();
        mPeerJam.setKey(Integer.parseInt(key));
        if (mOnPeerChangeListener != null) mOnPeerChangeListener.onChange(mPeerJam);
    }
    private void onSetScale(String scale) {
        assurePeerJam();
        mPeerJam.setScale(scale);
        if (mOnPeerChangeListener != null) mOnPeerChangeListener.onChange(mPeerJam);
    }
    private void onSetSubbeatLength(String subbeatLength) {
        assurePeerJam();
        mPeerJam.setSubbeatLength(Integer.parseInt(subbeatLength));
        if (mOnPeerChangeListener != null) mOnPeerChangeListener.onChange(mPeerJam);
    }
    private void onSetChannels(String channelInfo) {
        channelInfo.split("");
    }

    private void assurePeerJam() {
        if (mPeerJam == null) {
            mPeerJam = new JamInfo();
        }
    }

    static abstract class OnPeerChangeListener {
        abstract void onChange(JamInfo jam);
    }

    void setOnPeerChangeListener(OnPeerChangeListener listener) {
        mOnPeerChangeListener = listener;
    }

    JamInfo getJam() {
        return mPeerJam;
    }

    void sendSavedJams() {
        SavedDataOpenHelper data = new SavedDataOpenHelper(mContext);
        Cursor cursor = data.getSavedCursor();
        StringBuilder value = new StringBuilder();
        while (cursor.moveToNext()) {
            value.append(cursor.getString(cursor.getColumnIndex("_id")));
            value.append(":");
            value.append(cursor.getString(cursor.getColumnIndex("tags")));
            if (!cursor.isLast()) {
                value.append("|");
            }
        }
        cursor.close();
        mConnection.sendNameValuePair("SAVED_JAMS", value.toString());
    }

    void sendSoundSets() {
        SoundSetDataOpenHelper data = new SoundSetDataOpenHelper(mContext);
        Cursor cursor = data.getCursor();
        StringBuilder value = new StringBuilder();
        int idColumn = cursor.getColumnIndex("_id");
        int nameColumn = cursor.getColumnIndex("name");
        while (cursor.moveToNext()) {
            value.append(cursor.getString(idColumn));
            value.append(":");
            value.append(cursor.getString(nameColumn));
            if (!cursor.isLast()) {
                value.append("|");
            }
        }
        cursor.close();
        mConnection.sendNameValuePair("SOUNDSETS", value.toString());
    }

    private void addChannel(long soundsetId) {
        mJam.newChannel(soundsetId);
    }

    private void loadJam(long jamId) {

    }
}
