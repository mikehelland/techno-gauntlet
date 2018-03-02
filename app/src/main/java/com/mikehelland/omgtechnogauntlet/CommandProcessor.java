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
    final static String SET_ARPNOTES = "SET_ARPNOTES";

    private BluetoothConnection mConnection;
    private Jam mJam;
    private Channel mChannel = null;

    private JamInfo mPeerJam;
    private OnPeerChangeListener mOnPeerChangeListener;

    final private DatabaseContainer mDatabase;
    final private Main mContext;

    private boolean mSync = false;

    CommandProcessor(Context context) {
        //todo storing the context because I need to load the jam
        //maybe this should be done somehow else
        mContext = (Main)context;
        mDatabase = mContext.getDatabase();
    }

    void setup(BluetoothConnection connection, Jam jam, Channel channel) {
        mJam = jam;
        mConnection = connection;

        //can be null
        mChannel = channel;

        sendJamInfo();
    }

    @Override
    public void newData(String name, String value) {

        Log.d("MGH BT newdata", name + (value != null ? ("=" + value) : ""));

        if (name.equals("SET_SUBBEATLENGTH")) {
            if (value == null) return;
            mJam.setSubbeatLength(Integer.parseInt(value), mConnection.getDevice().getAddress());
            return;
        }
        if (name.equals("SET_PLAY")) {
            mJam.kickIt();
            return;
        }
        if (name.equals("SET_STOP")) {
            mJam.pause();
            return;
        }
        if (name.equals("SET_KEY")) {
            if (value == null) return;
            mJam.setKey(Integer.parseInt(value), mConnection.getDevice().getAddress());
            return;
        }
        if (name.equals("SET_SCALE")) {
            mJam.setScale(value, mConnection.getDevice().getAddress());
            return;
        }

        if (name.equals("SET_CHANNEL")) {
            if (value == null) return;
            int channelI = Integer.parseInt(value);
            if (channelI < mJam.getChannels().size()) {
                mChannel = mJam.getChannels().get(channelI);
                sendChannelInfo();
            }
            return;
        }

        if (name.equals("SET_CHORD")) {
            if (value == null) return;
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
            if (value == null) return;
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

        if (name.equals("SET_ARPEGGIATOR")) {
            if (value == null) return;
            if (mChannel != null)
                mChannel.setArpeggiator(Integer.parseInt(value));
        }

        if (name.equals(SET_ARPNOTES)) {
            onSetArpNotes(value);
        }

        if (name.equals(JAMINFO_KEY)) {
            if (value == null) return;
            onSetKey(value);
        }
        if (name.equals(JAMINFO_SCALE)) {
            if (value == null) return;
            onSetScale(value);
        }
        if (name.equals(JAMINFO_SUBBEATLENGTH)) {
            if (value == null) return;
            onSetSubbeatLength(value);
        }
        if (name.equals("JAMINFO_CHANNELS")) {
            if (value == null) return;
            onSetChannels(value);
        }

        if (name.equals("GET_SAVED_JAMS")) sendSavedJams();
        if (name.equals("GET_SOUNDSETS")) sendSoundSets();

        if (name.equals("ADD_CHANNEL")) {
            if (value == null) return;
            addChannel(Long.parseLong(value));
        }
        if (name.equals("LOAD_JAM")) {
            if (value == null) return;
            loadJam(Long.parseLong(value));
        }

        if (name.equals("SET_CHANNEL_VOLUME")) {
            if (value == null) return;
            setChannelVolume(value);
        }
        if (name.equals("SET_CHANNEL_PAN")) {
            if (value == null) return;
            setChannelPan(value);
        }
        if (name.equals("SET_CHANNEL_ENABLED")) {
            if (value == null) return;
            setChannelEnabled(value);
        }

        if (name.equals("CLEAR_CHANNEL")) {
            if (value == null) return;
            int channelI = Integer.parseInt(value);
            if (channelI < mJam.getChannels().size()) {
                mJam.getChannels().get(channelI).clearNotes();
            }
            return;
        }

        if (name.equals("ON_NEW_LOOP")) {
            if (mSync) {
                mJam.setSubbeatLength(mPeerJam.getSubbeatLength());
                if (mJam.isPaused()) {
                    mJam.kickIt();
                }
                else {
                    mJam.syncNow();
                }
                mSync = false;
            }
            return;
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
                setChannels += "|";
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

        String surfaceURL = channel.getSurfaceURL();
        String surface = "0";
        if ("PRESET_SEQUENCER".equals(surfaceURL))
            surface = "0";
        if ("PRESET_VERTICAL".equals(surfaceURL))
            surface = "1";
        if ("PRESET_FRETBOARD".equals(surfaceURL))
            surface = "2";

        return  (channel.isEnabled() ? "1," : "0,") +
                (channel.getSoundSet().isChromatic() ? "1," : "0,") +
                surface + "," + channel.getSoundSetName() + "," + channel.getVolume() + "," + channel.getPan();
    }

    private void sendChannelInfo() {
        if (mChannel == null)
            return;

        Channel channel = mChannel;

        String drumbeatInfo = getDrumbeatInfo(channel) + ";";
        mConnection.sendNameValuePair("DRUMBEAT_INFO", drumbeatInfo);

        String fretboardInfo = channel.getLowNote() + "," +
                channel.getHighNote() + "," + channel.getOctave() + getCaptions() +  ";";
        mConnection.sendNameValuePair("FRETBOARD_INFO", fretboardInfo);

        String noteInfo = getNoteInfo(channel) + ";";
        mConnection.sendNameValuePair("NOTE_INFO", noteInfo);
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
        StringBuilder info = new StringBuilder();
        int subbeatTotal = mJam.getTotalSubbeats();
        int channels = Math.min(channel.pattern.length, channel.getSoundSet().getSounds().size());
        for (int i = 0; i < channels; i++) {

            info.append(channel.getSoundSet().getSounds().get(i).getName()).append("|");

            for (int j = 0; j < subbeatTotal; j++) {
                if (j >= channel.pattern[i].length)
                    break;

                info.append(channel.pattern[i][j] ? "1" : "0");
                if (j < channel.pattern[i].length - 1) {
                    info.append("|");
                }

            }

            if (i < channel.pattern.length - 1) {
                info.append(",");
            }
        }
        return info.toString();
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
        if (channelInfo != null) {
            channelInfo.split("");
        }
    }

    private void assurePeerJam() {
        if (mPeerJam == null) {
            mPeerJam = new JamInfo();
        }
    }

    boolean isSynced() {
        return mSync;
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

    private void sendSavedJams() {
        Cursor cursor = mDatabase.getSavedData().getSavedCursor();
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

    private void sendSoundSets() {
        Cursor cursor = mDatabase.getSoundSetData().getCursor();
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

    private void addChannel(long soundSetId) {

        SoundSet soundSet = mDatabase.getSoundSetData().getSoundSetById(soundSetId);
        mJam.newChannel(soundSet);
    }

    private void loadJam(long jamId) {
        SavedDataOpenHelper dataHelper = mDatabase.getSavedData();
        mContext.loadJam(dataHelper.getJamJson(jamId));
    }

    private String getCaptions() {
        if (mChannel == null || mChannel.getSoundSet() == null) {
            return "";
        }

        if (mChannel.getSoundSet().isChromatic()) {
            return "";
        }

        String[] captions = mChannel.getSoundSet().getSoundNames();
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int i = 0; i < captions.length; i++) {
            sb.append(captions[i]);
            if (i < captions.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private void setChannelVolume(String params) {
        try {
            String[] data = params.split(",");
            float volume = Float.parseFloat(data[0]);
            int channel = Integer.parseInt(data[1]);

            mJam.setChannelVolume(channel, volume, mConnection.getDevice().getAddress());
            //mJam.getChannels().get(channel).setVolume(volume);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setChannelPan(String params) {
        try {
            String[] data = params.split(",");
            float pan = Float.parseFloat(data[0]);
            int channel = Integer.parseInt(data[1]);

            mJam.setChannelPan(channel, pan, mConnection.getDevice().getAddress());
            //mJam.getChannels().get(channel).setPan(pan);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setChannelEnabled(String params) {
        try {
            String[] data = params.split(",");
            int enabled = Integer.parseInt(data[0]);
            int channel = Integer.parseInt(data[1]);

            mJam.setChannelEnabled(channel, enabled != 0, mConnection.getDevice().getAddress());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getChannelEnabledCommand(int channelNumber, boolean enabled) {
        return "CHANNEL_ENABLED=" + (enabled?"1,":"0,") + channelNumber;
    }
    static String getChannelVolumeCommand(int channelNumber, float volume) {
        return "CHANNEL_VOLUME=" + volume + "," + channelNumber;
    }
    static String getChannelPanCommand(int channelNumber, float pan) {
        return "CHANNEL_PAN=" + pan + "," + channelNumber;
    }

    void setSync(boolean sync) {
        mSync = sync;
    }

    private void onSetArpNotes(String value) {
        if (value == null || mChannel == null) return;

        try {
            String[] notePairs = value.split("\\|");
            String[] basicAndInstrument;
            Note[] notes = new Note[notePairs.length];
            int i = 0;
            for (String notePair : notePairs) {
                basicAndInstrument = notePair.split(",");
                notes[i] = new Note();
                notes[i].setBasicNote(Integer.parseInt(basicAndInstrument[0]));
                notes[i].setInstrumentNote(Integer.parseInt(basicAndInstrument[1]));
                i++;
            }
            mChannel.setArpNotes(notes);
        }
        catch (Exception e) {
            Log.e("MGH CommandProcessor", e.getMessage());
        }
    }
}
