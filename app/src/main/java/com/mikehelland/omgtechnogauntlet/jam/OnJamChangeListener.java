package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/29/18.
 */

public abstract class OnJamChangeListener {
    public abstract void onSubbeatLengthChange(int length, String source);
    public abstract void onKeyChange(int key, String source);
    public abstract void onScaleChange(int[] scale, String source);
    public abstract void onChordProgressionChange(int[] chords);
    public abstract void onNewPart(JamPart part);
    public abstract void onPartEnabledChanged(JamPart part, boolean enabled, String source);
    public abstract void onPartVolumeChanged(JamPart part, float volume, String source);
    public abstract void onPartPanChanged(JamPart part, float pan, String source);

    public abstract void onPlay(String source);
    public abstract void onStop(String source);
    public abstract void onNewLoop(String source);

    public abstract void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source);
    public abstract void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source);
    public abstract void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source);
    public abstract void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source);
    public abstract void onPartEndLiveNotes(JamPart jamPart, String source);

}
