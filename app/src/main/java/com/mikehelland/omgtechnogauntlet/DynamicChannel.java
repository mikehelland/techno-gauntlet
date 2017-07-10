package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.util.ArrayList;

public abstract class DynamicChannel {

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
    protected String[] captions;

    protected int playingId = -1;

    protected Context context;

    protected float volume;

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
    private String mSoundSetName;
    private String mSoundSetURL;
    private String mMainSound;

    private SoundSet mSoundSet;

    public DynamicChannel(Context context, Jam jam, OMGSoundPool pool) {
        //mPool = pool;
        mPool = pool; new OMGSoundPool(8, AudioManager.STREAM_MUSIC, 0);
        this.context = context;
        mJam = jam;

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

    public void toggleEnabled() {
        if (enabled) {
            disable();
        }
        else {
            enable();
        }
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

    public int loadPool() {

        ids = new int[rids.length];
        for (int i = 0; i < rids.length; i++) {

            ids[i] = mPool.load(context, rids[i], 1);

            if (mPool.isCanceled())
                return -1;

        }


        return ids.length;
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
        return mSoundSetName;
    }
    public String getSoundSetURL() {
        return mSoundSetURL;
    }

    public String getType() {
        return mType;
    }

    public String getMainSound() {
        return mMainSound;
    }


    public boolean loadSoundSet(long id) {

        SoundSetDataOpenHelper dataHelper = new SoundSetDataOpenHelper(context);
        mSoundSet = dataHelper.getSoundSetById(id);

        String path = context.getFilesDir() + "/" + Long.toString(id) + "/";

        int preset_id;
        for (int i = 0; i < mSoundSet.getSounds().size(); i++) {
            SoundSet.Sound sound = mSoundSet.getSounds().get(i);

            captions[i] = sound.getName();

            if (sound.isPreset()) {
                preset_id = sound.getPresetId();

                ids[i] = mPool.load(context, preset_id, 1);
            }
            else {

                ids[i] = mPool.load(path + Integer.toString(i), 1);
            }
        }

        return true;
    }

    public String[] getCaptions() {
        return captions;
    }


}
