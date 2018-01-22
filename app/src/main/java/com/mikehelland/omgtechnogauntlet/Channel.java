package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
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

    private String mSurfaceURL = "";

    boolean[][] pattern;
    private float volume = 0.75f;
    private float leftVolume = 0.75f;
    private float rightVolume = 0.75f;
    private float pan = 0f;

    public Channel(Context context, Jam jam, OMGSoundPool pool) {
        mPool = pool;
        this.context = context;
        mJam = jam;

        mSoundSet = new SoundSet();
        mSoundSet.setName("DRUMBEAT");
        mSoundSet.setURL("");
        mSoundSet.setChromatic(false);

        pattern = new boolean[8][256]; // use a high limit [mJam.getTotalSubbeats()];

        setup();
    }

    private void setup() {
        subbeats = mJam.getSubbeats();
        totalsubbeats = subbeats * mJam.getTotalBeats();
        dsubbeats = (double)subbeats;
    }

    int playLiveNote(Note note) {
        return playLiveNote(note, false);
    }

    int playLiveNote(Note note, boolean multiTouch) {

        if (mSoundSet.isOscillator()) {
            mPool.makeSureDspIsRunning();
            mSoundSet.getOscillator().unmute();
        }

        int noteHandle = playNote(note, multiTouch);

        if (note.isRest()) {
            arpeggiate = 0;
            stopRecording();
            state = STATE_PLAYBACK;
        }
        else {
            if (mJam.isPlaying() && enabled) {
                if (arpeggiate == 0)
                    startRecordingNote(note);
                else {
                    note.setBeats(arpeggiate / dsubbeats);
                    recordNote(note, mJam.getCurrentSubbeat());
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

        if (mSoundSet.isOscillator()) {
            return mSoundSet.getOscillator().playNote(note, multiTouch);
        }

        note.isPlaying(true);
        lastPlayedNote = note;

        finishAt = -1;

        if (playingId > -1 && (note.isRest() || !multiTouch)) {
            mute();
            /*mPool.setVolume(playingId, 0.01f, 0.01f);
            mPool.pause(playingId);
            mPool.stop(playingId);
            playingId = -1;*/
        }

        int noteHandle = -1;
        if (!note.isRest()) {
            int noteToPlay = note.getInstrumentNote();
            if (noteToPlay >= 0 && noteToPlay < ids.length) {
                noteHandle = mPool.play(ids[noteToPlay], leftVolume, rightVolume, 1, 0, 1);
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

    void prepareSoundSetFromURL(String url) {
        SoundSetDataOpenHelper dataHelper = ((Main)context).getDatabase().getSoundSetData();
        SoundSet soundSet = dataHelper.getSoundSetByURL(url);
        if (soundSet != null) {
            prepareSoundSet(soundSet);
        }
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
        int totalSubbeats = mJam.getTotalSubbeats();
        if (pattern == null || pattern.length != soundCount) {
            pattern = new boolean[soundCount][256]; //[totalSubbeats];
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
        sb.append("\", \"soundsetURL\": \"");
        sb.append(mSoundSet.getURL());
        sb.append("\", \"surfaceURL\" : \"");
        sb.append(getSurfaceURL());
        sb.append("\", \"volume\": ");
        sb.append(volume);
        sb.append(", \"pan\": ");
        sb.append(pan);
        if (!enabled)
            sb.append(", \"mute\": true");

        if (getSurfaceURL().equals("PRESET_SEQUENCER")) {
            getTrackData(sb);
        } else {
            getNoteData(sb);
        }

        sb.append("}");
    }

    void getNoteData(StringBuilder sb) {

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

    void getTrackData(StringBuilder sb) {
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

        if (getSurfaceURL().equals("PRESET_SEQUENCER")) {
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
            //TODO this crashes when, say a 1 beat pattern is played in a jam with more than 1 beat
            // the only real way fix it is change the pattern to the right length at a synchronized time
            for (int i = 0; i < pattern.length; i++) {
                try {
                    if (pattern[i][subbeat]) {
                        if (i < ids.length)
                            playingId = mPool.play(ids[i], leftVolume, rightVolume, 10, 0, 1);
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

            if (lastPlayedNote != null && !lastPlayedNote.isRest() ) {
                Note note = lastPlayedNote.cloneNote();

                playNote(note, false);
                finishCurrentNoteAt(System.currentTimeMillis() +
                        (long) (arpeggiate * mJam.getSubbeatLength()) - 50);

                note.setBeats(arpeggiate / dsubbeats);

                if (enabled) {
                    recordNote(note, subbeat);
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
        if (mSurfaceURL.length() > 0)
            return mSurfaceURL;

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
    }

    void updateLiveNote(Note note) {
        lastPlayedNote = note;
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
}
