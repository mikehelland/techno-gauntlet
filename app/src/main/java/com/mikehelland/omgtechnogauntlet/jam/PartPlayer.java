package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;

/**
 * Created by m on 3/30/18.
 * used by Player to do the Part stuff
 */

class PartPlayer {

    Section section;
    Part part;
    JamPart jamPart;

    Note nextNote;
    float nextBeat = 0f;
    int nextNoteIndex = 0;

    int nextLiveNoteI = 0;
    Note lastLiveNote = null;

    PartPlayer(Section section, JamPart jamPart) {
        this.section = section;
        this.jamPart = jamPart;
        this.part = jamPart.part;
    }

    static PlaySoundCommand getCommandForNote(Part part, Note note) {
        return new PlaySoundCommand(part, note);
    }

    static PlaySoundCommand[] getCommandsForNotes(Part part, Note[] notes) {
        PlaySoundCommand[] commands = new PlaySoundCommand[notes.length];
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].isPlaying()) {
                commands[i] = null;
            }
            else {
                commands[i] = getCommandForNote(part, notes[i]);
            }
        }
        return commands;
    }

    void getSoundsToPlayForPartAtSubbeat(ArrayList<PlaySoundCommand> commands,
                                                int subbeat, int chord) {
        if (jamPart.useSequencer()) {
            getDrumbeatSounds(commands, subbeat);
        } else {
            if (part.liveNotes == null || part.liveNotes.length == 0) {
                getNoteSounds(commands, subbeat, chord);
            }
            else if (part.autoBeat > 0 && (subbeat % part.autoBeat) == 0) {
                if (nextLiveNoteI >= part.liveNotes.length) {
                    nextLiveNoteI = 0;
                }
                if (lastLiveNote != null) {
                    //lastLiveNote.playingHandle
                }
                part.liveNotes[nextLiveNoteI].setBeats(part.autoBeat / (float)section.beatParameters.subbeats);
                commands.add(new PlaySoundCommand(part, part.liveNotes[nextLiveNoteI]));
                lastLiveNote = part.liveNotes[nextLiveNoteI];
                nextLiveNoteI++;
            }
        }
    }

    private void getDrumbeatSounds(ArrayList<PlaySoundCommand> commands, int subbeat) {
        if (!part.audioParameters.mute) {
            int i = 0;
            for (SequencerTrack track : part.sequencerPattern.getTracks()) {
                if (track.getData()[subbeat] && !track.isMuted()) {
                    commands.add(new PlaySoundCommand(i < part.poolIds.length ? part.poolIds[i] : -1,
                            -1, -1,
                            track.audioParameters.volume * part.audioParameters.volume,
                            track.audioParameters.pan + part.audioParameters.pan,
                            part.audioParameters.speed * track.audioParameters.speed));
                }
                i++;
            }
        }
    }

    private void getNoteSounds(ArrayList<PlaySoundCommand> commands, int subbeat, int chord) {

        if (part.notes.size() == 0) {
            return;
        }

        if (subbeat == 0) {
            nextBeat = 0;
            nextNote = part.notes.get(0);
            nextNoteIndex = 0;

            if (part.soundSet.isChromatic()) {
                KeyHelper.applyScaleToPart(section, part, chord);
            }
        }

        if (nextNote == null) {
            return;
        }

        if (nextBeat == subbeat / (float)section.beatParameters.subbeats) {

            if (!jamPart.getMute() && !nextNote.isRest()) {
                commands.add(new PlaySoundCommand(part, nextNote));
            }

            nextBeat += nextNote.getBeats();
            nextNoteIndex++;
            nextNote = nextNoteIndex < part.notes.size() ?
                    part.notes.get(nextNoteIndex) : null;
        }
    }
}
