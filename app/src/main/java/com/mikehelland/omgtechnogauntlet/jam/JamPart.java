package com.mikehelland.omgtechnogauntlet.jam;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by m on 5/10/18.
 */

public class JamPart {

    //this for holding data about live input from the user
    LiveNotes liveNotes = null;

    Part part;
    PartPlayer partPlayer = null;

    JamPart (Part part) {
        this.part = part;
        this.partPlayer = new PartPlayer(this, part.beatParameters, part.keyParameters);
    }

    void stopPlayingSounds() {
        //todo get a list of playing handles and stop them
        //or maybe just store the list here and let the Player class stop them
    }



    public String getName() {
        return part.soundSet.getName();
    }

    public String getId() {
        return part.uuid;
    }

    public float getSpeed() {
        return part.audioParameters.speed;
    }

    public String getSurfaceURL() {
        return part.surface.getURL();
    }

    public boolean getMute() {
        return part.audioParameters.mute;
    }

    public float getVolume() {
        return part.audioParameters.volume;
    }

    public float getPan() {
        return part.audioParameters.pan;
    }

    public boolean useSequencer() {
        return Surface.PRESET_SEQUENCER.equals(part.surface.getURL());
    }

    public boolean isValid() {
        return part.soundSet.getURL().length() > 0 && part.soundSet.isValid();
    }

    public SequencerPattern getSequencerPattern() { return part.sequencerPattern;}

    public CopyOnWriteArrayList<SequencerTrack> getTracks() {
        return part.sequencerPattern.getTracks();
    }

    public SoundSet getSoundSet() {
        return part.soundSet;
    }

    public Surface getSurface() {
        return part.surface;
    }

    public NoteList getNotes() {
        return part.notes;
    }

    public int getOctave() {
        return part.octave;
    }

    public boolean[][] getPattern() {
        return part.pattern;
    }

    private void clearPattern() {
        if (part.pattern == null) {
            return;
        }
        for (int i = 0; i < part.pattern.length; i++) {
            for (int j = 0; j < part.pattern[i].length; j++) {
                part.pattern[i][j] = false;
            }
        }
    }

    void clear() {
        if (useSequencer()) {
            clearPattern();
        }
        else {
            part.notes.clear();
        }
    }

    public Note findLiveNote(int basicNoteNumber, int instrumentNoteNumber) {
        try {
            for (Note note : liveNotes.notes) {
                if (note.getBasicNote() == basicNoteNumber) {
                    return note;
                }
            }
        }
        catch (Exception ignore) {}

        Note note = new Note(false, basicNoteNumber, 0, instrumentNoteNumber, -1);
        return note;
    }
}
