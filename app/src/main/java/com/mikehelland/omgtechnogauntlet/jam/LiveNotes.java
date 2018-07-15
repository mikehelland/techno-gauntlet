package com.mikehelland.omgtechnogauntlet.jam;

class LiveNotes {
    Note[] notes;
    int autoBeat;
    Note liveNote;

    LiveNotes(int autoBeat, Note[] notes) {
        this.autoBeat = autoBeat;
        this.notes = notes;

        if (notes.length > 0) {
            liveNote = notes[0];
        }
    }
}
