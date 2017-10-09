package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

class Channel {

    private static int STATE_LIVEPLAY = 0;
    private static int STATE_PLAYBACK = 1;

    private int arpeggiate= -1;

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

    private String mSurfaceURL = "";

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

        playingNoteNumber = note.getScaledNote();

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
            //Log.d("MGH noteToPlay", Integer.toString(noteToPlay));

            if (noteToPlay >= 0 && noteToPlay < ids.length) {
                noteHandle = mPool.play(ids[noteToPlay], volume, volume, 1, 0, 1);
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

        Log.d("MGH recording", "start");

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
        if (recordingNote == null)
            return;

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

        recordingNote.setBeats(beats);

        mNoteList.overwrite(recordingNote, startBeat);

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

    String getMainSound() {
        return mMainSound;
    }

    void prepareSoundSetFromURL(String url) {
        SoundSetDataOpenHelper dataHelper = new SoundSetDataOpenHelper(context);
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

        //creates a new array from the old one
        if (pattern.length != mSoundSet.getSounds().size()) {
            pattern = new boolean[mSoundSet.getSounds().size()][mJam.getTotalSubbeats()];
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
        sb.append("\", \"scale\": \"");
        sb.append(mJam.getScaleString());
        sb.append("\", \"ascale\": [");
        sb.append(mJam.getScaleString());
        sb.append("], \"rootNote\": ");
        sb.append(mJam.getKey());
        sb.append(", \"octave\": ");
        sb.append(getOctave());
        sb.append(", \"volume\": ");
        sb.append(volume);
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
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i][subbeat]) {

                    if (i < ids.length)
                        playingId = mPool.play(ids[i], volume, volume, 10, 0, 1);
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

        if (mSoundSet != null && mSoundSet.isChromatic())
            return "PRESET_VERTICAL";

        return "PRESET_SEQUENCER";
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
}
