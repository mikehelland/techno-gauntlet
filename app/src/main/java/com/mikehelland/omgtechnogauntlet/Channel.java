package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

class Channel {

    static int STATE_LIVEPLAY = 0;
    static int STATE_PLAYBACK = 1;

    private int state = 1;

    private boolean enabled = true;

    private long finishAt;

    private Note lastPlayedNote;
    private int playingNoteNumber = -1;

    private int highNote = 0;
    private int lowNote = 0;

    private boolean isAScale = true;

    private NoteList mNoteList = new NoteList();

    private int playingI = 0;

    private OMGSoundPool mPool;

    private  int[] ids;

    private int playingId = -1;

    private Context context;

    private int octave = 3;

    private Note recordingNote;
    private int recordingStartedAtSubbeat;

    private Jam mJam;

    private double nextBeat = 0.0d;

    private int subbeats;
    private int totalsubbeats;
    private double dsubbeats;

    ArrayList<DebugTouch> debugTouchData = new ArrayList<>();

    private SoundSet mSoundSet;
    private String mMainSound;

    private String mSurfaceURL = "PRESET_SEQUENCER";

    boolean[][] pattern;
    float volume = 0.75f;

    public Channel(Context context, Jam jam, OMGSoundPool pool) {
        mPool = pool;
        this.context = context;
        mJam = jam;

        pattern = new boolean[8][mJam.getSubbeats() * mJam.getBeats()];

        mSoundSet = new SoundSet();
        mSoundSet.setName("DRUMBEAT");
        mSoundSet.setURL("");
        mSoundSet.setChromatic(false);


        setup();
    }

    public Channel(Context context, Jam jam, OMGSoundPool pool, String type, String sound) {

        mSoundSet = new SoundSet();
        mSoundSet.setName(type);
        mSoundSet.setURL(sound);
        mSoundSet.setChromatic(true);


        mPool = pool; //new OMGSoundPool(8, AudioManager.STREAM_MUSIC, 0);
        this.context = context;
        mJam = jam;

        pattern = new boolean[8][mJam.getSubbeats() * mJam.getBeats()];


        mMainSound = sound;
        //mSoundSetName = sound;

        setup();
    }

    private void setup() {
        subbeats = mJam.getSubbeats();
        totalsubbeats = subbeats * mJam.getBeats();
        dsubbeats = (double)subbeats;
    }

    int playLiveNote(Note note) {
        return playLiveNote(note, false);
    }

    int playLiveNote(Note note, boolean multiTouch) {

        if (mSoundSet.isOscillator()) {
            mSoundSet.getOscillator().unmute();
        }

        int noteHandle = playNote(note, multiTouch);

        if (!mJam.isPlaying() || !enabled)
            return noteHandle;

        if (note.isRest()) {
            stopRecording();
            state = STATE_PLAYBACK;
        }
        else {
            startRecordingNote(note);
            state = STATE_LIVEPLAY;
        }

        return noteHandle;
    }

    void playRecordedNote(Note note) {
        if (state == STATE_PLAYBACK && enabled) {
            playNote(note, false);
        }
    }

    int playNote(Note note, boolean multiTouch) {

        if (lastPlayedNote != null)
            lastPlayedNote.isPlaying(false);

        //enabled used to be "mute", now it's for recording
        //we always want the note to be played if this is called
        //if (!enabled)
        //    return -1;

        if (mSoundSet.isOscillator()) {
            return mSoundSet.getOscillator().playNote(note, multiTouch);
        }

        playingNoteNumber = note.getScaledNote();

        note.isPlaying(true);
        lastPlayedNote = note;

        if (playingId > -1 && (note.isRest() || !multiTouch)) {
            mPool.pause(playingId);
            mPool.stop(playingId);
            playingId = -1;
        }

        finishCurrentNoteAt(-1);

        int noteHandle = -1;
        if (!note.isRest()) {
            int noteToPlay = note.getInstrumentNote();
            //Log.d("MGH noteToPlay", Integer.toString(noteToPlay));

            if (noteToPlay >= 0 && noteToPlay < ids.length) {
                noteHandle = mPool.play(ids[noteToPlay], volume, volume, 10, 0, 1);
                playingId = noteHandle;
            }

        }
        return noteHandle;
    }

