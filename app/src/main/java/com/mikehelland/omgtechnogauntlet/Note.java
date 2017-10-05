package com.mikehelland.omgtechnogauntlet;

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

    public void setBeats(double beats) {

        mBeats = beats;

    }

    public void setRest(boolean value) {
        isrest = value;

    }

    public void setBasicNote(int number) {
        basicNote = number;
    }

    public int getBasicNote() {
        return basicNote;
    }

    public void setInstrumentNote(int number) {
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

    public void setScaledNote(int newScaledNote) {
        scaledNote = newScaledNote;
    }

    public int getScaledNote() {
        return scaledNote;
    }

    public Note clone() {
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
