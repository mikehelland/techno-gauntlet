package com.mikehelland.omgtechnogauntlet;

import android.util.Log;

import java.util.ArrayList;

class Channel {

    private static int STATE_LIVEPLAY = 0;
    private static int STATE_PLAYBACK = 1;

    private int arpeggiate = 0;

    private int state = 1;

    private boolean enabled = true;

    private long finishAt;

    private Note lastPlayedNote;

    private int highNote = 0;
    private int lowNote = 0;

    private boolean isAScale = true;

    private NoteList mNoteList = new NoteList();

    private int playingI = 0;

    private OMGSoundPool mPool;

    private  int[] ids = {};

    private int playingId = -1;

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

    private Surface mSurface;

    boolean[][] pattern;
    private float volume = 0.75f;
    private float leftVolume = 0.75f;
    private float rightVolume = 0.75f;
    private float pan = 0f;
    private float mSampleSpeed = 1;

    private Note[] arpNotes = new Note[10];
    private int nextArpNote = 0;
    private int arpNotesCount = 0;

    public Channel(Jam jam, OMGSoundPool pool) {
        mPool = pool;
        mJam = jam;

        mSoundSet = new SoundSet();
        mSoundSet.setName("DRUMBEAT");
        mSoundSet.setURL("");

        mSurface = new Surface();

        pattern = new boolean[8][256]; // use a high limit [mJam.getTotalSubbeats()];

        setup();
    }

    private void setup() {
        subbeats = mJam.getSubbeats();
        totalsubbeats = subbeats * mJam.getTotalBeats();
        dsubbeats = (double)subbeats;
    }

    void playLiveNote(Note note) {
        playLiveNote(note, false);
    }

    int playLiveNote(Note note, boolean multiTouch) {

        if (mSoundSet.isOscillator()) {
            mPool.makeSureDspIsRunning();
            mSoundSet.getOscillator().unmute();
        }

        int noteHandle = playNote(note, multiTouch);

        if (note.isRest()) {
            arpeggiate = 0;
            arpNotesCount = 0;
            stopRecording();
            state = STATE_PLAYBACK;
        }
        else {
            if (mJam.isPlaying()) {
                if (arpeggiate == 0) {
                    if (enabled) {
                        startRecordingNote(note);
                    }
                }
                else {
                    if (arpNotesCount < arpNotes.length) {
                        arpNotes[arpNotesCount] = note;
                        arpNotesCount++;
                    }
                    note.setBeats(arpeggiate / dsubbeats);
                    if (enabled) {
                        recordNote(note, mJam.getCurrentSubbeat());
                    }
                }
            }
            state = STATE_LIVEPLAY;
        }

        return noteHandle;
    }

    private int playNote(Note note, boolean multiTouch) {

        if (lastPlayedNote != null)
            lastPlayedNote.isPlaying(false);

        //enabled used to be "mute", now it's for recording
        //we always want the note to be played if this is called
        //if (!enabled)
        //    return -1;

        note.isPlaying(true);
        lastPlayedNote = note;

        if (mSoundSet.isOscillator()) {
            return mSoundSet.getOscillator().playNote(note, multiTouch);
        }

        finishAt = -1;

        if (playingId > -1 && (note.isRest() || !multiTouch)) {
            mute();
        }

        int noteHandle = -1;
        if (!note.isRest()) {
            int noteToPlay = note.getInstrumentNote();
            if (noteToPlay >= 0 && noteToPlay < ids.length) {
                noteHandle = mPool.play(ids[noteToPlay], leftVolume, rightVolume, 1, 0, mSampleSpeed);
                playingId = noteHandle;
            }

        }
        return noteHandle;
    }

