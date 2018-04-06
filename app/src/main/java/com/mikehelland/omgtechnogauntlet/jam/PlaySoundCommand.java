package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

/**
 * Created by m on 3/24/18.
 */

class PlaySoundCommand {
    int poolId = -1;
    int instrumentNote = -1;
    float duration = 1;
    float[] stereoVolume = new float[] {0.75f, 0.75f};
    float speed = 1f;
    Oscillator osc = null;

    Note note = null;

    PlaySoundCommand(Part part, Note note) {
        instrumentNote = note.getInstrumentNote();
        duration = (float)note.getBeats();
        calculateStereoVolume(part.audioParameters.volume, part.audioParameters.pan);
        speed = part.audioParameters.speed;
        if (part.soundSet.isOscillator()) {
            osc = part.soundSet.getOscillator();
        }
        else {
            if (part.poolIds != null && part.poolIds.length > note.getInstrumentNote()) {
                poolId = part.poolIds[note.getInstrumentNote()];
            }
            else {
                Log.e("MGH PlaySoundCommand()", "couldn't load poolId " + note.getInstrumentNote() +
                        "for part " + part.getName());
            }
        }


        this.note = note;

    }
    PlaySoundCommand(int poolId, int instrumentNote, float duration, float volume, float pan, float speed) {
        this.poolId = poolId;
        this.instrumentNote = instrumentNote;
        this.duration = duration;
        this.speed = speed;

        //todo maybe not most efficient to do this every single subbeat?
        calculateStereoVolume(volume, pan);
    }

    private void calculateStereoVolume(float volume, float pan) {
        float cross = (pan + 1) / 2;
        final double halfPi = Math.PI / 2;
        stereoVolume[0] = volume * (float)Math.sin((1 - cross) * halfPi);
        stereoVolume[1] = volume * (float)Math.sin(cross * halfPi);
    }

}
