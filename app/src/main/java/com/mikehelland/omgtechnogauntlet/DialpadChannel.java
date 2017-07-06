package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

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
public class DialpadChannel extends Channel {

    private boolean envActive = false;
    private final WtOsc ugOscA1 = new WtOsc();
    final ExpEnv ugEnvA = new ExpEnv();
    public final Dac ugDac = new Dac();
    private final Delay ugDelay = new Delay(UGen.SAMPLE_RATE / 2);
    private final Flange ugFlange = new Flange(UGen.SAMPLE_RATE / 64, 0.25f);
    private boolean delayed = false;
    private boolean flanged = false;

    public DialpadChannel(Context context, Jam jam, OMGSoundPool pool,
                          String type, DialpadChannelSettings settings) {
        super(context, jam, pool, type, settings.settingsName);

        lowNote = 0;
        highNote = 108;

        octave = 5;

        boolean delay = settings.delay;
        boolean flange = settings.flange;
        boolean softTimbre = settings.softt;
        boolean softEnvelope = settings.softe;
        boolean saw = settings.saw;


        if (saw)
            ugOscA1.fillWithSaw();
        else if (softTimbre) {
            ugOscA1.fillWithHardSin(7.0f);
        } else {
            ugOscA1.fillWithSqrDuty(0.6f);
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

        pool.addDac(ugDac);

    }

    @Override
    public int playNote(Note note, boolean multiTouch) {
        //super.playNote(note);

        if (lastPlayedNote != null)
            lastPlayedNote.isPlaying(false);

        if (!enabled)
            return -1;

        if (note.isRest()) {
            mute();
        }
        else {
            mPool.makeSureDspIsRunning();;

            float frequency = buildFrequencyFromMapped(note.getInstrumentNote());

            unmute();

            ugOscA1.setFreq(frequency);

            note.isPlaying(true);
            lastPlayedNote = note;
        }
        return 1;
    }



    public void mute() {
        envActive = false;
        ugEnvA.setActive(false);
    }
    public void unmute() {
        envActive = true;
        ugEnvA.setActive(true);
    }

    public static float buildFrequencyFromMapped(float mapped) {
        return (float)Math.pow(2, (mapped-69.0f)/12.0f) * 440.0f;
    }

}
