package com.mikehelland.omgtechnogauntlet.jam;

/**
 * Created by m on 3/31/18.
 * just some helper functions for the key and scale
 */

public class KeyHelper {

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
