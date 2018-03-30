package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;
import java.util.List;

public class Jam {

    private Section section;
    private Player player = new Player();
    private SoundManager soundManager;

    private ArrayList<OnSubbeatListener> onSubbeatListeners = new ArrayList<>();
    private ArrayList<OnJamChangeListener> onJamChangeListeners = new ArrayList<>();
    private OnGetSoundSetListener onGetSoundSetListener;

    private Part currentPart;

    public Jam(SoundManager soundManager, OnGetSoundSetListener onGetSoundSetListener) {
        this.soundManager = soundManager;
        this.onGetSoundSetListener = onGetSoundSetListener;
    }

    public void loadFromJSON(String json) {
        try {
            section = SectionFromJSON.fromJSON(json);

            for (Part part : section.parts) {
                loadSoundSetForPart(part);
            }

            soundManager.loadSounds();

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
    }

    public int getKey() {
        return section.keyParameters.rootNote;
    }

    public void setKey(int key, String sourceDevice) {
        section.keyParameters.rootNote = key;
    }

    public int[] getProgression() {
        return section.progression;
    }

    public void setProgression(int[] progression) {
        section.progression = progression;
    }

    public List<Part> getParts() {
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
        return "Figure keyname out";
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
        player.stop();
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

    }

    public void copyPart(Part part) {

    }

    public void setPartSpeed(Part part, float speed) {
        part.audioParameters.speed = speed;
    }

    public void setPartTrackMute(Part part, boolean mute) {
        part.audioParameters.mute = mute;
    }

    public void setPartTrackVolume(Part part, float volume) {
        part.audioParameters.volume = volume;
    }

    public void setPartTrackPan(Part part, float pan) {
        part.audioParameters.pan = pan;
    }

    public void addOnSubbeatListener(OnSubbeatListener listener) {
        onSubbeatListeners.add(listener);
    }

    public void removeOnSubbeatListener(OnSubbeatListener listener) {
        onSubbeatListeners.remove(listener);
    }

    public void addOnJamChangeListener(OnJamChangeListener listener) {
        onJamChangeListeners.add(listener);
    }

    public void removeOnJamChangeListener(OnJamChangeListener listener) {
        onJamChangeListeners.remove(listener);
    }

    public void setCurrentPart(Part currentPart) {
        this.currentPart = currentPart;
    }
    public Part getCurrentPart() {
        return currentPart;
    }


    public boolean isReady() {
        return section != null;
    }

    private void loadSoundSetForPart(Part part) {
        if (onGetSoundSetListener == null) {
            return;
        }

        SoundSet soundSet = onGetSoundSetListener.onGetSoundSet(part.soundSet.getURL());
        if (soundSet == null) {
            return;
        }

        part.soundSet = soundSet;

        for (SoundSet.Sound sound : soundSet.getSounds()) {
            soundManager.addSoundToLoad(sound);
        }
    }
}
