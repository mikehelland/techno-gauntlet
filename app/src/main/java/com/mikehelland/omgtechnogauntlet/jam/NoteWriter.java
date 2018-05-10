package com.mikehelland.omgtechnogauntlet.jam;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by m on 5/10/18.
 */

class NoteWriter {
    static void addNote(Note newNote, int subbeat,
                        CopyOnWriteArrayList<Note> noteList, BeatParameters beatParameters) {

        double beatToWrite = subbeat / (double)beatParameters.subbeats;
        double beatsUsed = 0.0d;
        Note lastNote = null;
        Note note;
        Note nextNote = null;
        boolean appended = false;

        for (int i = 0; i < noteList.size(); i++) {
            note = noteList.get(i);

            //we past it, go back and chop the last note down to fit
            if (beatToWrite < beatsUsed) {
                lastNote.setBeats(lastNote.getBeats() - (beatsUsed - beatToWrite));
                noteList.add(i, newNote);
                if (beatsUsed - beatToWrite > 1.0d / beatParameters.subbeats) {
                    Note restNote = new Note(true, -1, -1, -1,
                            (beatsUsed - beatToWrite - (1.0d / beatParameters.subbeats)));
                    noteList.add(i + 1, restNote);
                }
                return;
            }

            //this is where to write the note
            if (beatToWrite == beatsUsed) {
                noteList.add(i, newNote);

                if (noteList.size() - 1 > i) {
                    nextNote = noteList.get(i + 1);
                    if (nextNote.getBeats() == 1.0f / beatParameters.subbeats) {
                        noteList.remove(i);
                    }
                    else {
                        nextNote.setRest(true);
                        nextNote.setBeats(nextNote.getBeats() - 1.0f / beatParameters.subbeats);
                    }
                }
                return;
            }

            lastNote = note;
            beatsUsed += lastNote.getBeats();
        }

        //nothing got written, add some beats and write
        double beatsLeft = beatToWrite - beatsUsed;
        Note restNote = new Note(true, 0, 0, 0, beatsLeft);
        noteList.add(restNote);
        noteList.add(newNote);
    }
}
