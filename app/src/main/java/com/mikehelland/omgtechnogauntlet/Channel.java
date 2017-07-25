package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class Channel {

    private boolean wasSetup = false;

    public static int STATE_LIVEPLAY = 0;
    public static int STATE_PLAYBACK = 1;

    protected int state = 1;

    protected boolean enabled = true;

    private long finishAt;

    protected Note lastPlayedNote;
    protected int playingNoteNumber = -1;

    protected int highNote = 0;
    protected int lowNote = 0;

    protected boolean isAScale = true;

    protected NoteList mNoteList = new NoteList();
    //protected NoteList mBasicMelody = new NoteList();

    protected int playingI = 0;

    protected OMGSoundPool mPool;

    protected int[] ids;
    protected int[] rids;
    //protected String[] captions;

    protected int playingId = -1;

    protected Context context;

    protected float volume = 0.75f;

    protected int octave = 3;

    protected Note recordingNote;
    protected int recordingStartedAtSubbeat;

    protected Jam mJam;

    protected double nextBeat = 0.0d;

    protected int subbeats;
    protected int mainbeats;
    protected int totalsubbeats;
    private double dsubbeats;

    ArrayList<DebugTouch> debugTouchData = new ArrayList<DebugTouch>();

    private String mType;
    protected SoundSet mSoundSet;
    private String mMainSound;

    private String mSurfaceURL = "PRESET_SEQUENCER";

    protected boolean[][] pattern;

    public Channel(Context context, Jam jam, OMGSoundPool pool) {
        mPool = pool; //new OMGSoundPool(8, AudioManager.STREAM_MUSIC, 0);
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


        mType = type;
        mMainSound = sound;
        //mSoundSetName = sound;

        setup();
    }

    private void setup() {
        subbeats = mJam.getSubbeats();
        mainbeats = mJam.getBeats();
        totalsubbeats = subbeats * mainbeats;
        dsubbeats = (double)subbeats;
    }

    public int playLiveNote(Note note) {
        return playLiveNote(note, false);
    }

    public int playLiveNote(Note note, boolean multiTouch) {

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

    public void playRecordedNote(Note note) {
        if (state == STATE_PLAYBACK && enabled) {
            playNote(note, false);
        }
    }

    public int playNote(Note note, boolean multiTouch) {

        if (lastPlayedNote != null)
            lastPlayedNote.isPlaying(false);

        //enabled used to be "mute", now it's for recording
        //we always want the note to be played if this is called
        //if (!enabled)
        //    return -1;


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

    public void stopWithHandle(int handle) {
        mPool.stop(handle);
    }

    public boolean toggleEnabled() {
        if (enabled) {
            disable();
        }
        else {
            enable();
        }
        return enabled;
    }

    public void disable() {
        enabled = false;
        mute();
    }

    public void enable() {
        enabled = true;
    }

    public void finishCurrentNoteAt(long time) {
        finishAt = time;
    }

    public long getFinishAt() {
        return finishAt;
    }

    public void mute() {

        if (playingId > -1) {
            mPool.pause(playingId);
            mPool.stop(playingId);
            playingId = -1;
        }

    }


    public int getHighNote() {
        return highNote;
    }

    public int getLowNote() {
        return lowNote;
    }

    public boolean isAScale() {
        return isAScale;
    }

    public int getPlayingNoteNumber() {
        return playingNoteNumber;
    }

    public NoteList getNotes() {
        return mNoteList;
    }


    public void fitNotesToInstrument() {

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

    public int getInstrumentNoteNumber(int scaledNote) {
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

    public double getNextBeat() {
        return nextBeat;
    }

    public void setNextBeat(double beats) {
        nextBeat = beats;
        playingI++;
    }

    public int getI() {
        return playingI;
    }

    public void resetI() {
        nextBeat = 0.0d;
        playingI = 0;

        if (recordingNote == null)
            state = STATE_PLAYBACK;
    }

    public int getState() {
        return state;
    }

    public void clearNotes() {
        mNoteList.clear();
        state = STATE_LIVEPLAY;

        if (!mSoundSet.isChromatic()) {
            clearPattern();
        }
    }

    public int getSoundCount() {
        return rids.length;
    }

    public int getOctave() {
        return octave;
    }


    public void startRecordingNote(Note note) {

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

    public void stopRecording() {
        Log.d("MGH recording", "stop1");

        if (recordingNote == null)
            return;

        Log.d("MGH recording", "stop2");

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

        Log.d("MGH dsubbeats", Double.toString(dsubbeats));
        double beats = (nowSubbeat - recordingStartedAtSubbeat) / dsubbeats;
        double startBeat = recordingStartedAtSubbeat / dsubbeats;

        Log.d("MGH recordingnote", Integer.toString(recordingNote.getInstrumentNote()));


        recordingNote.setBeats(beats);

        mNoteList.overwrite(recordingNote, startBeat);

        String mynotes = "";
        for (Note debugNote : mNoteList) {
            mynotes = mynotes + debugNote.getInstrumentNote();
            if (debugNote.isRest())
                mynotes = mynotes + "R";
            mynotes = mynotes + "=" + debugNote.getBeats() + ":";
        }
        Log.d("MGH notelist", mynotes);

        recordingNote = null;
        recordingStartedAtSubbeat = -1;
    }

    public float getVolume() {
        return volume;
    }

    public String getSoundSetName() {
        return mSoundSet.getName();
    }
    public String getSoundSetURL() {
        return mSoundSet.getURL();
    }

    public String getType() {
        return mType;
    }

    public String getMainSound() {
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
        mSoundSet = soundset;

        return loadSoundSet();
    }
    boolean loadSoundSet() {

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


    public SoundSet getSoundSet() {
        return mSoundSet;
    }

    public void getData(StringBuilder sb) {
        if (!mSoundSet.isChromatic()) {
            getDrumData(sb);
        }
        else {
            getNotesData(sb);
        }
    }

    private void getNotesData(StringBuilder sb) {
        sb.append("{\"type\" : \"");
        sb.append(getType());
        sb.append("\", \"soundsetName\": \"");
        sb.append(getSoundSetName());
        sb.append("\", \"soundsetURL\": \"");
        sb.append(getSoundSetURL());
        sb.append("\", \"surfaceURL\" : \"");
        sb.append(mSurfaceURL);
        sb.append("\", \"scale\": \"");
        sb.append(mJam.getScale());
        sb.append("\", \"rootNote\": ");
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
        sb.append("]}");

    }

    public void playBeat(int subbeat) {

        if (!mSoundSet.isChromatic()) {
            playDrumBeat(subbeat);
            return;
        }

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

    public void playDrumBeat(int subbeat) {
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

    public void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
    }

    public void setPattern(boolean[][] pattern) {
        Log.d("MGH", "set pattern " + pattern.length);
        this.pattern = pattern;
    }

    public boolean[] getTrack(int track)  {
        return pattern[track];
    }

    public void setPattern(int track, int subbeat, boolean value) {
        pattern[track][subbeat] = value;
    }

    public void setSurface(String surfaceURL) {
        mSurfaceURL = surfaceURL;
    }

    public String getSurfaceURL() {
        return mSurfaceURL;
    }
}
