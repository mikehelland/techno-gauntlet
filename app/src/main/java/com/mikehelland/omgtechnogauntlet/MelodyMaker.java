package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

import java.util.ArrayList;
import java.util.Random;

public class MelodyMaker {

    Random rand;
    Context mContext;

    int key;

    NoteList currentMelody;
    NoteList mCurrentMotif;


    int[] ascale;

    String[] keyCaptions;
    String[] keys;
    String[] scales;
    String[] scaleCaptions;

    private int scaleI;

    public MelodyMaker(Context context) {
        mContext = context;

        keyCaptions = context.getResources().getStringArray(R.array.keys_captions);
        keys = context.getResources().getStringArray(R.array.keys);
        scales = context.getResources().getStringArray(R.array.quantizer_values);
        scaleCaptions = context.getResources().getStringArray(R.array.quantizer_entries);

        rand = new Random();

        pickRandomKey();
        pickRandomScale();

    }

    public String getKeyName() {
        return keyCaptions[key] + " " + scaleCaptions[scaleI];
    }

    public void pickRandomKey() {
        // pick a key
        setKey(rand.nextInt(keys.length));

    }

    public void pickRandomScale() {
        // pick a scale
        setScale(rand.nextInt(scales.length));
    }


    public ArrayList<Note> makeMelody(double totalBeats) {

        return makeMelody(totalBeats, -1.0d);
    }

    public NoteList makeMelody(double totalBeats, double beatBias) {

        // use the motif approach
        if (rand.nextBoolean()) {
            return makeMelodyFromMotif(totalBeats);
        }


        NoteList line = new NoteList();

        // choose a duration to tend toward
        if (beatBias == -1.0d) {
            int bbr = rand.nextInt(3);
            beatBias = bbr == 0 ? 0.250d : bbr == 1 ? 0.5d : 1.0d;
        }

        int adjust = 0;

        int restRatio = 2 + rand.nextInt(10);


        double playedBeats = 0.0f;
        double currentNoteDuration;
        int currentNoteNumber;
        Note currentNote;

        int lastNote = 0;

        int notesAway;

        boolean goingUp = rand.nextBoolean();

        while (playedBeats < totalBeats) {

            currentNote = new Note();
            //currentNote.setBeatPosition(playedBeats);

            line.add(currentNote);

            currentNoteDuration = Math.min(getRandomNoteDuration(beatBias),
                    totalBeats - playedBeats);
            currentNote.setBeats(currentNoteDuration);

            playedBeats += currentNoteDuration;

            // rest?
            if (rand.nextInt(restRatio) == 0) {
                currentNote.setRest(true);
                continue;
            }

            // play the last note
            if (rand.nextBoolean()) {
                currentNote.setBasicNote(adjust + lastNote);
                continue;
            }

            // maybe change the direction
            if (rand.nextBoolean()) {
                goingUp = rand.nextBoolean();
            }

            // play a different note
            notesAway = rand.nextBoolean() ? 1 : rand.nextBoolean() ? 2 : rand.nextBoolean() ? 3 : 1;

            if (!goingUp) {
                notesAway = notesAway * -1;
            }
            currentNoteNumber = lastNote + notesAway;
            currentNote.setBasicNote(adjust + currentNoteNumber);
            lastNote = currentNoteNumber;

        }

        // go backwards
        playedBeats = 0.0d;
        if (rand.nextInt(3) == 0) {
            NoteList line2 = new NoteList();
            for (int ii = line.size(); ii > 0; ii--) {
                currentNote = line.get(ii - 1);
                //currentNote.setBeatPosition(playedBeats);
                playedBeats += currentNote.getBeats();
                line2.add(currentNote);
            }
            line = line2;
        }

        currentMelody = line;
        return line;
    }

    public void applyScale(Channel channel, int chord) {
        applyScale(channel.getNotes(), chord);

        channel.fitNotesToInstrument();
    }

    public void applyScale(NoteList notes, int chord) {


        Note note;
        for (int i = 0; i < notes.size(); i++) {

            note = notes.get(i);

            if (note.isRest()) {
                continue;
            }

            note.setScaledNote(scaleNote(note.getBasicNote(), chord));

        }

    }

    public int scaleNote(int oldNoteNumber, int chord) {
        int newNoteNumber;
        int octaves;

        octaves = 0;

        newNoteNumber = oldNoteNumber + chord;

        while (newNoteNumber >= ascale.length) {
            octaves++;
            newNoteNumber = newNoteNumber - ascale.length;
        }

        while (newNoteNumber < 0) {
            octaves--;
            newNoteNumber = newNoteNumber + ascale.length;
        }

        newNoteNumber = ascale[newNoteNumber];


        return key + newNoteNumber + octaves * 12;
    }

    public double getRandomNoteDuration(double beatBias) {

        if (rand.nextBoolean() ) {
            return beatBias;
        }

        // 50 50 chance we get an eighth note
        if (rand.nextBoolean())
            return 0.5d;

        // quarter note
        if (rand.nextBoolean())
            return 1.0d;

        // go for a sixteenth
        if (rand.nextBoolean())
            return 0.25d;

        // try and eighth note again
        if (rand.nextBoolean())
            return 0.5d;


        return beatBias;

    }

