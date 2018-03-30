package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import java.util.ArrayList;

public class SectionToJSON {

    public static String getData(Section section) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\": \"SECTION\", \"tags\": \"");
        sb.append(section.tags);
        sb.append("\", \"omgVersion\":0.9, \"madeWith\": \"");
        sb.append("");
        sb.append("\", \"keyParameters\": ");
        section.keyParameters.getData(sb);
        sb.append(", \"beatParameters\": ");
        section.beatParameters.getData(sb);
        sb.append(", ");
        getChordsData(sb, section);

        sb.append(", \"parts\" : [");

        for (Part part : section.parts) {
            getPartData(sb, part, section);
            sb.append(",");
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append("]}");

        Log.d("MGH getData", sb.toString());
        return sb.toString();

    }

    static private void getChordsData(StringBuilder sb, Section jam) {

        sb.append("\"chordProgression\" : [");

        boolean first = true;

        for (int chord : jam.progression) {
            if (first)
                first = false;
            else
                sb.append(", ");

            sb.append(chord);
        }
        sb.append("]");

    }

    static private void getPartData(StringBuilder sb, Part part, Section section) {

        boolean useSequencer = part.surface.getURL().equals(Surface.PRESET_SEQUENCER);
        sb.append("{\"type\" : \"PART\", ");
        sb.append("\", \"surface\" : ");
        part.surface.getData(sb);
        sb.append(", \"soundSet\" : ");
        part.soundSet.getData(sb);
        sb.append(", \"audioParameters\" : ");
        part.audioParameters.getData(sb);
        sb.append(", ");
        ///todo add beat and rootNote objects from jam

        if (useSequencer) {
            getTrackData(sb, part, section);
        } else {
            getNoteData(sb, part);
        }

        sb.append("}");
    }

    static private void getNoteData(StringBuilder sb, Part part) {

        sb.append(", \"octave\": ");
        sb.append(part.octave);
        sb.append(", \"notes\" : [");

        boolean first = true;
        for (Note note : part.notes) {

            if (first)
                first = false;
            else
                sb.append(", ");

            sb.append("{\"rest\": ");
            sb.append(note.isRest());
            sb.append(", \"beats\": ");
            sb.append(note.getBeats());
            if (!note.isRest()) {
                sb.append(", \"note\" :");
                sb.append(note.getBasicNote());
            }
            sb.append("}");
        }
        sb.append("]");
    }

    static private void getTrackData(StringBuilder sb, Part part, Section section) {
        sb.append(", \"tracks\": [");

        int totalSubbeats = BeatParameters.getTotalSubbeats(section.beatParameters);
        ArrayList<SoundSet.Sound> sounds = part.soundSet.getSounds();
        for (int p = 0; p < sounds.size(); p++) {

            sb.append("{\"name\": \"");
            sb.append(sounds.get(p).getName());
            sb.append("\", \"sound\": \"");
            sb.append(sounds.get(p).getURL());
            sb.append("\", ");
            if (p < part.sequencerPattern.getTracks().size()) {
                sb.append("\"audioParameters\": ");
                part.sequencerPattern.getTrack(p).audioParameters.getData(sb);
            }
            sb.append(", \"data\": [");
            for (int i = 0; i < totalSubbeats; i++) {
                sb.append(part.pattern[p][i] ? 1 : 0);
                if (i < totalSubbeats - 1)
                    sb.append(",");
            }
            sb.append("]}");

            if (p < sounds.size() - 1)
                sb.append(",");

        }

        sb.append("]");
    }


}
