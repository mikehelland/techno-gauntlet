package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;

/**
 * Created by m on 3/30/18.
 * used by Player to do the Part stuff
 */

class PartPlayer {

    BeatParameters beatParameters;
    KeyParameters keyParameters;
    Part part;
    JamPart jamPart;

    Note nextNote;
    float nextBeat = 0f;
    int nextNoteIndex = 0;

    int nextLiveNoteI = 0;
    Note lastLiveNote = null;

    PartPlayer(JamPart jamPart, BeatParameters beatParameters, KeyParameters keyParameters) {
        this.jamPart = jamPart;
        this.part = jamPart.part;
        this.beatParameters = beatParameters;
        this.keyParameters = keyParameters;
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
            LiveNotes liveNotes = jamPart.liveNotes;
            if (liveNotes == null || liveNotes.notes.length == 0) {
                getNoteSounds(commands, subbeat, chord);
            }
            else if (liveNotes.autoBeat > 0 && (subbeat % liveNotes.autoBeat) == 0) {
                if (nextLiveNoteI >= liveNotes.notes.length) {
                    nextLiveNoteI = 0;
                }
                if (lastLiveNote != null) {
                    //lastLiveNote.playingHandle
                }
                liveNotes.liveNote = liveNotes.notes[nextLiveNoteI].cloneNote();
                liveNotes.liveNote.setBeats(liveNotes.autoBeat / (float)beatParameters.subbeats);
                //jamPart.liveNote.setBeats(1.0f / section.beatParameters.subbeats);
                if (!jamPart.getMute()) {
                    liveNotes.liveNote = NoteWriter.addNote(liveNotes.liveNote, subbeat, jamPart.getNotes(), beatParameters);
                }
                commands.add(new PlaySoundCommand(part, liveNotes.liveNote));
                lastLiveNote = liveNotes.notes[nextLiveNoteI];
                nextLiveNoteI++;
            }
            else if (liveNotes.liveNote != null && liveNotes.autoBeat == 0) {
                if (!jamPart.getMute()) {
                    NoteWriter.extendNote(liveNotes.liveNote, jamPart.part.notes, beatParameters);
                }
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
                KeyHelper.applyScaleToPart(part, chord, keyParameters);
            }
        }

        if (nextNoteIndex == -1) {
            nextBeat = 0f;
            for (int i = 0; i < part.notes.size(); i++) {
                nextNote = part.notes.get(i);
                nextBeat += nextNote.getBeats();

                if (nextBeat >= subbeat / (float)beatParameters.subbeats) {
                    nextNoteIndex = i + 1;
                    nextNote = nextNoteIndex < part.notes.size() ?
                            part.notes.get(nextNoteIndex) : null;
                    break;
                }
            }
        }

        if (nextNoteIndex == -1 || nextNote == null) {
            return;
        }

        if (nextBeat == subbeat / (float)beatParameters.subbeats) {

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
