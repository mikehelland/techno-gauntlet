package com.mikehelland.omgtechnogauntlet.jam;

import java.util.List;

public class Jam {

    private Section section;
    private Player player;


    public Jam() {
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

    public void setSubbeatLength(int subbeatLength) {
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

    public void setScale(int[] scale) {
        section.keyParameters.scale = scale;
    }

    public int getKey() {
        return section.keyParameters.rootNote;
    }

    public void setKey(int key) {
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


    int getCurrentSubbeat() {
        return player.getCurrentSubbeat();
    }
    boolean isPlaying() {
        return player.isPlaying();
    }
    public boolean isPaused() {
        return player.isPaused();
    }


    // a couple helper functions
    int getTotalBeats() {
        return section.beatParameters.beats * section.beatParameters.measures;
    }
    String getKeyName() {
        return "Figure keyname out";
    }
    int getBPM() {
        return 60000 / (section.beatParameters.subbeatLength * section.beatParameters.subbeats);
    }
    void setBPM(float bpm) {
        setSubbeatLength((int)((60000 / bpm) / section.beatParameters.subbeats));
    }

    abstract static class PlayStatusChangeListener {
        abstract void onPlay();
        abstract void onStop();
    }


}
