package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/29/18.
 */

public abstract class OnJamChangeListener {
    public abstract void onSubbeatLengthChange(int length, String source);
    public abstract void onKeyChange(int key, String source);
    public abstract void onScaleChange(String scale, String source);
    public abstract void onChordProgressionChange(int[] chords);
    public abstract void onNewPart(JamPart part);
    public abstract void onPartEnabledChanged(JamPart part, boolean enabled, String source);
    public abstract void onPartVolumeChanged(JamPart part, float volume, String source);
    public abstract void onPartPanChanged(JamPart part, float pan, String source);

    public abstract void newState(String stateChange, Object... args);
}
