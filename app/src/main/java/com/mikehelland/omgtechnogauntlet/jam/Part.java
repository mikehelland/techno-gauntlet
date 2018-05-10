package com.mikehelland.omgtechnogauntlet.jam;

import java.util.UUID;

class Part {

    String id;

    KeyParameters keyParameters;
    BeatParameters beatParameters;
    AudioParameters audioParameters;

    int octave = 3;

    SoundSet soundSet;
    Surface surface;

    NoteList notes = new NoteList();
    SequencerPattern sequencerPattern = new SequencerPattern();

    //short cut to all the sequencerPattern's track's data
    boolean[][] pattern;

    int[] poolIds;

    //calculated vaules from volume and pan
    //todo not used but might be optimal to precalculate and reuse?
    float leftVolume = 0.75f;
    float rightVolume = 0.75f;

    Note[] liveNotes = null;
    int autoBeat = 0;
    int[] liveNoteHandles;
    int arpeggiate = 0;
    int nextArpNote = 0;
    int arpNotesCount = 0;

    Part(Section section) {
        id = UUID.randomUUID().toString();

        keyParameters = section.keyParameters;
        beatParameters = section.beatParameters;

        audioParameters = new AudioParameters();

        soundSet = new SoundSet();
        soundSet.setName("DRUMBEAT");
        soundSet.setURL("");

        surface = new Surface();
    }

}
