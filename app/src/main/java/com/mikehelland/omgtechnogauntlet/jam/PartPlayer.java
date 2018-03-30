package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;

/**
 * Created by m on 3/30/18.
 */

public class PartPlayer {
    static void getSoundsToPlayForPartAtSubbeat(ArrayList<PlaySoundCommand> commands, Part part, int subbeat) {
        if (part.useSequencer()) {
            getDrumbeatSounds(commands, part, subbeat);
        } else {

        }
    }

    private static void getDrumbeatSounds(ArrayList<PlaySoundCommand> commands, Part part, int subbeat) {
        if (!part.audioParameters.mute) {
            int i = 0;
            for (SequencerTrack track : part.sequencerPattern.getTracks()) {
                if (track.getData()[subbeat] && !track.isMuted()) {
                    commands.add(new PlaySoundCommand(i < part.poolIds.length ? part.poolIds[i] : -1,
                            -1, -1,
                            track.audioParameters.volume * part.audioParameters.volume,
                            track.audioParameters.pan + part.audioParameters.pan,
                            track.audioParameters.speed));
                }
                i++;
            }

            /*for (int i = 0; i < part.getPattern().length; i++) {
                try {
                    if (part.getPattern()[i][subbeat] && !mPatternInfo.getTrack(i).isMuted()) {
                        if (i < ids.length) {
                            //playingId = mPool.play(ids[i], leftVolume, rightVolume, 10, 0, mSampleSpeed);
                            float volume = mPatternInfo.getTrack(i).getVolume() * this.volume;
                            float pan = mPatternInfo.getTrack(i).getPan() + this.pan;
                            float[] stereoVolume = calculateStereoVolume(volume, pan);
                            playingId = mPool.play(ids[i], stereoVolume[0], stereoVolume[1],
                                    10, 0, mSampleSpeed);
                        }
                    }
                }
                catch (Exception excp) {
                    excp.printStackTrace();
                }
            }*/
        }

    }
}
