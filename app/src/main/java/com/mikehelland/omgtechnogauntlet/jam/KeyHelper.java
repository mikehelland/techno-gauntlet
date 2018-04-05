package com.mikehelland.omgtechnogauntlet.jam;

import java.util.Arrays;

/**
 * Created by m on 3/31/18.
 * just some helper functions for the key and scale
 */

public class KeyHelper {

    public final static String[] KEY_CAPTIONS = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
    public final static String[] SCALE_CAPTIONS = {"Major", "Minor", "Pentatonic", "Blues",
            "Harmonic Minor", "Chromatic"};
    public final static int[][] SCALES = { {0,2,4,5,7,9,11}, {0,2,3,5,7,8,10}, {0,2,4,7,9}, {0,3,5,6,7,10},
            {0,2,3,5,7,8,11}, {0,1,2,3,4,5,6,7,8,9,10,11}};
    //public final static String[] SCALES = {"0,2,4,5,7,9,11", "0,2,3,5,7,8,10", "0,2,4,7,9", "0,3,5,6,7,10",
    //        "0,2,3,5,7,8,11", "0,1,2,3,4,5,6,7,8,9,10,11"};

    static String getKeyName(int rootNote, int[] scale) {
        if (rootNote < 0 || rootNote >= KEY_CAPTIONS.length) {
            return "(?)";
        }

        String scaleName = "";
        for (int i = 0; i < SCALES.length; i++) {
            if (Arrays.equals(scale, SCALES[i])) {
                scaleName = SCALE_CAPTIONS[i];
                break;
            }
        }

        return KEY_CAPTIONS[rootNote] + " " + scaleName;
    }

    static void applyScaleToPart(Section section, Part part, int chord) {
        for (Note note : part.notes) {

            if (note.isRest()) {
                continue;
            }

            note.setScaledNote(scaleNote(section, note.getBasicNote(), chord));
            note.setInstrumentNote(getInstrumentNote(part, note.getScaledNote()));
        }
    }

    private static int scaleNote(Section section, int oldNoteNumber, int chord) {
        int newNoteNumber;
        int octaves;

        octaves = 0;

        newNoteNumber = oldNoteNumber + chord;

        while (newNoteNumber >= section.keyParameters.scale.length) {
            octaves++;
            newNoteNumber = newNoteNumber - section.keyParameters.scale.length;
        }

        while (newNoteNumber < 0) {
            octaves--;
            newNoteNumber = newNoteNumber + section.keyParameters.scale.length;
        }

        newNoteNumber = section.keyParameters.scale[newNoteNumber];

        return section.keyParameters.rootNote + newNoteNumber + octaves * 12;
    }

    private static int getInstrumentNote(Part part, int scaledNote) {
        int noteToPlay = scaledNote + part.octave * 12;

        while (noteToPlay < part.soundSet.getLowNote()) {
            noteToPlay += 12;
        }
        while (noteToPlay > part.soundSet.getHighNote()) {
            noteToPlay -= 12;
        }

        noteToPlay -= part.soundSet.getLowNote();

        return noteToPlay;
    }
}
