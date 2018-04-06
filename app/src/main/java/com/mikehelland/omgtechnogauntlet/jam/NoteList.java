package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

//todo this is basically one method, shouldn't really be part of NoteList anyway, maybe a NoteListOverwrite class
public class NoteList extends CopyOnWriteArrayList<Note> {

    private int beats = 8;
    //todo this should come from the jam, which we don't have access to!

    void overwrite(Note note) {
        if (note == null) {
            return;
        }

        Note existingNote;

        final double beat = note.startedPlayingAtSubbeat / 4d; //todo hardcoded subbeats
        double beatsUsed = 0.0d;
        double beatsDisplaced = note.getBeats();

        int j;

        if (size() == 0) {
            Note quarterRest;
            for (int ib = 0; ib < beats; ib++) {
                quarterRest =  new Note();
                quarterRest.setBeats(1.0);
                quarterRest.setRest(true);
                add(quarterRest);
            }
        }

        // go through the note list find out where we go
        for (int i = 0; i < size(); i++) {
            existingNote = get(i);

            //probably a concurrency thing //todo CopyOnWriteArrayList?
            if (existingNote == null)
                return;

            // if we past it, get the last and cut it short
            if (beatsUsed > beat && i > 0) {
                Note lastNote = get(i - 1);
                double lastNoteBeats = lastNote.getBeats();

                lastNote.setBeats(lastNoteBeats - (beatsUsed - beat));
                beatsUsed = beat;

                //if we cut off more beats than we're replacing it by, add a rest to compensate
                if (lastNoteBeats - lastNote.getBeats() > beatsDisplaced) {
                    add(i, new Note(true,
                            -1, -1, -1,
                            lastNoteBeats - lastNote.getBeats() - beatsDisplaced));
                    beatsDisplaced = 0;
                }
                else {
                    beatsDisplaced -= lastNoteBeats - lastNote.getBeats();
                }

            }

            // this is right where the note is supposed to start
            if (beatsUsed >= beat) {

                // if this eight note is over writing another, chop it in two
                /*if (!existingNote.isRest() && note.getTotalBeats() == 0.5d && existingNote.getTotalBeats() == 0.5d) {
                    existingNote.setBeats(0.25d);
                    note.setBeats(0.25d);
                    add(i + 1, note);
                }
                else {*/
                    // add it and cut out what was in its place
                    add(i, note);
                    j = i + 1;
                    while (beatsDisplaced > 0 && j < size()) {
                        existingNote = get(j);

                        //probably a concurrency thing //todo CopyOnWriteArrayList?
                        if (existingNote == null)
                            return;

                        if (existingNote.getBeats() <= beatsDisplaced) {
                            remove(j);
                            Log.d("MGH overwrite bd", "nextNote.beats: " + existingNote.getBeats());
                        }
                        else {
                            existingNote.setBeats(existingNote.getBeats() - beatsDisplaced);
                        }
                        beatsDisplaced -= existingNote.getBeats();

                    }
                //}

                break;
            }

            // the new note starts after the last note
            if (i == size() - 1) {
                //Log.d("MGH overwrite 1", Double.toString(beat - beatsUsed));
                existingNote.setBeats(beat - beatsUsed);
                add(note);

                beatsUsed += existingNote.getBeats();
                beatsUsed += note.getBeats();

                //Log.d("MGH overwrite 2", Double.toString(beats - beatsUsed));
                if (beatsUsed < beats) {
                    Note endRest = new Note();
                    endRest.setRest(true);
                    endRest.setBeats(beats - beatsUsed);
                    //Log.d("MGH overwrite 3", Double.toString(beatsUsed));
                    add(endRest);
                }

                // we could be adding notes, so this needs to be here
                // or out of memory will ensue
                break;
            }
            else {
                // get ready for the next one
                beatsUsed += existingNote.getBeats();
            }

        }

    }

}
