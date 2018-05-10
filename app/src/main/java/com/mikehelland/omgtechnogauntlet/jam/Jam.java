package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Jam {

    //this gets set to false when acting as a remote control
    boolean localPlayBack = true;

    private Song currentSong;
    private Section currentSection;
    private int currentSectionI = 0;
    private Player player;
    private SoundManager soundManager;

    private ArrayList<OnJamChangeListener> onJamChangeListeners = new ArrayList<>();
    private OnGetSoundSetListener onGetSoundSetListener;

    private String keyName = "";

    private CopyOnWriteArrayList<JamPart> jamParts = new CopyOnWriteArrayList<>();

    public Jam(SoundManager soundManager, OnGetSoundSetListener onGetSoundSetListener) {
        this.soundManager = soundManager;
        this.onGetSoundSetListener = onGetSoundSetListener;
        this.player = new Player(soundManager);
    }

    public String loadRemoteFromJSON(String json) {
        localPlayBack = false;
        return loadJSON(json);
    }
    public String loadFromJSON(String json) {

        String errorMessage = loadJSON(json);
        if (errorMessage != null) {
            return errorMessage;
        }

        updateKeyName();

        for (Section section: currentSong.sections) {
            for (Part part : section.parts) {
                prepareSoundSetForPart(part);
            }
        }

        //load them off the UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                soundManager.loadSounds();

                for (Section section: currentSong.sections) {
                    for (Part part : section.parts) {
                        setPoolIdsForPart(part);
                    }
                }
            }
        }).start();

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


    public String getTags() {
        return currentSection.tags;
    }

    public void setTags(String tags) {
        currentSection.tags = tags;
    }

    public int getSubbeatLength() {
        return currentSection.beatParameters.subbeatLength;
    }

    public void setSubbeatLength(int subbeatLength, String sourceDevice) {
        currentSection.beatParameters.subbeatLength = subbeatLength;
    }

    public int getSubbeats() {
        return currentSection.beatParameters.subbeats;
    }

    public void setSubbeats(int subbeats) {
        currentSection.beatParameters.subbeats = subbeats;
    }

    public int getBeats() {
        return currentSection.beatParameters.beats;
    }

    public void setBeats(int beats) {
        currentSection.beatParameters.beats = beats;
    }

    public int getMeasures() {
        return currentSection.beatParameters.measures;
    }

    public void setMeasures(int measures) {
        currentSection.beatParameters.measures = measures;
    }

    public float getShuffle() {
        return currentSection.beatParameters.shuffle;
    }

    public void setShuffle(float shuffle) {
        currentSection.beatParameters.shuffle = shuffle;
    }

    public int[] getScale() {
        return currentSection.keyParameters.scale;
    }

    public void setScale(int[] scale, String sourceDevice) {
        currentSection.keyParameters.scale = scale;
        updateKeyName();
    }

    public int getKey() {
        return currentSection.keyParameters.rootNote;
    }

    public void setKey(int key, String sourceDevice) {
        currentSection.keyParameters.rootNote = key;
        updateKeyName();
    }

    public int[] getProgression() {
        return currentSection.progression;
    }

    public void setProgression(int[] progression) {
        currentSection.progression = progression;
        if (progression.length == 1) {
            updateNotesWithChord(currentSection.progression[0]);
        }
    }

    private void updateNotesWithChord(int chord) {
        for (Part part : currentSection.parts) {
            if (part.soundSet.isChromatic()) {
                KeyHelper.applyScaleToPart(currentSection, part, chord);
            }
        }
    }

    public CopyOnWriteArrayList<JamPart> getParts() {
        //todo currentsection jamParts!?
        return jamParts;
    }


    public int getCurrentSubbeat() {
        return player.getCurrentSubbeat();
    }
    public boolean isPlaying() {
        return player.isPlaying();
    }

    //todo set totalSubbeats in the Jam instead of calculating it everytime
    // a couple helper functions
    public int getTotalBeats() {
        return currentSection.beatParameters.beats * currentSection.beatParameters.measures;
    }
    public int getTotalSubbeats() {
        return currentSection.beatParameters.subbeats * currentSection.beatParameters.beats * currentSection.beatParameters.measures;
    }
    public String getKeyName() {
        return keyName;
    }
    public int getBPM() {
        return 60000 / (currentSection.beatParameters.subbeatLength * currentSection.beatParameters.subbeats);
    }
    public void setBPM(float bpm) {
        setSubbeatLength((int)((60000 / bpm) / currentSection.beatParameters.subbeats), null);
    }

    public void play() {
        player.play(currentSection);
    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }
    }

    public void finish() {
        player.finish();
        //player.cleanUp();
        //todo unref everything here
    }

    public void setPartMute(JamPart jamPart, boolean mute, String device) {
        jamPart.part.audioParameters.mute = mute;
    }

    public void setPartVolume(JamPart jamPart, float volume, String device) {
        jamPart.part.audioParameters.volume = volume;
    }

    public void setPartPan(JamPart jamPart, float pan, String device) {
        jamPart.part.audioParameters.pan = pan;
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

    public String getData() {
        return SectionToJSON.getData(currentSection);
    }

    public void removePart(JamPart jamPart) {

    }

    public void clearPart(JamPart jamPart) {
        jamPart.clear();

    }

    public void copyPart(JamPart jamPart) {

    }

    public void setPartSpeed(JamPart jamPart, float speed, String source) {
        jamPart.part.audioParameters.speed = speed;
    }

    public void setPartTrackMute(JamPart jamPart, SequencerTrack track, boolean mute) {
        track.audioParameters.mute = mute;
    }

    public void setPartTrackVolume(JamPart jamPart, SequencerTrack track, float volume) {
        track.audioParameters.volume = volume;
    }

    public void setPartTrackPan(JamPart jamPart, SequencerTrack track, float pan) {
        track.audioParameters.pan = pan;
    }

    public void addOnSubbeatListener(OnSubbeatListener listener) {
        player.onSubbeatListeners.add(listener);
    }

    public void removeOnSubbeatListener(OnSubbeatListener listener) {
        player.onSubbeatListeners.remove(listener);
    }

    public void addOnJamChangeListener(OnJamChangeListener listener) {
        onJamChangeListeners.add(listener);
    }

    public void removeOnJamChangeListener(OnJamChangeListener listener) {
        onJamChangeListeners.remove(listener);
    }

    public boolean isReady() {
        return currentSection != null;
    }

    public void startPartLiveNotes(JamPart jamPart, Note  note, int autoBeat) {
        jamPart.part.liveNotes =  new Note[] {note};
        if (!player.isPlaying() || autoBeat == 0) {
            player.playPartLiveNote(jamPart.part, note);
        }
        jamPart.part.autoBeat = autoBeat;
    }

    public void updatePartLiveNotes(JamPart jamPart, Note[] notes, int autoBeat) {
        jamPart.part.liveNotes =  notes;
        if (!player.isPlaying() || autoBeat == 0) {
            player.playPartLiveNotes(jamPart.part, notes);
        }
        jamPart.part.autoBeat = autoBeat;
    }

    public void removeFromPartLiveNotes(JamPart jamPart, Note note, Note[] notes) {
        jamPart.part.liveNotes =  notes;
        player.stopPartLiveNote(jamPart.part, note);
    }

    public void endPartLiveNotes(JamPart jamPart) {
        if (jamPart.part.liveNotes.length > 0) {
            player.stopPartLiveNote(jamPart.part, jamPart.part.liveNotes[0]);
        }
        jamPart.part.liveNotes = null;
    }

    //I'm not too sure these should be here, but where?
    private void prepareSoundSetForPart(Part part) {
        //todo maybe an isloaded instead of isvalid here
        if (!part.soundSet.isValid()) {
            if (onGetSoundSetListener == null) {
                return;
            }

            SoundSet soundSet = onGetSoundSetListener.onGetSoundSet(part.soundSet.getURL());
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

    private void updateKeyName() {
        keyName = KeyHelper.getKeyName(currentSection.keyParameters.rootNote, currentSection.keyParameters.scale);
    }

    public int getChordInProgression() {
        return player.getChordInProgression();
    }

    public void newPart(SoundSet soundSet) {
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

        prepareSoundSetForPart(part);
        loadSounds();
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
            jamPart.getTracks().add(new SequencerTrack(sound.getName()));
            jamPart.part.pattern[i] = jamPart.getTracks().get(i).getData();
            i++;
        }
    }
}
