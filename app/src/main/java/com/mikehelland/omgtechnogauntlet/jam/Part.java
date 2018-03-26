package com.mikehelland.omgtechnogauntlet.jam;

import java.util.UUID;

public class Part {

    String id;

    KeyParameters keyParameters;
    BeatParameters beatParameters;
    AudioParameters audioParameters;

    int octave = 3;

    //calculated vaules from volume and pan
    float leftVolume = 0.75f;
    float rightVolume = 0.75f;

    int arpeggiate = 0;
    Note[] arpNotes = new Note[10];
    int nextArpNote = 0;
    int arpNotesCount = 0;

    SoundSet soundSet;
    Surface surface;

    NoteList notes = new NoteList();
    SequencerPattern sequencerPattern = new SequencerPattern();

    boolean[][] pattern;


    //todo we have soundset, pattern and patternInfo all kind of doing the same thign
    //find the the best way to separate them but stay performant


    Part(Section section) {
        id = UUID.randomUUID().toString();

        keyParameters = section.keyParameters;
        beatParameters = section.beatParameters;

        soundSet = new SoundSet();
        soundSet.setName("DRUMBEAT");
        soundSet.setURL("");

        surface = new Surface();

        //todo maybe the pattern should be part of .... the squence pattern

        pattern = new boolean[8][256]; // use a high limit [mJam.getTotalSubbeats()];
    }

    private void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
    }



    void fitNotesToInstrument() {

        for (Note note : notes) {

            int noteToPlay = note.getScaledNote() + 12 * octave;
            while (noteToPlay < soundSet.getLowNote()) {
                noteToPlay += 12;
            }
            while (noteToPlay > soundSet.getHighNote()) {
                noteToPlay -= 12;
            }

            note.setInstrumentNote(noteToPlay - soundSet.getLowNote());
        }
    }

    int getInstrumentNoteNumber(int scaledNote) {
        int noteToPlay = scaledNote + octave * 12;

        while (noteToPlay < soundSet.getLowNote()) {
            noteToPlay += 12;
        }
        while (noteToPlay > soundSet.getHighNote()) {
            noteToPlay -= 12;
        }

        noteToPlay -= soundSet.getLowNote();

        return noteToPlay;
    }


    void clear() {
        notes.clear();
        clearPattern();
    }

    public String getName() {
        return soundSet.getName();
    }

    public String getId() {
        return id;
    }
}
