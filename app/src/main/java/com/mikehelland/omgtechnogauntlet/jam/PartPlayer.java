package com.mikehelland.omgtechnogauntlet.jam;

import java.util.ArrayList;

/**
 * Created by m on 3/30/18.
 * used by Player to do the Part stuff
 */

class PartPlayer {

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

    static void getSoundsToPlayForPartAtSubbeat(ArrayList<PlaySoundCommand> commands,
                                                Section section, Part part,
                                                int subbeat, int chord) {
        if (part.useSequencer()) {
            getDrumbeatSounds(commands, part, subbeat);
        } else {
            getNoteSounds(commands, section, part, subbeat, chord);
        }
    }

    private static void getDrumbeatSounds(ArrayList<PlaySoundCommand> commands, Part part, int subbeat) {
        if (!part.audioParameters.mute) {
            int i = 0;
            for (SequencerTrack track : part.sequencerPattern.getTracks()) {
                if (track.getData()[subbeat] && !track.isMuted()) {
                    commands.add(new PlaySoundCommand(i < part.poolIds.length ? part.poolIds[i] : -1,
                            -1, -1,
                            track.audioParameters.volume * part.audioParameters.volume,
                            track.audioParameters.pan + part.audioParameters.pan,
                            track.audioParameters.speed));
                }
                i++;
            }
        }
    }

    private static void getNoteSounds(ArrayList<PlaySoundCommand> commands,
                                      Section section, Part part, int subbeat, int chord) {

        if (part.notes.size() == 0) {
            return;
        }

        //todo skipping oscillators for now but add them
        if (part.soundSet.isOscillator()) {
            return;
        }

        if (subbeat == 0) {
            part.nextBeat = 0;
            part.nextNote = part.notes.get(0);
            part.nextNoteIndex = 0;

            if (part.soundSet.isChromatic()) {
                KeyHelper.applyScaleToPart(section, part, chord);
            }
        }

        if (part.nextNote == null) {
            return;
        }

        if (part.nextBeat == subbeat / (float)section.beatParameters.subbeats) {

            if (!part.getMute() && !part.nextNote.isRest()) {
                commands.add(new PlaySoundCommand(part.poolIds[part.nextNote.getInstrumentNote()],
                        part.nextNote.getInstrumentNote(), (float) part.nextNote.getBeats(),
                        part.audioParameters.volume,
                        part.audioParameters.pan,
                        part.audioParameters.speed));
            }

            part.nextBeat += part.nextNote.getBeats();
            part.nextNoteIndex++;
            part.nextNote = part.nextNoteIndex < part.notes.size() ?
                    part.notes.get(part.nextNoteIndex) : null;
        }
    }
}