    void stopWithHandle(int handle) {
        mPool.stop(handle);
    }

    boolean toggleEnabled() {
        if (enabled) {
            disable();
        }
        else {
            enable();
        }
        return enabled;
    }

    void disable() {
        enabled = false;
        mute();
    }

    void enable() {
        enabled = true;
    }

    void finishCurrentNoteAt(long time) {
        finishAt = time;
    }

    long getFinishAt() {
        return finishAt;
    }

    void mute() {

        if (mSoundSet.isOscillator()) {
            mSoundSet.getOscillator().mute();
        }

        if (playingId > -1) {
            mPool.pause(playingId);
            mPool.stop(playingId);
            playingId = -1;
        }

    }


    int getHighNote() {
        return highNote;
    }

    int getLowNote() {
        return lowNote;
    }

    boolean isAScale() {
        return isAScale;
    }

    int getPlayingNoteNumber() {
        return playingNoteNumber;
    }

    NoteList getNotes() {
        return mNoteList;
    }


    void fitNotesToInstrument() {

        for (Note note : mNoteList) {

            int noteToPlay = note.getScaledNote() + 12 * octave;
            while (noteToPlay < lowNote) {
                noteToPlay += 12;
            }
            while (noteToPlay > highNote) {
                noteToPlay -= 12;
            }

            note.setInstrumentNote(noteToPlay - lowNote);
        }

        state = STATE_PLAYBACK;
    }

    int getInstrumentNoteNumber(int scaledNote) {
        int noteToPlay = scaledNote + octave * 12;

        while (noteToPlay < lowNote) {
            noteToPlay += 12;
        }
        while (noteToPlay > highNote) {
            noteToPlay -= 12;
        }

        noteToPlay -= lowNote;

        return noteToPlay;
    }

    double getNextBeat() {
        return nextBeat;
    }

    void setNextBeat(double beats) {
        nextBeat = beats;
        playingI++;
    }

    public int getI() {
        return playingI;
    }

    void resetI() {
        nextBeat = 0.0d;
        playingI = 0;

        if (recordingNote == null)
            state = STATE_PLAYBACK;
    }

    int getState() {
        return state;
    }

    void clearNotes() {
        mNoteList.clear();
        state = STATE_LIVEPLAY;

        if (!mSoundSet.isChromatic()) {
            clearPattern();
        }
    }

    int getOctave() {
        return octave;
    }

    void startRecordingNote(Note note) {

        Log.d("MGH recording", "start");

        if (recordingNote != null) {
            stopRecording();
        }

        DebugTouch debugTouch = new DebugTouch();
        debugTouch.mode = "START";
        debugTouchData.add(debugTouch);

        recordingStartedAtSubbeat = mJam.getClosestSubbeat(debugTouch);

        recordingNote = note;

    }

    void stopRecording() {
        if (recordingNote == null)
            return;

        DebugTouch debugTouch = new DebugTouch();
        debugTouch.mode = "STOP";
        debugTouchData.add(debugTouch);

        int nowSubbeat = mJam.getClosestSubbeat(debugTouch);

        if (nowSubbeat < recordingStartedAtSubbeat) {
            nowSubbeat += totalsubbeats;
        }
        if (nowSubbeat - recordingStartedAtSubbeat < 2) {
            nowSubbeat = recordingStartedAtSubbeat + 2;
        }

        double beats = (nowSubbeat - recordingStartedAtSubbeat) / dsubbeats;
        double startBeat = recordingStartedAtSubbeat / dsubbeats;

        recordingNote.setBeats(beats);

        mNoteList.overwrite(recordingNote, startBeat);

        String mynotes = "";
        for (Note debugNote : mNoteList) {
            mynotes += debugNote.getInstrumentNote();
            if (debugNote.isRest())
                mynotes += "R";
            mynotes = mynotes + "=" + debugNote.getBeats() + ":";
        }

        recordingNote = null;
        recordingStartedAtSubbeat = -1;
    }

