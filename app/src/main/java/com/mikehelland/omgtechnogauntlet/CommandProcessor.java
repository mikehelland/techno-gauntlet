package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothDataCallback;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

/**
 * Created by m on 7/31/16.
 * the layer between the bluetooth connection and the jam
 */
class CommandProcessor extends BluetoothDataCallback {

    final static String REMOTE_CONTROL = "REMOTE_CONTROL";

    final static String JAMINFO_KEY = "JAMINFO_KEY";
    final static String JAMINFO_SCALE = "JAMINFO_SCALE";
    private final static String CHANNEL_SET_ARPNOTES = "CHANNEL_SET_ARPNOTES";

    final static String SET_PLAY = "SET_PLAY";
    final static String SET_STOP = "SET_STOP";

    final static String SET_SUBBEATLENGTH = "SET_SUBBEATLENGTH";
    final static String SET_KEY = "SET_KEY";
    final static String SET_SCALE = "SET_SCALE";

    final static String SET_PART_TRACK_VALUE = "SET_PART_TRACK_VALUE";
    final static String SET_PART_LIVE_START = "SET_PART_LIVE_START";
    final static String SET_PART_LIVE_UPDATE = "SET_PART_LIVE_UPDATE";
    final static String SET_PART_LIVE_REMOVE = "SET_PART_LIVE_REMOVE";
    final static String SET_PART_LIVE_END = "SET_PART_LIVE_END";

    final static String SET_PART_PAN = "SET_PART_PAN";
    final static String SET_PART_MUTE = "SET_PART_MUTE";
    final static String SET_PART_WARP = "SET_PART_WARP";
    final static String SET_PART_VOLUME = "SET_PART_VOLUME";
    final static String SET_PART_TRACK_PAN = "SET_PART_TRACK_PAN";
    final static String SET_PART_TRACK_MUTE = "SET_PART_TRACK_MUTE";
    final static String SET_PART_TRACK_WARP = "SET_PART_TRACK_vWARP";
    final static String SET_PART_TRACK_VOLUME = "SET_PART_TRACK_VOLUME";

    private BluetoothConnection mConnection;
    private Jam mJam;
    private JamPart mPart = null;

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

