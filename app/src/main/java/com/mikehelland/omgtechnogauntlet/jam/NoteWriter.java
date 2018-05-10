package com.mikehelland.omgtechnogauntlet.jam;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by m on 5/10/18.
 */

class NoteWriter {
    static Note addNote(Note newNote, int subbeat,
                        CopyOnWriteArrayList<Note> noteList, BeatParameters beatParameters) {

        newNote = newNote.cloneNote();
        PlaceNoteOutput placeNoteOutput = placeNote(newNote, subbeat, noteList, beatParameters);

        Note note;
        while (placeNoteOutput.newBeats > 0 && noteList.size() > placeNoteOutput.i + 1) {
            note = noteList.get(placeNoteOutput.i + 1);
            if (note.getBeats() > placeNoteOutput.newBeats) {
                note.setBeats(note.getBeats() - placeNoteOutput.newBeats);
                note.setRest(true);
                placeNoteOutput.newBeats = 0;
            }
            else {
                noteList.remove(placeNoteOutput.i + 1);
                placeNoteOutput.newBeats -= note.getBeats();
            }
        }

        return newNote;
    }

    static void extendNote(Note note,
                        CopyOnWriteArrayList<Note> noteList, BeatParameters beatParameters) {

        note.setBeats(note.getBeats() + 1.0f / beatParameters.subbeats);
        int i = noteList.indexOf(note);
        if (i > -1 && noteList.size() - 1 > i) {
            Note nextNote = noteList.get(i + 1);
            if (nextNote.getBeats() == 1.0f / beatParameters.subbeats) {
                noteList.remove(i + 1);
            }
            else {
                nextNote.setRest(true);
                nextNote.setBeats(nextNote.getBeats() - 1.0f / beatParameters.subbeats);
            }
        }
    }

    //return the number of extra beats that need to be trimmed
    private static PlaceNoteOutput placeNote(Note newNote, int subbeat,
                                    CopyOnWriteArrayList<Note> noteList, BeatParameters beatParameters) {

        double beatToWrite = subbeat / (double) beatParameters.subbeats;
        double beatsUsed = 0.0d;

        Note note;
        for (int i = 0; i < noteList.size(); i++) {
            note = noteList.get(i);

            if (beatToWrite < beatsUsed) {
                double choppedLength = beatsUsed - beatToWrite;
                note = noteList.get(i - 1);
                note.setBeats(note.getBeats() - choppedLength);
                noteList.add(i, newNote);
                if (newNote.getBeats() == choppedLength) {
                    return new PlaceNoteOutput(i, 0.0d);
                }
                else if (newNote.getBeats() > choppedLength) {
                    return new PlaceNoteOutput(i, newNote.getBeats() - choppedLength);
                }
                else if (newNote.getBeats() < choppedLength) {
                    Note restNote = new Note(true, -1, -1, -1,
                            choppedLength - newNote.getBeats());
                    noteList.add(i + 1, restNote);
                    return new PlaceNoteOutput(i, 0.0d);
                }
            }

            if (beatToWrite == beatsUsed) {
                noteList.add(i, newNote);
                if (note.getBeats() == newNote.getBeats()) {
                    noteList.remove(note);
                    return new PlaceNoteOutput(i, 0.0d);
                }
                else if (note.getBeats() > newNote.getBeats()) {
                    note.setRest(true);
                    note.setBeats(note.getBeats() - newNote.getBeats());
                    return new PlaceNoteOutput(i, 0.0d);
                }
                else if (note.getBeats() < newNote.getBeats()) {
                    return new PlaceNoteOutput(i, newNote.getBeats());
                }
            }

            beatsUsed += note.getBeats();
        }

        double beatsLeft = beatToWrite - beatsUsed;
        if (beatsLeft > 0) {
            Note restNote = new Note(true, 0, 0, 0, beatsLeft);
            noteList.add(restNote);
        }
        noteList.add(newNote);
        return new PlaceNoteOutput(noteList.size() -1, 0.0d);
    }

    private static class PlaceNoteOutput {
        int i = 0;
        double newBeats = 0.0d;
        PlaceNoteOutput(int i, double newBeats) {
            this.i = i;
            this.newBeats = newBeats;
        }
    }
}