    public NoteList makeMelodyFromMotif(ArrayList<Note> motif, double totalbeats) {

        NoteList ret = new NoteList();

        int loops = (int)(totalbeats / 2.0d);

        melodify(motif);

        double playedBeats = 0.0d;

        int pattern = rand.nextInt(5);


        for (int i = 0; i < loops; i++) {
            if ((pattern > 2 && i%2==1)
                    || (pattern == 2 && i == loops -1)
                    || (pattern == 1 && i%2==0)) {
                if (rand.nextInt(4) > 0) {
                    Note note = new Note();
                    note.setRest(true);
                    note.setBeats(2.0d);
                    ret.add(note);
                }
                else {
                    Note note = motif.get(0).cloneNote();
                    ret.add(note);
                    double beatsFromFirstNote = note.getBeats();

                    if (beatsFromFirstNote < 2.0d) {
                        note = new Note();
                        note.setRest(true);
                        note.setBeats(2.0d - beatsFromFirstNote);
                        ret.add(note);
                    }
                }
                playedBeats += 2.0d;


            }
            else {
                for (Note note : motif) {
                    note = note.cloneNote();
                    ret.add(note);
                    playedBeats += note.getBeats();
                }

            }

        }

        return ret;
    }

    public NoteList makeMelodyFromMotif(double totalbeats) {
        NoteList motif = makeMotif();
        currentMelody = makeMelodyFromMotif(motif, totalbeats);

        return currentMelody;
    }

    // a 2 beat motif
    public NoteList makeMotif() {
        NoteList ret = new NoteList();

        double beatsNeeded = 2.0d;
        double beatsUsed = 0.0d;

        Note currentNote;
        double currentBeats;

        double beatBias;
        if (rand.nextBoolean()) {
            beatBias = 1;
        }
        else {
            beatBias = 0.5;
        }

        while (beatsUsed < beatsNeeded) {
            currentNote = new Note();

            // first note 1 in 6 on the downbeat
            if (beatsUsed == 0) {
                if (rand.nextInt(6) == 0) {
                    currentNote.setRest(true);
                }
            }
            else {
                if (rand.nextInt(3) == 0) {
                    currentNote.setRest(true);
                }
            }

            currentBeats = getRandomNoteDuration(beatBias);
            currentBeats = Math.min(currentBeats, beatsNeeded - beatsUsed);
            currentNote.setBeats(currentBeats);

            beatsUsed += currentBeats;
            ret.add(currentNote);


        }

        mCurrentMotif = ret;
        return ret;
    }

    public ArrayList<Note> melodify(ArrayList<Note> motif) {

        int lastNote = 0;
        int notesAway;
        boolean goingUp = rand.nextBoolean();

        Note note;
        for (int i = 1; i < motif.size(); i++) {
            note = motif.get(i);

            // play the last note
            if (rand.nextBoolean()) {
                note.setBasicNote(lastNote);
                continue;
            }

            // maybe change the direction
            if (rand.nextBoolean()) {
                goingUp = rand.nextBoolean();
            }

            // play a different note
            notesAway = rand.nextBoolean() ? 1 : rand.nextBoolean() ? 2 : rand.nextBoolean() ? 3 : 1;

            if (!goingUp) {
                notesAway = notesAway * -1;
            }

            note.setBasicNote(notesAway);

        }

        return motif;
    }

    public String getScale() {
        return scales[scaleI];
    }

    public int[] getScaleArray() {
        return ascale;
    }

    public int getScaleIndex() {
        return scaleI;
    }

    public int getKey() {
        return key;
    }

    public String[] getKeyCaptions() {
        return keyCaptions;
    }

    public String getNoteName(int i) {
        return keyCaptions[i % 12];
    }

    public int getScaleLength() {
        return ascale.length;
    }

    public void setScale(int scaleIndex) {
        scaleI = scaleIndex;
        ascale = buildScale(scales[scaleI]);
    }

    public void setScale(String scale) {

        for (int i = 0; i < scales.length; i ++) {
            if (scale.equals(scales[i])) {
                scaleI = i;
                ascale = buildScale(scales[i]);
                return;
            }
        }
    }

    public void setKey(int keyIndex) {
        key = keyIndex;
    }

    public NoteList getCurrentMelody() {
        return currentMelody;
    }

    static int[] buildScale(String quantizerString) {
        if (quantizerString != null && quantizerString.length() > 0) {
            String[] parts = quantizerString.split(",");
            int[] scale = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                scale[i] = Integer.parseInt(parts[i]);
            }
            return scale;
        } else {
            return null;
        }
    }

    public void cloneCurrentMelodyTo(NoteList noteList) {
        noteList.clear();

        Note newNote;
        for (Note note : currentMelody) {
            newNote = note.cloneNote();
            noteList.add(newNote);
        }
    }

}
