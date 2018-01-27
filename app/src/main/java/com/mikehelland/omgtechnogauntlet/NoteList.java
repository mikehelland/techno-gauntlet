package com.mikehelland.omgtechnogauntlet;

import java.util.ArrayList;

public class NoteList extends ArrayList<Note> {

    private int beats = 8;


    public void overwrite(Note note, double beat) {

        Note existingNote;
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

            // if we past it, get the last
            if (beatsUsed > beat) {
                Note lastNote = get(i - 1);
                lastNote.setBeats(lastNote.getBeats() - (beatsUsed - beat));
                beatsUsed = beat;
            }

            // this is right where the note is supposed to start
            if (beatsUsed == beat) {

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
