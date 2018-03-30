package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/24/18.
 */

class PlaySoundCommand {
    int poolId = -1;
    int instrumentNote = -1;
    float duration = 1;
    float[] stereoVolume = new float[] {0.75f, 0.75f};
    float speed = 1f;

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
