package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothDataCallback;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.Part;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

/**
 * Created by m on 7/31/16.
 * the layer between the bluetooth connection and the jam
 */
class CommandProcessor extends BluetoothDataCallback {

    final static String JAMINFO_SUBBEATLENGTH = "JAMINFO_SUBBEATLENGTH";
    final static String JAMINFO_KEY = "JAMINFO_KEY";
    final static String JAMINFO_SCALE = "JAMINFO_SCALE";
    private final static String CHANNEL_SET_ARPNOTES = "CHANNEL_SET_ARPNOTES";

    private BluetoothConnection mConnection;
    private Jam mJam;
    private Part mPart = null;

    private Jam mPeerJam;
    //private OnPeerChangeListener mOnPeerChangeListener;

    final private DatabaseContainer mDatabase;
    final private Main mContext;

    private boolean mSync = false;

    CommandProcessor(Context context) {
        //todo storing the context because I need to load the jam
        //maybe this should be done somehow else
        mContext = (Main)context;
        mDatabase = mContext.getDatabase();
    }

    void setup(BluetoothConnection connection, Jam jam, Part channel) {
        mJam = jam;
        mConnection = connection;

        //can be null
        mPart = channel;

        //todo put this back when we uncomment the rest
        // sendJamInfo();
    }

    Jam getPeerJam() {
        return mPeerJam;
    }


    @Override
    public void newData(String name, String value) {

        Log.d("MGH BT newdata", name + (value != null ? ("=" + value) : ""));

        switch (name) {
            case "SET_PLAY":
                mJam.play();
                return;
            case "SET_STOP":
                mJam.stop();
                return;
            case "GET_JAM_INFO":
                sendJamInfo();
                return;
            case "GET_JAM_JSON":
                sendJamJSON();
                return;
            case "GET_SAVED_JAMS":
                sendSavedJams();
                return;
            case "GET_SOUNDSETS":
                sendSoundSets();
                return;
            case "ON_NEW_LOOP":
                onNewLoop();
                return;
        }

        // the rest of the cases need a value
        if (value == null)
            return;

        switch (name) {
            case "ADD_CHANNEL":
                addPart(Long.parseLong(value));
                return;
            case "LOAD_JAM":
                loadJam(Long.parseLong(value));
                return;
            case "SET_SUBBEATLENGTH":
                mJam.setSubbeatLength(Integer.parseInt(value), mConnection.getDevice().getAddress());
                return;
            case "SET_KEY":
                mJam.setKey(Integer.parseInt(value), mConnection.getDevice().getAddress());
                return;
            case "SET_SCALE":
                //todo mJam.setScale(value, mConnection.getDevice().getAddress());
                return;
            case "SET_CHANNEL":
                //todo mPart = mJam.getPartByID(value);
                sendPartInfo();
                return;
            case "SET_CHORD":
                int chordI = Integer.parseInt(value);
                int[] chords = {chordI};
                mJam.setProgression(chords);
                return;
            case "SET_CHANNEL_VOLUME":
                setPartVolume(value);
                return;
            case "SET_CHANNEL_PAN":
                setPartPan(value);
                return;
            case "SET_CHANNEL_ENABLED":
                setPartEnabled(value);
                return;
            case "CLEAR_CHANNEL":
                clearPart(value);
                return;

            case JAMINFO_KEY:
                onSetKey(value);
                return;
            case JAMINFO_SCALE:
                onSetScale(value);
                return;
            case JAMINFO_SUBBEATLENGTH:
                onSetSubbeatLength(value);
                return;
            case "JAMINFO_CHANNELS":
                //onSetParts(value);
                return;
            case "JAM_JSON":
                //onSetParts(value);
                mJam.loadFromJSON(value);
                return;
        }

        // the rest the cases need a channel
        if (mPart == null)
            return;

        switch (name) {

            case "CHANNEL_PLAY_NOTE":
                channelPlayNote(value);
                return;
            case "CHANNEL_SET_PATTERN":
                channelSetPattern(value);
                return;
            case "CHANNEL_SET_ARPEGGIATOR":
                //todo mPart.setArpeggiator(Integer.parseInt(value));
                break;
            case CHANNEL_SET_ARPNOTES:
                onSetArpNotes(value);
                break;
        }
    }

