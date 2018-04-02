package com.mikehelland.omgtechnogauntlet.jam;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Part {

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

    //the part shouldn't really have to know these? But probably makes it faster?
    int[] poolIds;

    //also not quite part of the data but used by the player to keep track of where we are
    //for performance
    Note nextNote;
    float nextBeat = 0f;
    int nextNoteIndex = 0;


    //calculated vaules from volume and pan
    //todo not used but might be optimal to precalculate and reuse?
    float leftVolume = 0.75f;
    float rightVolume = 0.75f;

    Note[] liveNotes = null;
    int[] liveNoteHandles;
    int arpeggiate = 0;
    int nextArpNote = 0;
    int arpNotesCount = 0;

    Part(Section section) {
        id = UUID.randomUUID().toString();

        keyParameters = section.keyParameters;
        beatParameters = section.beatParameters;

        soundSet = new SoundSet();
        soundSet.setName("DRUMBEAT");
        soundSet.setURL("");

        surface = new Surface();
    }

    private void clearPattern() {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                pattern[i][j] = false;
            }
        }
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

    public float getSpeed() {
        return audioParameters.speed;
    }

    public String getSurfaceURL() {
        return surface.getURL();
    }

    public boolean getMute() {
        return audioParameters.mute;
    }

    public float getVolume() {
        return audioParameters.volume;
    }

    public float getPan() {
        return audioParameters.pan;
    }

    public boolean useSequencer() {
        return Surface.PRESET_SEQUENCER.equals(surface.getURL());
    }

    public boolean isValid() {
        return soundSet.getURL().length() > 0 && soundSet.isValid();
    }

    public SequencerPattern getSequencerPattern() { return sequencerPattern;}

    public CopyOnWriteArrayList<SequencerTrack> getTracks() {
        return sequencerPattern.getTracks();
    }

    public SoundSet getSoundSet() {
        return soundSet;
    }

    public Surface getSurface() {
        return surface;
    }

    public NoteList getNotes() {
        return notes;
    }

    public int getOctave() {
        return octave;
    }

    public boolean[][] getPattern() {
        return pattern;
    }
}
