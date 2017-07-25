package com.mikehelland.omgtechnogauntlet;

/**
 * User: m
 * Date: 11/15/13
 * Time: 6:07 PM
 */
public class DialpadChannelSettings {

    public boolean delay = true;
    public boolean flange = false;
    public boolean softe = false;

    public WaveType waveType = WaveType.SINE;

    public enum WaveType {SINE, SQUARE, SAW};

    public String settingsName;

    public DialpadChannelSettings(String settings) {

        settingsName = settings;

        delay = settings.contains("DELAY");
        flange = settings.contains("FLANGE");
        if (settings.contains("SINE")) {
            waveType = WaveType.SINE;
        }
        else if (settings.contains("SQUARE")) {
            waveType = WaveType.SQUARE;
        }
        else if (settings.contains("SAW")) {
            waveType = WaveType.SAW;
        }

        softe = settings.contains("SOFT");

    }
}