    private void channelSetPattern(String value) {
        String[] params = value.split(",");
        int track = Integer.parseInt(params[0]);
        int subbeat = Integer.parseInt(params[1]);
        boolean patternValue = params[2].equals("true");
        //todo mPart.setPattern(track, subbeat, patternValue);
    }

    private void channelPlayNote(String value) {
        //todo
        /*Note note = new Note();
        String[] noteInfo = value.split(",");
        int basicNoteNumber = Integer.parseInt(noteInfo[0]);
        int instrumentNoteNumber = Integer.parseInt(noteInfo[1]);

        note.setInstrumentNote(instrumentNoteNumber);
        note.setBasicNote(basicNoteNumber);
        if (instrumentNoteNumber == -1) {
            note.setRest(true);
        }

        mPart.playLiveNote(note);*/
    }

    private void sendJamInfo() {
        //should this be JSON? Maybe faster this way tho
        //this is the old way for the old remote app
        mConnection.sendNameValuePair(JAMINFO_KEY, Integer.toString(mJam.getKey()));
        //todo? mConnection.sendNameValuePair(JAMINFO_SCALE, mJam.getScaleString());
        mConnection.sendNameValuePair(JAMINFO_SUBBEATLENGTH,
                Integer.toString(mJam.getSubbeatLength()));

        String setParts = getPartsInfo(mJam);
        mConnection.sendNameValuePair("JAMINFO_CHANNELS", setParts);

        mConnection.sendCommand(mJam.isPlaying() ? "PLAY" : "STOP");
    }


    private void sendJamJSON() {
        mConnection.sendNameValuePair("JAM_JSON", mJam.getData());

        //todo send when we started, or at least do the on new loop regular updates
        mConnection.sendCommand(mJam.isPlaying() ? "PLAY" : "STOP");
    }

    static private String getPartsInfo(Jam jam) {
        StringBuilder setParts = new StringBuilder();
        for (int i = 0; i < jam.getParts().size(); i++) {
            Part channel = jam.getParts().get(i);

            CommandHelper.getPartInfo(setParts, channel);

            if (i < jam.getParts().size() - 1) {
                setParts.append("|");
            }
        }
        return setParts.toString();
    }



    private void sendPartInfo() {
        if (mPart == null)
            return;

        Part channel = mPart;

        String drumbeatInfo = getDrumbeatInfo(channel) + ";";
        mConnection.sendNameValuePair("DRUMBEAT_INFO", drumbeatInfo);

        String fretboardInfo = channel.getSoundSet().getLowNote() + "," +
                channel.getSoundSet().getHighNote() + "," + channel.getOctave() + getCaptions() +  ";";
        mConnection.sendNameValuePair("FRETBOARD_INFO", fretboardInfo);

        String noteInfo = getNoteInfo(channel) + ";";
        mConnection.sendNameValuePair("NOTE_INFO", noteInfo);
    }

    private String getNoteInfo(Part channel) {

        StringBuilder info = new StringBuilder();
        int i = 0;
        for (Note note : channel.getNotes()) {
            info.append(note.isRest() ? "-" : "");
            info.append(note.getBeats());
            info.append("|");
            info.append(note.getInstrumentNote());
            if (i++ < channel.getNotes().size() - 1) {
                info.append(",");
            }
        }
        return info.toString();
    }

    private String getDrumbeatInfo(Part channel) {
        StringBuilder info = new StringBuilder();
        int subbeatTotal = mJam.getTotalSubbeats();
        int channels = Math.min(channel.getPattern().length, channel.getSoundSet().getSounds().size());
        for (int i = 0; i < channels; i++) {

            info.append(channel.getSoundSet().getSounds().get(i).getName()).append("|");

            for (int j = 0; j < subbeatTotal; j++) {
                if (j >= channel.getPattern()[i].length)
                    break;

                info.append(channel.getPattern()[i][j] ? "1" : "0");
                if (j < channel.getPattern()[i].length - 1) {
                    info.append("|");
                }

            }

            if (i < channel.getPattern().length - 1) {
                info.append(",");
            }
        }
        return info.toString();
    }