    void setup(BluetoothConnection connection, Jam jam, JamPart channel) {
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
            case REMOTE_CONTROL:
                mSync = true;
                sendJamJSON();
                return;

            case SET_PLAY:
                if (mSync) {
                    mJam.play(getAddress());
                }
                return;
            case SET_STOP:
                if (mSync) {
                    mJam.stop(getAddress());
                }
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
            case SET_SUBBEATLENGTH:
                if (mSync) {
                    mJam.setSubbeatLength(Integer.parseInt(value), getAddress());
                }
                return;
            case SET_KEY:
                if (mSync) {
                    mJam.setKey(Integer.parseInt(value), getAddress());
                }
                return;
            case SET_SCALE:
                if (mSync) {
                    setScale(value);
                }
                return;

            case SET_PART_TRACK_VALUE:
                if (mSync) {
                    setPartTrackValue(value);
                }
                return;
            case SET_PART_LIVE_START:
                if (mSync) {
                    onPartLiveStart(value);
                }
                return;
            case SET_PART_LIVE_UPDATE:
                if (mSync) {
                    onPartLiveUpdate(value);
                }
                return;
            case SET_PART_LIVE_REMOVE:
                if (mSync) {
                    onPartLiveRemove(value);
                }
                return;
            case SET_PART_LIVE_END:
                if (mSync) {
                    onPartLiveEnd(value);
                }
                return;

            case "ADD_CHANNEL":
                addPart(Long.parseLong(value));
                return;
            case "LOAD_JAM":
                loadJam(Long.parseLong(value));
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

            case SET_PART_VOLUME:
                setPartVolume(value);
                return;
            case SET_PART_PAN:
                setPartPan(value);
                return;
            case SET_PART_MUTE:
                setPartMute(value);
                return;
            case SET_PART_WARP:
                setPartWarp(value);
                return;

            case "CLEAR_CHANNEL":
                clearPart(value);
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

    //old way, old remote
    private void channelSetPattern(String value) {
        String[] params = value.split(",");
        int track = Integer.parseInt(params[0]);
        int subbeat = Integer.parseInt(params[1]);
        boolean patternValue = params[2].equals("true");

        if (mSync) {
            mJam.setPartTrackValue(mPart, track, subbeat, patternValue, getAddress());
        }
    }

    private void channelPlayNote(String value) {
        //todo for old omg-remote
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


    private void sendJamJSON() {
        mConnection.sendNameValuePair("JAM_JSON", mJam.getData());

        //todo send when we started, or at least do the on new loop regular updates
        mConnection.sendCommand(mJam.isPlaying() ? SET_PLAY : SET_STOP);
    }

    static private String getPartsInfo(Jam jam) {
        StringBuilder setParts = new StringBuilder();
        for (int i = 0; i < jam.getParts().size(); i++) {
            JamPart channel = jam.getParts().get(i);

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

        JamPart channel = mPart;

        String drumbeatInfo = getDrumbeatInfo(channel) + ";";
        mConnection.sendNameValuePair("DRUMBEAT_INFO", drumbeatInfo);

        String fretboardInfo = channel.getSoundSet().getLowNote() + "," +
                channel.getSoundSet().getHighNote() + "," + channel.getOctave() + getCaptions() +  ";";
        mConnection.sendNameValuePair("FRETBOARD_INFO", fretboardInfo);

        String noteInfo = getNoteInfo(channel) + ";";
        mConnection.sendNameValuePair("NOTE_INFO", noteInfo);
    }

    private String getNoteInfo(JamPart channel) {

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

    private String getDrumbeatInfo(JamPart channel) {
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
            float volume = Float.parseFloat(data[1]);

            mJam.setPartVolume(mJam.getPart(data[0]), volume, getAddress());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setPartPan(String params) {
        try {
            String[] data = params.split(",");
            float pan = Float.parseFloat(data[1]);

            mJam.setPartPan(mJam.getPart(data[0]), pan, getAddress());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setPartMute(String params) {
        try {
            String[] data = params.split(",");
            boolean on = !data[1].equals("0");

            mJam.setPartMute(mJam.getPart(data[0]), on, getAddress());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setPartWarp(String params) {
        try {
            String[] data = params.split(",");
            float speed = Float.parseFloat(data[1]);

            mJam.setPartWarp(mJam.getPart(data[0]), speed, mConnection.getDevice().getAddress());

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

    private String getAddress() {
        return mConnection.getDevice().getAddress();
    }



    private void setPartTrackValue(String value) {
        String[] params = value.split(",");
        JamPart jamPart = mJam.getPart(params[0]);

        int track = Integer.parseInt(params[1]);
        int subbeat = Integer.parseInt(params[2]);
        boolean patternValue = !params[3].equals("0");

        if (mSync) {
            mJam.setPartTrackValue(jamPart, track, subbeat, patternValue, getAddress());
        }
    }
    private void onPartLiveStart(String value) {

        String[] noteInfo = value.split(",");
        int autoBeat = Integer.parseInt(noteInfo[1]);
        int basicNoteNumber = Integer.parseInt(noteInfo[2]);
        int instrumentNoteNumber = Integer.parseInt(noteInfo[3]);

        Note note = new Note(false, basicNoteNumber, 0, instrumentNoteNumber, -1);

        mJam.startPartLiveNotes(mJam.getPart(noteInfo[0]), note, autoBeat, getAddress());
    }

    private void onPartLiveUpdate(String value) {

        String[] noteInfo = value.split(",");
        int autoBeat = Integer.parseInt(noteInfo[1]);

        int i = 2;
        int noteI = 0;
        int basicNote;
        int instrumentNote;
        Note[] notes = new Note[(noteInfo.length - 2) / 2];
        while (i < noteInfo.length) {
            basicNote = Integer.parseInt(noteInfo[i++]);
            instrumentNote = Integer.parseInt(noteInfo[i++]);
            notes[noteI++] = new Note(false,
                    basicNote, 0, instrumentNote, -1);
        }

        mJam.updatePartLiveNotes(mJam.getPart(noteInfo[0]), notes, autoBeat, getAddress());
    }

    private void onPartLiveRemove(String value) {

        String[] noteInfo = value.split(",");
        int basicNoteNumber = Integer.parseInt(noteInfo[1]);
        int instrumentNoteNumber = Integer.parseInt(noteInfo[2]);

        Note note = new Note(false, basicNoteNumber, 0, instrumentNoteNumber, -1);

        int i = 3;
        int noteI = 0;
        int basicNote;
        int instrumentNote;
        Note[] notes = new Note[(noteInfo.length - 3) / 2];
        while (i < noteInfo.length) {
            basicNote = Integer.parseInt(noteInfo[i++]);
            instrumentNote = Integer.parseInt(noteInfo[i++]);
            notes[noteI++] = new Note(false,
                    basicNote, 0, instrumentNote, -1);
        }

        mJam.removeFromPartLiveNotes(mJam.getPart(noteInfo[0]), note, notes, getAddress());
    }

    private void onPartLiveEnd(String value) {
        mJam.endPartLiveNotes(mJam.getPart(value), getAddress());
    }

    private void setScale(String value) {
        String[] ints = value.split(",");
        int[] scale = new int[ints.length];
        for (int i = 0; i < ints.length; i++) {
            scale[i] = Integer.parseInt(ints[i]);
        }
        mJam.setScale(scale, getAddress());
    }
}
