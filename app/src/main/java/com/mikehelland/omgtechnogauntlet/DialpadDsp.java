package com.mikehelland.omgtechnogauntlet;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:52 PM
 */
public class DialpadDsp {


    /*public DialpadChannel makeBasslineChannel() {

        OscillatorSettings settings = new OscillatorSettings();
        settings.saw = false;
        settings.softe = false;

        DialpadChannel ret = new DialpadChannel(settings);
        thread.addDac(ret.ugDac);

        ret.ugEnvA.setGain(1.5f);

        return ret;

    }

    public DialpadChannel makeGlitzlineChannel() {

        OscillatorSettings settings = new OscillatorSettings();
        settings.softe = false;//true;
        settings.softt = true;
        settings.delay = false;//true;
        DialpadChannel ret = new DialpadChannel(settings);
        thread.addDac(ret.ugDac);

        return ret;

    }*/


    static int[] buildScale(String quantizerString) {
        if (quantizerString != null && quantizerString.length() > 0) {
            String[] parts = quantizerString.split(",");
            int[] scale = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                scale[i] = Integer.parseInt(parts[i]);
            }
            return scale;
        } else {
            return null;
        }
    }

    static float buildFrequency(final float[] scale, final int octaves, float input, float pBase) {
        input = Math.min(Math.max(input, 0.0f), 1.0f);
        //final float base = 24;
        //final float base = 48;
        final float base = pBase;

        float mapped;
        if (scale == null) {
            mapped = base + input * octaves * 12.0f;
        } else {
            int idx = (int) ((scale.length * octaves + 1) * input);
            mapped = base + scale[idx % scale.length] + 12 * (idx / scale.length);
        }
        return (float) Math.pow(2, (mapped - 69.0f) / 12.0f) * 440.0f;

    }


}