    private void onSetKey(String key) {
        assurePeerJam();
        //mPeerJam.setKey(Integer.parseInt(key));
        //if (mOnPeerChangeListener != null) mOnPeerChangeListener.onChange(mPeerJam);
    }
    private void onSetScale(String scale) {
        assurePeerJam();
        //mPeerJam.setScale(scale);
        //if (mOnPeerChangeListener != null) mOnPeerChangeListener.onChange(mPeerJam);
    }
    private void onSetSubbeatLength(String subbeatLength) {
        assurePeerJam();
        //mPeerJam.setSubbeatLength(Integer.parseInt(subbeatLength));
        //if (mOnPeerChangeListener != null) mOnPeerChangeListener.onChange(mPeerJam);
    }

    private void onSetParts(String channelInfo) {
        if (channelInfo != null) {
            channelInfo.split("");
        }
    }
    static String getPartsInfoCommand(Jam jam) {
        return "SET_CHANNELS=" + getPartsInfo(jam);
    }

    private void assurePeerJam() {
        //if (mPeerJam == null) {
        //    mPeerJam = new JamInfo();
        // }
    }

    boolean isSynced() {
        return mSync;
    }

    private void sendSavedJams() {
        /*Cursor cursor = mDatabase.getSavedData().getSavedCursor();
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
        mConnection.sendNameValuePair("SAVED_JAMS", value.toString());*/
    }

    private void sendSoundSets() {
        /*Cursor cursor = mDatabase.getSoundSetData().getCursor();
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
        mConnection.sendNameValuePair("SOUNDSETS", value.toString());*/
    }

    private void addPart(long soundSetId) {

        SoundSet soundSet = mDatabase.getSoundSetData().getSoundSetById(soundSetId);
        if (soundSet != null) {
            mJam.newPart(soundSet);
        }
    }

    private void loadJam(long jamId) {
        SavedDataOpenHelper dataHelper = mDatabase.getSavedData();
        //todo mContext.loadJam(dataHelper.getJamJson(jamId));
    }

    private String getCaptions() {
        if (mPart == null || mPart.getSoundSet() == null) {
            return "";
        }

        if (mPart.getSoundSet().isChromatic()) {
            return "";
        }

        String[] captions = mPart.getSoundSet().getSoundNames();
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

    private void setPartVolume(String params) {
        try {
            String[] data = params.split(",");
            float volume = Float.parseFloat(data[0]);

            //todo mJam.setPartVolume(data[1], volume, mConnection.getDevice().getAddress());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setPartPan(String params) {
        try {
            String[] data = params.split(",");
            float pan = Float.parseFloat(data[0]);

            //todo mJam.setPartPan(data[1], pan, mConnection.getDevice().getAddress());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setPartEnabled(String params) {
        try {
            String[] data = params.split(",");
            boolean on = !data[0].equals("0");

            //todo mJam.setPartEnabled(data[1], on, mConnection.getDevice().getAddress());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setSync(boolean sync) {
        mSync = sync;
    }

    private void onSetArpNotes(String value) {
        try {
            String[] notePairs = value.split("\\|");
            String[] basicAndInstrument;
            Note[] notes = new Note[notePairs.length];
            int i = 0;
            /*for (String notePair : notePairs) {
                basicAndInstrument = notePair.split(",");
                notes[i] = new Note();
                notes[i].setBasicNote(Integer.parseInt(basicAndInstrument[0]));
                notes[i].setInstrumentNote(Integer.parseInt(basicAndInstrument[1]));
                i++;
            }
            mPart.setArpNotes(notes);*/
        }
        catch (Exception e) {
            Log.e("MGH CommandProcessor", e.getMessage());
        }
    }

    private void onNewLoop() {
        if (mSync) {
            /*mJam.setSubbeatLength(mPeerJam.getSubbeatLength());
            if (mJam.isPaused()) {
                mJam.kickIt();
            } else {
                mJam.syncNow();
            }*/
            mSync = false;
        }
    }

    private void clearPart(String value) {
        /*Part channel = mJam.getPartByID(value);
        if (channel != null) {
            channel.clearNotes();
        }*/

    }
}