    float getVolume() {
        return volume;
    }

    String getSoundSetName() {
        return mSoundSet.getName();
    }
    String getSoundSetURL() {
        return mSoundSet.getURL();
    }

    String getMainSound() {
        return mMainSound;
    }


    int prepareSoundSet(long id) {
        SoundSetDataOpenHelper dataHelper = new SoundSetDataOpenHelper(context);
        SoundSet soundset = dataHelper.getSoundSetById(id);
        if (soundset == null) {
            Toast.makeText(context, dataHelper.getLastErrorMessage(), Toast.LENGTH_SHORT).show();
            return 0;
        }

        return prepareSoundSet(soundset);
    }

    int prepareSoundSet(String url) {
        SoundSetDataOpenHelper dataHelper = new SoundSetDataOpenHelper(context);
        SoundSet soundset = dataHelper.getSoundSetByURL(url);
        if (soundset == null) {
            return 0;
        }

        return prepareSoundSet(soundset);
    }


    int prepareSoundSet(SoundSet soundset) {
        mSoundSet = soundset;
        return soundset.getSounds().size();
    }

    boolean loadSoundSet(SoundSet soundset) {

        if (mSoundSet != null && mSoundSet.isOscillator()) {
            mSoundSet.getOscillator().mute();
        }

        mSoundSet = soundset;

        return loadSoundSet();
    }
    boolean loadSoundSet() {

        if (mSoundSet.isOscillator()) {
            mPool.addDac(mSoundSet.getOscillator().ugDac);
            mPool.makeSureDspIsRunning();;
        }

        String path = context.getFilesDir() + "/" + Long.toString(mSoundSet.getID()) + "/";
        isAScale = mSoundSet.isChromatic();
        highNote = mSoundSet.getHighNote();
        lowNote = mSoundSet.getLowNote();

        int preset_id;
        ArrayList<SoundSet.Sound> sounds = mSoundSet.getSounds();

        ids = new int[sounds.size()];

        for (int i = 0; i < sounds.size(); i++) {
            SoundSet.Sound sound = mSoundSet.getSounds().get(i);

            if (sound.isPreset()) {
                preset_id = sound.getPresetId();

                ids[i] = mPool.load(sound.getURL(), context, preset_id, 1);
            }
            else {

                ids[i] = mPool.load(sound.getURL(), path + Integer.toString(i), 1);
            }
        }

        return true;
    }


    SoundSet getSoundSet() {
        return mSoundSet;
    }

    void getData(StringBuilder sb) {

        sb.append("{\"type\" : \"PART");
        sb.append("\", \"soundsetName\": \"");
        sb.append(getSoundSetName());
        sb.append("\", \"soundsetURL\": \"");
        sb.append(getSoundSetURL());
        sb.append("\", \"surfaceURL\" : \"");
        sb.append(mSurfaceURL);
        sb.append("\", \"scale\": \"");
        sb.append(mJam.getScaleString());
        sb.append("\", \"ascale\": [");
        sb.append(mJam.getScaleString());
        sb.append("], \"rootNote\": ");
        sb.append(mJam.getKey());
        sb.append(", \"octave\": ");
        sb.append(getOctave());
        sb.append(", \"volume\": ");
        sb.append(getVolume());
        if (!enabled)
            sb.append(", \"mute\": true");
        sb.append(", \"notes\" : [");

        boolean first = true;
        for (Note note : getNotes()) {

            if (first)
                first = false;
            else
                sb.append(", ");

            sb.append("{\"rest\": ");
            sb.append(note.isRest());
            sb.append(", \"beats\": ");
            sb.append(note.getBeats());
            if (!note.isRest()) {
                sb.append(", \"note\" :");
                sb.append(note.getBasicNote());
            }
            sb.append("}");
        }
        sb.append("]");

        if (!mSoundSet.isChromatic()) {
            sb.append(", \"tracks\": [");

            ArrayList<SoundSet.Sound> sounds = mSoundSet.getSounds();
            for (int p = 0; p < sounds.size(); p++) {

                sb.append("{\"name\": \"");
                sb.append(sounds.get(p).getName());
                sb.append("\", \"sound\": \"");
                sb.append(sounds.get(p).getURL());
                sb.append("\", \"data\": [");
                for (int i = 0; i < pattern[p].length; i++) {
                    sb.append(pattern[p][i] ? 1 : 0);
                    if (i < pattern[p].length - 1)
                        sb.append(",");
                }
                sb.append("]}");

                if (p < sounds.size() - 1)
                    sb.append(",");

            }

            sb.append("]");
        }

        sb.append("}");

    }

