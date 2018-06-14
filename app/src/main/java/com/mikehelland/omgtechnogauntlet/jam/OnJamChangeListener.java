package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/29/18.
 */

public abstract class OnJamChangeListener {

    public abstract void onChordProgressionChange(int[] chords, String source);
    public abstract void onNewPart(JamPart part);

    public abstract void onPlay(String source);
    public abstract void onStop(String source);
    public abstract void onNewLoop(String source);

    public abstract void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source);
    public abstract void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source);
    public abstract void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source);
    public abstract void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source);
    public abstract void onPartEndLiveNotes(JamPart jamPart, String source);

}
