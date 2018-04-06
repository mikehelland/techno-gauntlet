package com.mikehelland.omgtechnogauntlet.jam;

/**
 * User: m
 * Date: 11/15/13
 * Time: 2:06 PM
 */
public class Note {

    private double mBeats;

    private int basicNote;
    private int scaledNote;

    private int instrumentNoteNumber;

    private boolean isrest = false;

    int playingHandle = -1;
    int startedPlayingAtSubbeat = 0;
    long createdAtTime = System.currentTimeMillis();

    boolean finishNow = false;

    Note() {

    }

    public Note(boolean rest, int basicNote, int scaledNote, int instrumentNote, double beats) {
        isrest = rest;
        this.basicNote = basicNote;
        this.scaledNote = scaledNote;
        this.instrumentNoteNumber = instrumentNote;
        this.mBeats = beats;
    }

    void setBeats(double beats) {

        mBeats = beats;

    }

    void setRest(boolean value) {
        isrest = value;

    }

    void setBasicNote(int number) {
        basicNote = number;
    }

    int getBasicNote() {
        return basicNote;
    }

    void setInstrumentNote(int number) {
        instrumentNoteNumber = number;
    }

    public int getInstrumentNote() {
        return instrumentNoteNumber;
    }

    public boolean isRest() {
        return isrest;
    }

    public double getBeats() {
        return mBeats;
    }

    void setScaledNote(int newScaledNote) {
        scaledNote = newScaledNote;
    }

    int getScaledNote() {
        return scaledNote;
    }

    public Note cloneNote() {
        Note ret = new Note();
        ret.mBeats = mBeats;
        ret.isrest = isrest;
        ret.basicNote = basicNote;

        ret.scaledNote = scaledNote;
        ret.instrumentNoteNumber = instrumentNoteNumber;

        return ret;
    }


    public boolean isPlaying() {
        return playingHandle > -1;
    }
}
