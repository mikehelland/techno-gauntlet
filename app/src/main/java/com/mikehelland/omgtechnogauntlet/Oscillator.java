package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.dsp.Dac;
import com.mikehelland.omgtechnogauntlet.dsp.Delay;
import com.mikehelland.omgtechnogauntlet.dsp.ExpEnv;
import com.mikehelland.omgtechnogauntlet.dsp.Flange;
import com.mikehelland.omgtechnogauntlet.dsp.UGen;
import com.mikehelland.omgtechnogauntlet.dsp.WtOsc;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
class Oscillator { //DialpadChannel extends Channel {

    private boolean envActive = false;
    private final WtOsc ugOscA1 = new WtOsc();
    final ExpEnv ugEnvA = new ExpEnv();
    final Dac ugDac = new Dac();
    private final Delay ugDelay = new Delay(UGen.SAMPLE_RATE / 2);
    private final Flange ugFlange = new Flange(UGen.SAMPLE_RATE / 64, 0.25f);
    private boolean delayed = false;
    private boolean flanged = false;

    Oscillator(OscillatorSettings settings) {

        boolean delay = settings.delay;
        boolean flange = settings.flange;
        boolean softEnvelope = settings.softe;


        if (settings.waveType == OscillatorSettings.WaveType.SAW)
            ugOscA1.fillWithSaw();
        else if (settings.waveType == OscillatorSettings.WaveType.SQUARE) {
            ugOscA1.fillWithSqrDuty(0.6f);
        } else {
            ugOscA1.fillWithHardSin(7.0f);
        }

        if (delay) {
            delayed = true;
            ugEnvA.chuck(ugDelay);

            if (flange) {
                flanged = true;
                ugDelay.chuck(ugFlange).chuck(ugDac);
            } else {
                ugDelay.chuck(ugDac);
            }
        } else {
            if (flange) {
                flanged = true;
                ugEnvA.chuck(ugFlange);
                ugFlange.chuck(ugDac);
            } else {
                ugEnvA.chuck(ugDac);
            }
        }

        ugOscA1.chuck(ugEnvA);
        if (!softEnvelope) {
            ugEnvA.setFactor(ExpEnv.hardFactor);
        }

        //ugEnvA.setActive(true);
        //envActive = true;

        //set the gain?
        //ugEnvA.setGain(2*y*y);
        ugEnvA.setGain(0.25f);

        ugDac.open();

    }


    int playNote(Note note, boolean multiTouch) {
        //if (!enabled)
        //    return -1;

        if (note.isRest()) {
            mute();
        }
        else {

            float frequency = buildFrequencyFromMapped(note.getInstrumentNote());

            unmute();

            ugOscA1.setFreq(frequency);
        }
        return 1;
    }



    void mute() {
        envActive = false;
        ugEnvA.setActive(false);
    }
    void unmute() {
        envActive = true;
        ugEnvA.setActive(true);
    }

    private static float buildFrequencyFromMapped(float mapped) {
        return (float)Math.pow(2, (mapped-69.0f)/12.0f) * 440.0f;
    }

    void finish() {
        ugDac.close();
    }
}