    void playBeat(int subbeat) {

        //if (!mSoundSet.isChromatic()) {
        playDrumBeat(subbeat);
        //    return;
        //}

        if (getState() != Channel.STATE_PLAYBACK)
            return;

        int i = getI();
        if (i <  getNotes().size()) {
            //Log.d("MGH next beat", Double.toString(subbeat / mJam.getBeats()));
            if (getNextBeat() == subbeat / (double)subbeats) {
                Note note = getNotes().get(i);

                playRecordedNote(note);
                finishCurrentNoteAt(System.currentTimeMillis() +
                        (long)(note.getBeats() * 4 * mJam.getSubbeatLength()) - 50);

                setNextBeat(getNextBeat() +  note.getBeats());

            }
        }

    }

    void playDrumBeat(int subbeat) {
        if (enabled) {
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i][subbeat]) {

                    if (i < ids.length)
                        playingId = mPool.play(ids[i], volume, volume, 10, 0, 1);
                }
            }
        }
    }


    private void getDrumData(StringBuilder sb) {

        int beats = mJam.getBeats();
        int totalBeats = beats * subbeats;
        sb.append("{\"type\" : \"DRUMBEAT\", \"soundsetName\": \"");
        sb.append(mSoundSet.getName());
        sb.append("\", \"soundsetURL\" : \"");
        sb.append(mSoundSet.getURL());
        sb.append("\", \"surfaceURL\" : \"");
        sb.append(mSurfaceURL);

        sb.append("\", \"volume\": ");
        sb.append(volume);
        if (!enabled)
            sb.append(", \"mute\": true");

        sb.append(", \"tracks\": [");

        ArrayList<SoundSet.Sound> sounds = mSoundSet.getSounds();
        for (int p = 0; p < sounds.size(); p++) {

            sb.append("{\"name\": \"");
            sb.append(sounds.get(p).getName());
            sb.append("\", \"sound\": \"");
            sb.append(sounds.get(p).getURL());
            sb.append("\", \"data\": [");
            for (int i = 0; i < totalBeats; i++) {
                sb.append(pattern[p][i] ?1:0) ;
                if (i < totalBeats - 1)
                    sb.append(",");
            }
            sb.append("]}");

            if (p < sounds.size() - 1)
                sb.append(",");

        }

        sb.append("]}");

    }

    void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
    }

    void setPattern(boolean[][] pattern) {
        Log.d("MGH", "set pattern " + pattern.length);
        this.pattern = pattern;
    }

    boolean[] getTrack(int track)  {
        return pattern[track];
    }

    void setPattern(int track, int subbeat, boolean value) {
        pattern[track][subbeat] = value;
    }

    void setSurface(String surfaceURL) {
        mSurfaceURL = surfaceURL;
    }

    String getSurfaceURL() {
        return mSurfaceURL;
    }

    boolean isEnabled() {
        return enabled;
    }
}
