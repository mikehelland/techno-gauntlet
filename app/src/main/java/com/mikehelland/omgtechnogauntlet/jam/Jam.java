package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class Jam {

    //this gets set to false when acting as a remote control
    boolean localPlayBack = true;

    private Song currentSong;
    private Section currentSection;
    private int currentSectionI = 0;
    private Player player;
    private SoundManager soundManager;

    private boolean isPlaying = false;

    private CopyOnWriteArrayList<OnJamChangeListener> onJamChangeListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnKeyChangeListener> onKeyChangeListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnBeatChangeListener> onBeatChangeListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<OnMixerChangeListener> onMixerChangeListeners = new CopyOnWriteArrayList<>();
    private SoundSetsProvider soundSetsProvider;

    private String keyName = "";

    private CopyOnWriteArrayList<JamPart> jamParts = new CopyOnWriteArrayList<>();

    public Jam(SoundManager soundManager, SoundSetsProvider soundSetsProvider) {
        this.soundManager = soundManager;
        this.soundSetsProvider = soundSetsProvider;
        this.player = new Player(this, soundManager);
    }

    public String loadRemoteFromJSON(String json) {
        localPlayBack = false;
        return loadJSON(json);
    }

    public String loadFromJSON(String json) {
        return loadFromJSON(json, null);
    }

    public String loadFromJSON(String json, String source) {

        String errorMessage = loadJSON(json);
        if (errorMessage != null) {
            return errorMessage;
        }

        updateKeyName();

        if (soundManager != null) {
            for (Section section : currentSong.sections) {
                for (Part part : section.parts) {
                    prepareSoundSetForPart(part);
                }
            }

            //load them off the UI thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    soundManager.loadSounds();

                    for (Section section : currentSong.sections) {
                        for (Part part : section.parts) {
                            setPoolIdsForPart(part);
                        }
                    }
                }
            }).start();
        }
        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onNewJam(this, source);
        }

        return null; //all seems good, no error message and the jam is loading
    }

    private String loadJSON(String json) {
        Log.d("MGH loadJSON", json);
        try {
            currentSong = JamLoader.load(json);
            currentSection = currentSong.sections.get(0);

            jamParts.clear();
            for (Part part : currentSection.parts) {
                jamParts.add(new JamPart(part));
            }
        } catch (JamLoaderException e) {
            return e.message;
        }
        return null; //all seems good, no error message and the jam is loading
    }

    public String getData() {
        return SectionToJSON.getData(currentSection);
    }

    public String getTags() {
        return currentSection.tags;
    }

    public void setTags(String tags) {
        currentSection.tags = tags;
    }

    // Main Controls //

    public int getCurrentSubbeat() {
        return player.getCurrentSubbeat();
    }

    public boolean isPlaying() {
        return isPlaying;
        //return player.isPlaying();
    }

    public void play() {
        play(null);
    }

    public void play(String source) {
        if (!isPlaying) {
            isPlaying = true;
            player.play(currentSection, jamParts);

            for (OnJamChangeListener listener : onJamChangeListeners) {
                listener.onPlay(source);
            }
        }
    }

    public void stop() {
        stop(null);
    }

    public void stop(String source) {
        if (isPlaying) {
            isPlaying = false;
            player.stop();

            for (OnJamChangeListener listener : onJamChangeListeners) {
                listener.onStop(source);
            }
        }
    }

    public void finish() {
        player.finish();
        //player.cleanUp();
        //todo unref everything here
    }


    // BEAT functions //

    public int getSubbeatLength() {
        return currentSection.beatParameters.subbeatLength;
    }

    public void setSubbeatLength(int subbeatLength, String sourceDevice) {
        currentSection.beatParameters.subbeatLength = subbeatLength;

        for (OnBeatChangeListener listener : onBeatChangeListeners) {
            listener.onSubbeatLengthChange(subbeatLength, sourceDevice);
        }
    }

    public void setBeats(int beats) {
        setBeats(beats, null);
    }

    public void setMeasures(int measures) {
        setMeasures(measures, null);
    }

    public void setShuffle(float shuffle) {
        setShuffle(shuffle, null);
    }

    public void setBeats(int beats, String source) {
        currentSection.beatParameters.beats = beats;
        for (OnBeatChangeListener listener : onBeatChangeListeners) {
            listener.onBeatsChange(beats, source);
        }
    }

    public void setMeasures(int measures, String source) {
        currentSection.beatParameters.measures = measures;
        for (OnBeatChangeListener listener : onBeatChangeListeners) {
            listener.onMeasuresChange(measures, source);
        }
    }

    public void setShuffle(float shuffle, String source) {
        currentSection.beatParameters.shuffle = shuffle;
        for (OnBeatChangeListener listener : onBeatChangeListeners) {
            listener.onShuffleChange(shuffle, source);
        }
    }

    public int getSubbeats() {
        return currentSection.beatParameters.subbeats;
    }

    public int getBeats() {
        return currentSection.beatParameters.beats;
    }

    public int getMeasures() {
        return currentSection.beatParameters.measures;
    }

    public float getShuffle() {
        return currentSection.beatParameters.shuffle;
    }

    //todo set totalSubbeats in the Jam instead of calculating it everytime
    // a couple helper functions
    public int getTotalBeats() {
        return currentSection.beatParameters.beats * currentSection.beatParameters.measures;
    }

    public int getTotalSubbeats() {
        return currentSection.beatParameters.subbeats * currentSection.beatParameters.beats * currentSection.beatParameters.measures;
    }

    public int getBPM() {
        return 60000 / (currentSection.beatParameters.subbeatLength * currentSection.beatParameters.subbeats);
    }

    public void setBPM(float bpm) {
        setSubbeatLength((int) ((60000 / bpm) / currentSection.beatParameters.subbeats), null);
    }


    // KEY FUNCTIONS //

    public void setScale(int[] scale, String sourceDevice) {
        currentSection.keyParameters.scale = scale;
        updateKeyName();

        for (OnKeyChangeListener listener : onKeyChangeListeners) {
            listener.onScaleChange(scale, sourceDevice);
        }
    }

    public void setKey(int key, String sourceDevice) {
        currentSection.keyParameters.rootNote = key;
        updateKeyName();

        for (OnKeyChangeListener listener : onKeyChangeListeners) {
            listener.onKeyChange(key, sourceDevice);
        }
    }

    public int[] getScale() {
        return currentSection.keyParameters.scale;
    }

    public int getKey() {
        return currentSection.keyParameters.rootNote;
    }

    public String getKeyName() {
        return keyName;
    }

    private void updateKeyName() {
        keyName = KeyHelper.getKeyName(currentSection.keyParameters.rootNote, currentSection.keyParameters.scale);
    }

    // MIXER functions //

    public void setPartMute(JamPart jamPart, boolean mute, String device) {
        jamPart.part.audioParameters.mute = mute;

        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartMuteChanged(jamPart, mute, device);
        }
    }

    public void setPartVolume(JamPart jamPart, float volume, String device) {
        jamPart.part.audioParameters.volume = volume;
        if (jamPart.getSoundSet().isOscillator()) {
            jamPart.getSoundSet().getOscillator().ugEnvA.setGain(volume);
        }

        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartVolumeChanged(jamPart, volume, device);
        }
    }

    public void setPartPan(JamPart jamPart, float pan, String device) {
        jamPart.part.audioParameters.pan = pan;

        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartPanChanged(jamPart, pan, device);
        }
    }

    public void setPartWarp(JamPart jamPart, float speed, String source) {
        jamPart.part.audioParameters.speed = speed;

        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartWarpChanged(jamPart, speed, source);
        }
    }

    public void setPartTrackMute(JamPart jamPart, SequencerTrack track, boolean mute, String source) {
        track.audioParameters.mute = mute;
        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartTrackMuteChanged(jamPart, track.getIndex(), mute, source);
        }
    }

    public void setPartTrackVolume(JamPart jamPart, SequencerTrack track, float volume, String source) {
        track.audioParameters.volume = volume;
        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartTrackVolumeChanged(jamPart, track.getIndex(), volume, source);
        }
    }

    public void setPartTrackPan(JamPart jamPart, SequencerTrack track, float pan, String source) {
        track.audioParameters.pan = pan;
        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartTrackPanChanged(jamPart, track.getIndex(), pan, source);
        }
    }

    public void setPartTrackWarp(JamPart jamPart, SequencerTrack track, float warp, String source) {
        track.audioParameters.speed = warp;
        for (OnMixerChangeListener listener : onMixerChangeListeners) {
            listener.onPartTrackWarpChanged(jamPart, track.getIndex(), warp, source);
        }
    }


    public boolean getPartMute(JamPart jamPart) {
        return jamPart.part.audioParameters.mute;
    }

    public float getPartVolume(JamPart jamPart) {
        return jamPart.part.audioParameters.volume;
    }

    public float getPartPan(JamPart jamPart) {
        return jamPart.part.audioParameters.pan;
    }


    // CHORD PROGRESSION stuff


    public int[] getProgression() {
        return currentSection.progression;
    }

    public void setProgression(int[] progression) {
        setProgression(progression, null);
    }
    public void setProgression(int[] progression, String source) {
        currentSection.progression = progression;
        if (progression.length == 1) {
            updateNotesWithChord(currentSection.progression[0]);
        }

        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onChordProgressionChange(progression, source);
        }
    }

    private void updateNotesWithChord(int chord) {
        for (Part part : currentSection.parts) {
            if (part.soundSet.isChromatic()) {
                KeyHelper.applyScaleToPart(part, chord, currentSection.keyParameters);
            }
        }
    }


    public int getChordInProgression() {
        return player.getChordInProgression();
    }




    // PART funcitons //

    public CopyOnWriteArrayList<JamPart> getParts() {
        //todo currentsection jamParts!?
        return jamParts;
    }

    public JamPart getPart(String id) {
        for (JamPart jamPart : jamParts) {
            if (jamPart.getId().equals(id)) {
                return jamPart;
            }
        }
        return null;
    }

    public void removePart(JamPart jamPart) {

    }

    public void clearPart(JamPart jamPart, String source) {
        jamPart.clear();
        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onPartClear(jamPart, source);
        }
    }

    public void copyPart(JamPart jamPart) {

    }


    // ADD and REMOVE LISTENER FUNCTIONS

    public void addOnSubbeatListener(OnSubbeatListener listener) {
        player.onSubbeatListeners.add(listener);
    }

    public void removeOnSubbeatListener(OnSubbeatListener listener) {
        player.onSubbeatListeners.remove(listener);
    }

    public void addOnJamChangeListener(OnJamChangeListener listener) {
        onJamChangeListeners.add(listener);
    }
    public void addOnBeatChangeListener(OnBeatChangeListener listener) {
        onBeatChangeListeners.add(listener);
    }
    public void addOnKeyChangeListener(OnKeyChangeListener listener) {
        onKeyChangeListeners.add(listener);
    }
    public void addOnMixerChangeListener(OnMixerChangeListener listener) {
        onMixerChangeListeners.add(listener);
    }

    public void removeOnJamChangeListener(OnJamChangeListener listener) {
        onJamChangeListeners.remove(listener);
    }
    public void removeOnBeatChangeListener(OnBeatChangeListener listener) {
        onBeatChangeListeners.remove(listener);
    }
    public void removeOnKeyChangeListener(OnKeyChangeListener listener) {
        onKeyChangeListeners.remove(listener);
    }
    public void removeOnMixerChangeListener(OnMixerChangeListener listener) {
        onMixerChangeListeners.remove(listener);
    }

    public boolean isReady() {
        return currentSection != null;
    }


    // LIVE NOTE FUNCTIONS //

    public void startPartLiveNotes(JamPart jamPart, Note  note, int autoBeat) {
        startPartLiveNotes(jamPart, note, autoBeat, null);
    }
    public void startPartLiveNotes(JamPart jamPart, Note  note, int autoBeat, String source) {
        jamPart.stopPlayingSounds();

        LiveNotes liveNotes = new LiveNotes(autoBeat, new Note[] {note});
        jamPart.liveNotes = liveNotes;

        if (!player.isPlaying() || autoBeat == 0) {
            if (soundManager != null) {
                player.playPartLiveNote(jamPart.part, note);
            }
            note.setBeats(1.0f / currentSection.beatParameters.subbeats);
            if (isPlaying && !jamPart.getMute()) {
                liveNotes.liveNote = NoteWriter.addNote(note, Math.max(0, player.isubbeat - 1), jamPart.getNotes(), currentSection.beatParameters);
            }
        }

        note.setBeats(1.0f / currentSection.beatParameters.subbeats);

        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onPartStartLiveNotes(jamPart, note, autoBeat, source);
        }
    }

    public void updatePartLiveNotes(JamPart jamPart, Note[] notes, int autoBeat) {
        updatePartLiveNotes(jamPart, notes, autoBeat, null);
    }
    public void updatePartLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) {
        Log.d("MGH updatelive note", "" + notes[0].getBeats());
        LiveNotes liveNotes = jamPart.liveNotes;
        if (liveNotes == null) {
            return;
        }

        liveNotes.notes =  notes;
        if (!player.isPlaying() || autoBeat == 0) {
            notes[0].setBeats(1.0f / currentSection.beatParameters.subbeats);
            player.playPartLiveNotes(jamPart.part, notes);
            if (isPlaying && !jamPart.getMute()) {
                liveNotes.liveNote = NoteWriter.addNote(notes[0], Math.max(0, player.isubbeat - 1), jamPart.getNotes(), currentSection.beatParameters);
            }
        }
        liveNotes.autoBeat = autoBeat;

        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onPartUpdateLiveNotes(jamPart, notes, autoBeat, source);
        }
    }

    public void removeFromPartLiveNotes(JamPart jamPart, Note note, Note[] notes) {
        removeFromPartLiveNotes(jamPart, note, notes, null);
    }
    public void removeFromPartLiveNotes(JamPart jamPart, Note note, Note[] notes, String source) {
        LiveNotes liveNotes = jamPart.liveNotes;
        if (liveNotes == null) {
            return;
        }

        liveNotes.notes =  notes;
        player.stopPartLiveNote(jamPart.part, note);

        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onPartRemoveLiveNotes(jamPart, note, notes, source);
        }
    }

    public void endPartLiveNotes(JamPart jamPart) {
        endPartLiveNotes(jamPart, null);
    }
    public void endPartLiveNotes(JamPart jamPart, String source) {
        if (jamPart.liveNotes == null) {
            return;
        }
        if (jamPart.liveNotes.notes.length > 0) {
            player.stopPartLiveNote(jamPart.part, jamPart.liveNotes.notes[0]);
        }
        jamPart.liveNotes = null;

        jamPart.partPlayer.nextNoteIndex = -1;

        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onPartEndLiveNotes(jamPart, source);
        }
    }

    public void setPartTrackValue(JamPart jamPart, int track, int subbeat, boolean value) {
        setPartTrackValue(jamPart, track, subbeat, value, null);
    }
    public void setPartTrackValue(JamPart jamPart, int track, int subbeat, boolean value, String source) {
        boolean good = false;
        try {
            jamPart.getPattern()[track][subbeat] = value;
            good = true;
        }
        catch (NullPointerException ignore) { }
        catch (ArrayIndexOutOfBoundsException ignore) { }

        if (good) {
            for (OnJamChangeListener listener : onJamChangeListeners) {
                listener.onPartTrackValueChange(jamPart, track, subbeat, value, source);
            }
        }
    }



    //I'm not too sure these should be here, but where?
    private void prepareSoundSetForPart(Part part) {
        //todo maybe an isloaded instead of isvalid here
        if (!part.soundSet.isValid()) {
            if (soundSetsProvider == null) {
                return;
            }

            SoundSet soundSet = soundSetsProvider.getSoundSetByURL(part.soundSet.getURL());
            if (soundSet == null) {
                return;
            }

            part.soundSet = soundSet;
        }

        if (part.soundSet.isOscillator()) {
            soundManager.addDac(part.soundSet.getOscillator().ugDac);
        }
        else {
            for (SoundSet.Sound sound : part.soundSet.getSounds()) {
                soundManager.addSoundToLoad(sound);
            }
        }
    }
    private void setPoolIdsForPart(Part part) {
        SoundSet.Sound sound;
        int size = part.soundSet.getSounds().size();
        part.poolIds = new int[size];
        for (int i = 0; i < size; i++) {
            sound = part.soundSet.getSounds().get(i);
            part.poolIds[i] = soundManager.getPoolId(sound.getURL());
        }
    }

    public void newPart(SoundSet soundSet) {
        newPart(soundSet, null);
    }

    public void newPart(SoundSet soundSet, String source) {
        Part part = new Part(currentSection);
        part.soundSet = soundSet;
        currentSection.parts.add(part);
        JamPart jamPart = new JamPart(part);
        jamParts.add(jamPart);

        String defaultSurface = soundSet.getDefaultSurface();
        if (defaultSurface != null) {
            part.surface = new Surface(defaultSurface);
        }
        if (jamPart.useSequencer()) {
            setupSequencerPatternForPart(jamPart);
        }

        if (soundManager != null) {
            prepareSoundSetForPart(part);
            loadSounds();
        }

        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onNewPart(jamPart, source);
        }
    }

    private void loadSounds() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                soundManager.loadSounds();

                for (Part part : currentSection.parts) {
                    setPoolIdsForPart(part);
                }
            }
        }).start();
    }

    public void setPartSurface(JamPart jamPart, Surface surface) {
        if (surface != null) {
            jamPart.part.surface = surface;
        }
    }

    public void setupSequencerPatternForPart(JamPart jamPart) {
        jamPart.part.pattern = new boolean[jamPart.part.soundSet.getSounds().size()][];
        int i = 0;
        for (SoundSet.Sound sound : jamPart.part.soundSet.getSounds()) {
            jamPart.getTracks().add(new SequencerTrack(sound.getName(), i));
            jamPart.part.pattern[i] = jamPart.getTracks().get(i).getData();
            i++;
        }
    }

    public void setPartZoom(JamPart part, int skipBottom, int skipTop) {
        part.getSurface().setSkipBottomAndTop(skipBottom, skipTop);
    }

    public void nudgePlayer(int i) {
        player.nudge(i);
    }

    public void restart() {
        Log.d("MGH", "restarting player");
        if (isPlaying()) {
            player.restart();
        }
        else {
            play();
        }
    }

    void onNewLoop() {
        for (OnJamChangeListener listener : onJamChangeListeners) {
            listener.onNewLoop(null);
        }
    }
}