    void stopWithHandle(int handle) {
        mPool.pause(handle);
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
            //mPool.setVolume(playingId, -100, -100);
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

    private double getNextBeat() {
        return nextBeat;
    }

    private void setNextBeat(double beats) {
        nextBeat = beats;
        playingI++;
    }

    public int getI() {
        return playingI;
    }

    void resetI() {
        nextBeat = 0.0d;
        playingI = 0;

        if (recordingNote == null && arpeggiate == 0)
            state = STATE_PLAYBACK;
    }


    void clearNotes() {
        mNoteList.clear();
        clearPattern();
    }

    int getOctave() {
        return octave;
    }

    private void startRecordingNote(Note note) {

        DebugTouch debugTouch = new DebugTouch();
        debugTouch.mode = "START";
        debugTouchData.add(debugTouch);

        int subbeat = mJam.getClosestSubbeat(debugTouch);

        if (recordingNote != null) {
            stopRecording();
        }

        recordingStartedAtSubbeat = subbeat;
        recordingNote = note;

    }

    private void stopRecording() {

        DebugTouch debugTouch = new DebugTouch();
        debugTouch.mode = "STOP";
        debugTouchData.add(debugTouch);

        int subbeat = mJam.getClosestSubbeat(debugTouch);

        if (subbeat < recordingStartedAtSubbeat) {
            subbeat += totalsubbeats;
        }
        if (subbeat - recordingStartedAtSubbeat < 2) {
            subbeat = recordingStartedAtSubbeat + 2;
        }

        double beats = (subbeat - recordingStartedAtSubbeat) / dsubbeats;
        double startBeat = recordingStartedAtSubbeat / dsubbeats;

        synchronized (this) {
            if (recordingNote == null)
                return;

            recordingNote.setBeats(beats);

            mNoteList.overwrite(recordingNote, startBeat);
        }

        recordingNote = null;
        recordingStartedAtSubbeat = -1;
    }

    private void recordNote(Note note, int startSubbeat) {
        if (recordingNote != null) {
            stopRecording();
        }

        double startBeat = startSubbeat / dsubbeats;
        mNoteList.overwrite(note, startBeat);

    }

    String getSoundSetName() {
        return mSoundSet.getName();
    }

    void prepareSoundSet(SoundSet soundSet) {

        // first take care of the last soundset
        //todo see if any sounds are running
        if (mSoundSet != null && mSoundSet.isOscillator()) {
            mSoundSet.getOscillator().mute();
        }

        mSoundSet = soundSet;

        //creates a new array from the old one //todo looks like it just blanks it really
        int soundCount = mSoundSet.getSounds().size();
        if (pattern == null || pattern.length != soundCount) {
            pattern = new boolean[soundCount][256];
        }

        for (SoundSet.Sound sound : mSoundSet.getSounds()) {
            mPool.addSoundToLoad(sound);
        }

        if (mSoundSet.isOscillator()) {
            mPool.addDac(mSoundSet.getOscillator().ugDac);
            mPool.makeSureDspIsRunning();
        }

        isAScale = mSoundSet.isChromatic();
        highNote = mSoundSet.getHighNote();
        lowNote = mSoundSet.getLowNote();

        if (isAScale) {
            int bottomC = (lowNote % 12 == 0) ? lowNote :
                    (lowNote + (lowNote % 12));
            int highC = highNote - (highNote % 12);
            int octaves = (bottomC + highC) / 12;
            octaves = (octaves % 2 == 0) ? octaves : (octaves + 1);
            octave = Math.min(highC / 12, Math.max(bottomC / 12, octaves / 2));
        }

        ids = new int[mSoundSet.getSounds().size()];

    }


    boolean loadSoundSetIds() {

        for (int i = 0; i < mSoundSet.getSounds().size(); i++) {
            ids[i] = mPool.getPoolId(mSoundSet.getSounds().get(i).getURL());
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
        sb.append("\", \"soundFont\": ");
        sb.append(mSoundSet.isSoundFont() ? "true" : "false");
        sb.append(", \"soundsetURL\": \"");
        sb.append(mSoundSet.getURL());
        sb.append("\", \"surfaceURL\" : \"");
        sb.append(getSurfaceURL());
        sb.append("\", \"volume\": ");
        sb.append(volume);
        sb.append(", \"pan\": ");
        sb.append(pan);
        sb.append(", \"sampleSpeed\": ");
        sb.append(mSampleSpeed);
        if (!enabled)
            sb.append(", \"mute\": true");

        if (useSequencer()) {
            getTrackData(sb);
        } else {
            getNoteData(sb);
        }

        sb.append("}");
    }

    private void getNoteData(StringBuilder sb) {

        sb.append(", \"scale\": \"");
        sb.append(mJam.getScaleString());
        sb.append("\", \"ascale\": [");
        sb.append(mJam.getScaleString());
        sb.append("], \"rootNote\": ");
        sb.append(mJam.getKey());
        sb.append(", \"octave\": ");
        sb.append(getOctave());
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
    }

    private void getTrackData(StringBuilder sb) {
        sb.append(", \"tracks\": [");

        ArrayList<SoundSet.Sound> sounds = mSoundSet.getSounds();
        for (int p = 0; p < sounds.size(); p++) {

            sb.append("{\"name\": \"");
            sb.append(sounds.get(p).getName());
            sb.append("\", \"sound\": \"");
            sb.append(sounds.get(p).getURL());
            sb.append("\", \"data\": [");
            for (int i = 0; i < mJam.getTotalSubbeats(); i++) {
                sb.append(pattern[p][i] ? 1 : 0);
                if (i < mJam.getTotalSubbeats() - 1)
                    sb.append(",");
            }
            sb.append("]}");

            if (p < sounds.size() - 1)
                sb.append(",");

        }

        sb.append("]");
    }

    void playBeat(int subbeat) {

        if (useSequencer()) {
            playDrumBeat(subbeat);
            return;
        }

        if (playArpeggiator(subbeat)) {
            return;
        }

        if (state != Channel.STATE_PLAYBACK)
            return;

        int i = getI();
        if (i <  getNotes().size()) {
            if (getNextBeat() == subbeat / (double)subbeats) {
                Note note = getNotes().get(i);

                if (enabled) {
                    playNote(note, false);
                    finishCurrentNoteAt(System.currentTimeMillis() +
                            (long) (note.getBeats() * 4 * mJam.getSubbeatLength()) - 50);
                }

                setNextBeat(getNextBeat() +  note.getBeats());

            }
        }

    }

    private void playDrumBeat(int subbeat) {
        if (enabled) {
            for (int i = 0; i < pattern.length; i++) {
                try {
                    if (pattern[i][subbeat]) {
                        if (i < ids.length)
                            playingId = mPool.play(ids[i], leftVolume, rightVolume, 10, 0, mSampleSpeed);
                    }
                }
                catch (Exception excp) {
                    excp.printStackTrace();
                }
            }
        }
    }

    private boolean playArpeggiator(int subbeat) {
        if (state == Channel.STATE_LIVEPLAY && arpeggiate > 0 &&
                subbeat % arpeggiate == 0) {

            if (arpNotesCount > 0) {
                if (nextArpNote >=  arpNotesCount) {
                    nextArpNote = 0;
                }

                if (nextArpNote < arpNotes.length && arpNotes[nextArpNote] != null) {
                    Note note = arpNotes[nextArpNote].cloneNote();
                    nextArpNote++;

                    playNote(note, false);
                    finishCurrentNoteAt(System.currentTimeMillis() +
                            (long) (arpeggiate * mJam.getSubbeatLength()) - 50);

                    note.setBeats(arpeggiate / dsubbeats);

                    if (enabled) {
                        recordNote(note, subbeat);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
    }

    void setPattern(boolean[][] pattern) {
        this.pattern = pattern;
    }

    void setPattern(int track, int subbeat, boolean value) {
        if (track >= pattern.length || track < 0 ||
                subbeat >= pattern[track].length || subbeat < 0) {
            Log.e("MGH Channel setPattern", "Illegal arguments: track=" + track + ", subbeat=" + subbeat);
        }
        else {
            pattern[track][subbeat] = value;
        }
    }

    void setSurface(Surface surface) {
        mSurface = surface;
    }

    Surface getSurface() {return mSurface;}

    String getSurfaceURL() {
        if (mSurface != null && mSurface.getURL().length() > 0)
            return mSurface.getURL();

        if (mSoundSet != null) {
            if (mSoundSet.getDefaultSurface().length() > 0)
                return mSoundSet.getDefaultSurface();

            if (mSoundSet.isChromatic())
                return SufacesDataHelper.PRESET_VERTICAL;
        }

        return SufacesDataHelper.PRESET_SEQUENCER;
    }

    String getSurfaceJSON() {
        return null;
    }

    boolean isEnabled() {
        return enabled;
    }

    void setArpeggiator(int newValue) {
        arpeggiate = newValue;
        if (newValue == 0) {
            arpNotesCount = 0;
        }
    }

    void finish() {
        if (mSoundSet.isOscillator()) {
            mSoundSet.getOscillator().finish();
        }
    }
    void setVolume(float volume) {
        this.volume = volume;
        calculateStereoVolume();
    }
    float getVolume() {
        return volume;
    }
    void setPan(float pan) {
        this.pan = pan;
        calculateStereoVolume();
    }
    float getPan() {return pan;}
    private void calculateStereoVolume() {
        float cross = (pan + 1) / 2;
        final double halfPi = Math.PI / 2;
        leftVolume = volume * (float)Math.sin((1 - cross) * halfPi);
        rightVolume = volume * (float)Math.sin(cross * halfPi);

        if (mSoundSet != null && mSoundSet.isOscillator()) {
            mSoundSet.getOscillator().ugDac.setVolume(leftVolume, rightVolume);
        }
    }

    void setSampleSpeed(float sampleSpeed) {
        mSampleSpeed = sampleSpeed;
    }

    float getSampleSpeed() {
        return mSampleSpeed;
    }

    void setArpNotes(Note[] notes) {
        arpNotesCount = 0;
        for (int i = 0; i < notes.length; i++) {
            if (i < arpNotes.length) {
                arpNotes[i] = notes[i];
                arpNotesCount++;
            }
        }
    }

    void setNotes(NoteList notes) {
        mNoteList = notes;
    }

    boolean useSequencer() {
        return mSurface.getURL().equals(SufacesDataHelper.PRESET_SEQUENCER);
    }
}
