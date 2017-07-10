package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:56 PM
 */
public abstract class DynamicDrumChannel extends DynamicChannel {


//    protected String[] mCaptions;
    protected String[] presetNames;

    protected boolean[][] pattern;

    protected int beats;
    protected int subbeats;

    protected Random rand;

    protected String kitName = "";

    public DynamicDrumChannel(Context context, Jam jam, OMGSoundPool pool) {
        super(context, jam, pool);

        rand = jam.getRand();

        isAScale = false;
        highNote = 7;
        lowNote = 0;

        volume = 1.0f;


        beats = jam.getBeats();
        subbeats = jam.getSubbeats();
        pattern = new boolean[8][beats * subbeats];
    }

    public void playBeat(int subbeat) {
        if (enabled) {
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i][subbeat]) {

                    if (i < ids.length)
                        playingId = mPool.play(ids[i], volume, volume, 10, 0, 1);
                }
            }
        }

    }


    public static boolean[] default_kick = new boolean[] {
            true, false, false, false,
            false, false, false, false,
            true, false, false, false,
            false, false, false, false,
            true, false, false, false,
            false, false, false, false,
            true, false, false, false,
            false, false, false, false,
    };
    public static boolean[] default_clap = new boolean[] {
            false, false, false, false,
            true, false, false, false,
            false, false, false, false,
            true, false, false, false,
            false, false, false, false,
            true, false, false, false,
            false, false, false, false,
            false, false, false, false,
    };
    public static boolean[] default_hithat = new boolean[] {
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


    public void makeHiHatBeats(boolean defaultPattern) {

        boolean[][] hihats = new boolean[][] {pattern[2], pattern[3]};

        int openhh = rand.nextInt(3) > 0 ? 0 : 1;
        int opensubs = rand.nextInt(3) > 0 ? 0 : 1;
        int tmpopensubs;

        for (int i = 0; i < hihats[0].length; i++) {
            hihats[0][i] = defaultPattern && default_hithat[i];
            hihats[1][i] = false;
        }

        if (defaultPattern)
            return;


        int downbeat;
        for (int i = 0; i < 4; i++) {
            downbeat = i * 4;
            hihats[openhh][downbeat] = rand.nextInt(20) > 0;
            hihats[openhh][downbeat + 16] = rand.nextInt(20) > 0;

            if (rand.nextBoolean()) {
                tmpopensubs = (opensubs == 1 && rand.nextBoolean()) ? 1 : 0;
                hihats[tmpopensubs][downbeat + 2] = true;
                hihats[tmpopensubs][downbeat + 2 + 16] = true;

                if (rand.nextBoolean()) {
                    hihats[opensubs][downbeat + 1] = true;
                    hihats[opensubs][downbeat + 3] = true;
                    hihats[opensubs][downbeat + 1 + 16] = true;
                    hihats[opensubs][downbeat + 3 + 16] = true;
                }

            }
        }
    }

    public void makeKickBeats(boolean defaultPattern) {

        boolean[] kick = pattern[0];
        if (defaultPattern) {
            for (int i = 0; i < kick.length; i++) {
                kick[i] = default_kick[i];
            }
            return;
        }

        int pattern = rand.nextInt(10);

        for (int i = 0; i < kick.length; i++) {
            kick[i] = pattern == 0 ? (rand.nextBoolean() && rand.nextBoolean()) :
                      pattern <  5  ? i % subbeats == 0 :
                      pattern <  9 ? i % 8 == 0 :
                              (i == 0 || i == 8 || i == 16);
        }
    }
    public void makeClapBeats(boolean defaultPattern) {

        boolean[] clap = pattern[1];
        if (defaultPattern) {
            for (int i = 0; i < clap.length; i++) {
                clap[i] = default_clap[i];
            }
            return;
        }

        int pattern = rand.nextInt(10);

//        clap = new boolean[beats * subbeats];

        boolean snareCutTime = rand.nextBoolean();
        for (int i = 0; i < clap.length; i++) {

            clap[i] = pattern != 0 && (
                (snareCutTime && i % (2 * subbeats) == subbeats) ||
                        (!snareCutTime && i % (4 * subbeats) == (2 * subbeats))

            );

        }

    }

    public boolean[] getTrack(int track)  {
        return pattern[track];
    }

    public void setPattern(int track, int subbeat, boolean value) {
        pattern[track][subbeat] = value;
    }

    public void makeDrumBeats() {

        clearPattern();

        makeKickBeats(false);
        makeClapBeats(false);
        makeHiHatBeats(false);

        makeTomBeats();

    }

    public void makeTomBeats() {

        //maybe none?
        if (rand.nextBoolean())
            return;

        if (rand.nextInt(5) > -1) {
            makeTomFill();
            return;
        }

        boolean[][] toms = new boolean[][] {pattern[5], pattern[6], pattern[7]};

        for (int ib = 0; ib < 4; ib++) {

        }

    }

    public void makeTomFill() {

        boolean everyBar = rand.nextBoolean();
        boolean[][] toms = new boolean[][] {pattern[5], pattern[6], pattern[7]};

        int start = 8;
        if (!everyBar && rand.nextInt(5) == 0) {
            start = 0;
        }

        boolean sparse = rand.nextBoolean();
        boolean on;
        int tom;
        for (int i = start; i < 16; i++) {

            on = (sparse && rand.nextBoolean()) ||
                    (!sparse && (rand.nextBoolean() || rand.nextBoolean()));
            tom = rand.nextInt(3);

            if (everyBar) {
                toms[tom][i] = on;
            }

            toms[tom][i + 16] = on;
        }

    }


    public void getData(StringBuilder sb) {

        int totalBeats = beats * subbeats;
        sb.append("{\"type\" : \"DRUMBEAT\", \"kit\": \"");
        sb.append(kitName);

        sb.append("\", \"volume\": ");
        sb.append(volume);
        if (!enabled)
            sb.append(", \"mute\": true");

        sb.append(", \"tracks\": [");

        for (int p = 0; p < pattern.length; p++) {
            sb.append("{\"name\": \"");
            sb.append(captions[p]);
            sb.append("\", \"sound\": \"");
            sb.append(presetNames[p]);
            sb.append("\", \"data\": [");
            for (int i = 0; i < totalBeats; i++) {
                sb.append(pattern[p][i] ?1:0) ;
                if (i < totalBeats - 1)
                    sb.append(",");
            }
            sb.append("]}");

            if (p < pattern.length - 1)
                sb.append(",");

        }

        sb.append("]}");

    }

    public void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
    }

    public void setPattern(boolean[][] pattern) {
        Log.d("MGH", "set pattern " + pattern.length);
        this.pattern = pattern;
    }

    @Override
    public void clearNotes() {
        clearPattern();
    }
}

