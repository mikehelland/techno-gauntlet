package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.jam.Note;

import java.util.ArrayList;
import java.util.Random;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
class DrumMonkey {



    private boolean[][] pattern;

    private int beats;
    private int subbeats;
    private int measures;

    private Random rand;

    private Channel mChannel;

    DrumMonkey(_OldJam jam, Channel channel) {
        mChannel = channel;

        rand = jam.getRand();

        beats = jam.getBeats();
        subbeats = jam.getSubbeats();
        measures = jam.getMeasures();

        int sounds = 8;
        if (channel.getSoundSet() != null
                && channel.getSoundSet().getSounds().size() > 8) {
            sounds = channel.getSoundSet().getSounds().size();
        }
        //256 is hard coded high limit of 8 measures with 8 beats and 4 subbeats
        pattern = new boolean[sounds][256]; //[measures * beats * subbeats];
    }

    static boolean[] default_hithat = new boolean[] {
            true, false, false, false,
            true, false, false, false,
            true, false, false, false,
            true, false, true, false,
            true, false, false, false,
            true, false, false, false,
            true, false, false, false,
            true, true, true, true,
    };


    public void makeDrumBeatsFromMelody(ArrayList<Note> bassline) {

        clearPattern();

        boolean[] kick = pattern[0];
        boolean[] clap = pattern[1];
        boolean[][] toms = new boolean[][] {pattern[5], pattern[6], pattern[7]};

        double usedBeats = 0.0d;

        boolean snareCutTime = rand.nextBoolean();

        int ib = 0;

        boolean usetom = false;
        boolean usekick;
        boolean useclap;

        for (Note note : bassline) {
            usekick = false;
            useclap = false;
            usetom = false;

            if ((snareCutTime && usedBeats % 2 == 1.0d) ||
                    (!snareCutTime && usedBeats % 4 == 2.0d)) {
                useclap = !note.isRest() || rand.nextBoolean();
            }
            else if ((snareCutTime && usedBeats % 2 == 0.0d) ||
                    (!snareCutTime && usedBeats % 4 == 0.0d)) {
                usekick = !note.isRest() || rand.nextBoolean();
            }
            else {
                if (rand.nextBoolean())
                    usekick = !note.isRest() && rand.nextBoolean();
                else
                    usetom = !note.isRest() && rand.nextBoolean();
            }

            kick[ib] = usekick;
            clap[ib] = useclap;
            if (usetom) {
                toms[rand.nextInt(3)][ib] = true;
            }

            for (ib = ib + 1; ib < (usedBeats  + note.getBeats()) * subbeats; ib++) {
                kick[ib] = rand.nextBoolean() && ((snareCutTime && ib % (2 * subbeats) == 0) ||
                        (!snareCutTime && ib % (4 * subbeats) == 0));
                clap[ib] = (snareCutTime && ib % (2 * subbeats) == subbeats) ||
                        (!snareCutTime && ib % (4 * subbeats) == (2 * subbeats));
            }

            usedBeats += note.getBeats();
        }

        for (ib = ib + 1; ib < (beats - usedBeats) * subbeats; ib++) {
            kick[ib] = rand.nextBoolean() && ((snareCutTime && ib % (2 * subbeats) == 0) ||
                    (!snareCutTime && ib % (4 * subbeats) == 0));
            clap[ib] = rand.nextBoolean() && ((snareCutTime && ib % (2 * subbeats) == subbeats) ||
                    (!snareCutTime && ib % (4 * subbeats) == (2 * subbeats)));
        }

        makeHiHatBeats(false);
    }


    private void makeHiHatBeats(boolean defaultPattern) {

        boolean[][] hihats = new boolean[][] {pattern[2], pattern[3]};

        int openhh = rand.nextInt(3) > 0 ? 0 : 1;
        int opensubs = rand.nextInt(3) > 0 ? 0 : 1;
        int tmpopensubs;
        int subbeatOffset;

        for (int measure = 0; measure < measures; measure++) {
            subbeatOffset = measure * beats* subbeats;
            for (int i = 0; i < beats* subbeats; i++) {
                hihats[0][i + subbeatOffset] = defaultPattern && default_hithat[i % default_hithat.length];
                hihats[1][i + subbeatOffset] = false;
            }
        }

        if (defaultPattern)
            return;


        int downbeat;
        for (int measure = 0; measure < measures; measure++) {
            subbeatOffset = measure * beats* subbeats;
            for (int i = 0; i < beats; i++) {
                downbeat = i * subbeats;

                hihats[openhh][downbeat + subbeatOffset] = rand.nextInt(20) > 0;

                if (rand.nextBoolean()) {
                    tmpopensubs = (opensubs == 1 && rand.nextBoolean()) ? 1 : 0;
                    hihats[tmpopensubs][downbeat + 2 + subbeatOffset] = true;

                    if (rand.nextBoolean()) {
                        hihats[opensubs][downbeat + 1 + subbeatOffset] = true;
                        hihats[opensubs][downbeat + 3 + subbeatOffset] = true;
                    }
                }
            }
        }
    }

