package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by m on 3/17/18.
 */

public class SequencerPattern {
    private CopyOnWriteArrayList<SequencerTrack> mTracks = new CopyOnWriteArrayList<>();

    boolean[][] updateTracks(ArrayList<SoundSet.Sound> sounds) {

        boolean[][] newPattern = new boolean[sounds.size()][];
        ArrayList<SequencerTrack> newTracks = new ArrayList<>();
        int i = 0;
        for (SoundSet.Sound sound : sounds) {
            SequencerTrack track = new SequencerTrack(sound.getName(), i);
            newTracks.add(track);

            if (i < mTracks.size()) {
                track.setData(mTracks.get(i).getData());
            }

            newPattern[i] = track.getData();
            i++;
        }

        mTracks.clear();
        mTracks.addAll(newTracks);

        return newPattern;
    }

    public SequencerTrack getTrack(int index) {
        if (index > -1 && index < mTracks.size()) {
            return mTracks.get(index);
        }
        return null;
    }

    CopyOnWriteArrayList<SequencerTrack> getTracks() {
        return mTracks;
    }
}
