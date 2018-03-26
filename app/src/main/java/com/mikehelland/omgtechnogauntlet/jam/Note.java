package com.mikehelland.omgtechnogauntlet.jam;

/**
 * User: m
 * Date: 11/15/13
 * Time: 2:06 PM
 */
public class Note {

    private double mBeats;
//    private int noteNumber;

    private int basicNote;
    private int scaledNote;

    private int instrumentNoteNumber;

    private boolean isrest = false;

    private boolean isplaying = false;

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

    int getInstrumentNote() {
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
        return isplaying;
    }
    public void isPlaying(boolean value) {
        isplaying = value;
    }

}