    void makeKickBeats(boolean defaultPattern) {

        boolean[] kick = pattern[0];
        if (defaultPattern) {
            for (int i = 0; i < kick.length; i++) {
                kick[i] = i % (subbeats * 2) == 0;
            }
            return;
        }

        int pattern = rand.nextInt(10);

        for (int i = 0; i < kick.length; i++) {
            kick[i] = pattern == 0 ? (rand.nextBoolean() && rand.nextBoolean()) :
                      pattern <  5  ? i % subbeats == 0 :
                      pattern <  9 ? i % beats * measures == 0 :
                              (i == 0 || i == beats * measures || i == beats * subbeats);
        }
    }
    void makeClapBeats(boolean defaultPattern) {

        boolean[] clap = pattern[1];
        if (defaultPattern) {
            for (int i = 0; i < clap.length; i++) {
                clap[i] =  (i - subbeats) % (beats * subbeats / 2) == 0;
            }
            return;
        }

        int pattern = rand.nextInt(10);

//        clap = new boolean[beats * subbeats];

        boolean snareCutTime = rand.nextBoolean();
        for (int i = 0; i < clap.length; i++) {

            clap[i] = pattern != 0 && (
                (snareCutTime && i % (2 * subbeats) == subbeats) ||
                        (!snareCutTime && i % (beats * subbeats) == (2 * subbeats))

            );

        }

    }


    void makeDrumBeats() {

        clearPattern();

        makeKickBeats(false);
        makeClapBeats(false);
        makeHiHatBeats(false);

        makeTomBeats();

    }

    private void makeTomBeats() {

        //maybe none?
        if (rand.nextBoolean())
            return;

        if (rand.nextInt(5) > -1) {
            makeTomFill();
            return;
        }

        boolean[][] toms = new boolean[][] {pattern[5], pattern[6], pattern[7]};

        for (int ib = 0; ib < measures * beats * subbeats; ib++) {
            if (rand.nextBoolean()) {
                toms[rand.nextInt(4)][ib] = true;
            }
        }

    }

    private void makeTomFill() {

        boolean everyBar = rand.nextBoolean();
        boolean[][] toms = new boolean[][] {pattern[5], pattern[6], pattern[7]};

        int start = 8;
        if (!everyBar && rand.nextInt(5) == 0) {
            start = 0;
        }

        boolean sparse = rand.nextBoolean();
        boolean on;
        int tom;
        for (int i = start; i < beats * subbeats; i++) {

            on = (sparse && rand.nextBoolean()) ||
                    (!sparse && (rand.nextBoolean() || rand.nextBoolean()));
            tom = rand.nextInt(3);

            for (int measure = 0; measure < measures; measure++) {
                if (everyBar || measure + 1 == measures) {
                    toms[tom][i] = on;
                }
            }
        }

    }

    void makePercussionFill() {

        clearPattern();

        int fillLevel = rand.nextInt(4);

        if (fillLevel == 0)
            return;

        boolean[][] toms = new boolean[][] {pattern[0], pattern[1], pattern[2],
                pattern[3], pattern[4]};
        boolean on;
        int tom;
        for (int i = 0; i < beats * subbeats; i++) {

            on = (fillLevel == 1 && (rand.nextBoolean() && rand.nextBoolean())) ||
                    (fillLevel == 2 && rand.nextBoolean()) ||
                    (fillLevel == 3 && (rand.nextBoolean() || rand.nextBoolean()));
            tom = rand.nextInt(5);

            for (int measure = 0; measure < measures; measure++) {
                toms[tom][i + measure * beats * subbeats] = on;
            }
        }

    }


    private void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
    }


    boolean[][] getPattern() {
        return pattern;
    }
}

