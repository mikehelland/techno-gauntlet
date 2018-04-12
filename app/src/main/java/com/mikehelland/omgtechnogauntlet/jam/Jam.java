package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Jam {

    private Section section;
    private Player player;
    private SoundManager soundManager;

    private ArrayList<OnJamChangeListener> onJamChangeListeners = new ArrayList<>();
    private OnGetSoundSetListener onGetSoundSetListener;

    private String keyName = "";

    public Jam(SoundManager soundManager, OnGetSoundSetListener onGetSoundSetListener) {
        this.soundManager = soundManager;
        this.onGetSoundSetListener = onGetSoundSetListener;
        this.player = new Player(soundManager);
    }

    public void loadFromJSON(String json) {
        try {
            section = SectionFromJSON.fromJSON(json);
            updateKeyName();

            for (Part part : section.parts) {
                loadSoundSetForPart(part);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    soundManager.loadSounds();

                    for (Part part : section.parts) {
                        setPoolIdsForPart(part);
                    }
                }
            }).start();

        } catch (Exception e) {
            //todo warn something here
            e.printStackTrace();
        }
    }


    public String getTags() {
        return section.tags;
    }

    public void setTags(String tags) {
        section.tags = tags;
    }

    public int getSubbeatLength() {
        return section.beatParameters.subbeatLength;
    }

    public void setSubbeatLength(int subbeatLength, String sourceDevice) {
        section.beatParameters.subbeatLength = subbeatLength;
    }

    public int getSubbeats() {
        return section.beatParameters.subbeats;
    }

    public void setSubbeats(int subbeats) {
        section.beatParameters.subbeats = subbeats;
    }

    public int getBeats() {
        return section.beatParameters.beats;
    }

    public void setBeats(int beats) {
        section.beatParameters.beats = beats;
    }

    public int getMeasures() {
        return section.beatParameters.measures;
    }

    public void setMeasures(int measures) {
        section.beatParameters.measures = measures;
    }

    public float getShuffle() {
        return section.beatParameters.shuffle;
    }

    public void setShuffle(float shuffle) {
        section.beatParameters.shuffle = shuffle;
    }

    public int[] getScale() {
        return section.keyParameters.scale;
    }

    public void setScale(int[] scale, String sourceDevice) {
        section.keyParameters.scale = scale;
        updateKeyName();
    }

    public int getKey() {
        return section.keyParameters.rootNote;
    }

    public void setKey(int key, String sourceDevice) {
        section.keyParameters.rootNote = key;
        updateKeyName();
    }

    public int[] getProgression() {
        return section.progression;
    }

    public void setProgression(int[] progression) {
        section.progression = progression;
        if (progression.length == 1) {
            updateNotesWithChord(section.progression[0]);
        }
    }

    private void updateNotesWithChord(int chord) {
        for (Part part : section.parts) {
            if (part.soundSet.isChromatic()) {
                KeyHelper.applyScaleToPart(section, part, chord);
            }
        }
    }

    public CopyOnWriteArrayList<Part> getParts() {
        return section.parts;
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
        return section.beatParameters.beats * section.beatParameters.measures;
    }
    public String getKeyName() {
        return keyName;
    }
    public int getBPM() {
        return 60000 / (section.beatParameters.subbeatLength * section.beatParameters.subbeats);
    }
    public void setBPM(float bpm) {
        setSubbeatLength((int)((60000 / bpm) / section.beatParameters.subbeats), null);
    }

    public void play() {
        player.play(section);
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

    public void setPartMute(Part part, boolean mute, String device) {
        part.audioParameters.mute = mute;
    }

    public void setPartVolume(Part part, float volume, String device) {
        part.audioParameters.volume = volume;
    }

    public void setPartPan(Part part, float pan, String device) {
        part.audioParameters.pan = pan;
    }

    public boolean getPartMute(Part part) {
        return part.audioParameters.mute;
    }
    public float getPartVolume(Part part) {
        return part.audioParameters.volume;
    }
    public float getPartPan(Part part) {
        return part.audioParameters.pan;
    }

    public String getData() {
        return SectionToJSON.getData(section);
    }

    public void removePart(Part part) {

    }

    public void clearPart(Part part) {
        part.clear();

    }

    public void copyPart(Part part) {

    }

    public void setPartSpeed(Part part, float speed, String source) {
        part.audioParameters.speed = speed;
    }

    public void setPartTrackMute(Part part, SequencerTrack track, boolean mute) {
        track.audioParameters.mute = mute;
    }

    public void setPartTrackVolume(Part part, SequencerTrack track, float volume) {
        track.audioParameters.volume = volume;
    }

    public void setPartTrackPan(Part part, SequencerTrack track, float pan) {
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
        return section != null;
    }

    public void startPartLiveNotes(Part part, Note  note, int autoBeat) {
        part.liveNotes =  new Note[] {note};
        if (!player.isPlaying() || autoBeat == 0) {
            player.playPartLiveNote(part, note);
        }
        part.autoBeat = autoBeat;
    }

    public void updatePartLiveNotes(Part part, Note[] notes, int autoBeat) {
        part.liveNotes =  notes;
        if (!player.isPlaying() || autoBeat == 0) {
            player.playPartLiveNotes(part, notes);
        }
        part.autoBeat = autoBeat;
    }

    public void removeFromPartLiveNotes(Part part, Note note, Note[] notes) {
        part.liveNotes =  notes;
        player.stopPartLiveNote(part, note);
    }

    public void endPartLiveNotes(Part part) {
        if (part.liveNotes.length > 0) {
            player.stopPartLiveNote(part, part.liveNotes[0]);
        }
        part.liveNotes = null;
    }

    //I'm not too sure these should be here, but where?
    private void loadSoundSetForPart(Part part) {
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
        keyName = KeyHelper.getKeyName(section.keyParameters.rootNote, section.keyParameters.scale);
    }

    public int getChordInProgression() {
        return player.getChordInProgression();
    }

    public void newPart(SoundSet soundSet) {
        Part part = new Part(section);
        part.soundSet = soundSet;
        section.parts.add(part);

        String defaultSurface = soundSet.getDefaultSurface();
        if (defaultSurface != null) {
            part.surface = new Surface(defaultSurface);
        }
        if (part.useSequencer()) {
            setupSequencerPatternForPart(part);
        }

        loadSoundSetForPart(part);
        loadSounds();
    }

    private void loadSounds() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                soundManager.loadSounds();

                for (Part part : section.parts) {
                    setPoolIdsForPart(part);
                }
            }
        }).start();
    }

    public void setPartSurface(Part part, Surface surface) {
        if (surface != null) {
            part.surface = surface;
        }
    }

    public void setupSequencerPatternForPart(Part part) {
        part.pattern = new boolean[part.soundSet.getSounds().size()][];
        int i = 0;
        for (SoundSet.Sound sound : part.soundSet.getSounds()) {
            part.getTracks().add(new SequencerTrack(sound.getName()));
            part.pattern[i] = part.getTracks().get(i).getData();
            i++;
        }
    }
}
