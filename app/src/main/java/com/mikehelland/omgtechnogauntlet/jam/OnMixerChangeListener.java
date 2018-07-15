package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/29/18.
 */

public abstract class OnMixerChangeListener {

    public abstract void onPartMuteChanged(JamPart part, boolean enabled, String source);
    public abstract void onPartVolumeChanged(JamPart part, float volume, String source);
    public abstract void onPartPanChanged(JamPart part, float pan, String source);
    public abstract void onPartWarpChanged(JamPart part, float speed, String source);

    public abstract void onPartTrackMuteChanged(JamPart part, int track, boolean enabled, String source);
    public abstract void onPartTrackVolumeChanged(JamPart part, int track, float volume, String source);
    public abstract void onPartTrackPanChanged(JamPart part, int track, float pan, String source);
    public abstract void onPartTrackWarpChanged(JamPart part, int track, float speed, String source);

}
