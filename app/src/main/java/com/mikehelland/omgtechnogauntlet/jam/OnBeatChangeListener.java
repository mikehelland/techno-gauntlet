package com.mikehelland.omgtechnogauntlet.jam;

public abstract class OnBeatChangeListener {
    public abstract void onSubbeatLengthChange(int length, String source);
    public abstract void onBeatsChange(int length, String source);
    public abstract void onMeasuresChange(int length, String source);
    public abstract void onShuffleChange(float length, String source);
}
