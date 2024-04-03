package org.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;

    NoteLength(float length) {
        timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

enum Note {
    // REST Must be the first 'Note'
    REST,
    A4,
    A4S,
    B4,
    C4,
    C4S,
    D4,
    D4S,
    E4,
    F4,
    F4S,
    G4,
    G4S,
    A5;

    public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
    public static final int MEASURE_LENGTH_SEC = 1;

    // Circumference of a circle divided by # of samples
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    private final double FREQUENCY_A_HZ = 440.0d;
    private final double MAX_VOLUME = 127.0d;

    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            // Create sinusoidal data sample for the desired frequency
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte) (Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    public byte[] sample() {
        return sinSample;
    }
}

public class Tone {
    private final AudioFormat af;

    Tone(String[] args) throws Exception {
        String songFile = "DefaultMaryLamb.txt";
        if(args.length != 0 && args[0] != null)
            songFile = args[0];
        this.af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        final List<BellNote> song = loadSong(songFile);
        // Creates threads that will play the notes
        createChoristers();

        if(song == null){
            System.out.println("Song can't be played due to syntax errors");
            System.exit(1);
        }
        this.playSong(song);
    }

    /**
     * @author Aidan Scott
     * Validates and reads data from input filename
     * @param filename
     * @return null if data is invalid or list of BellNotes if data is valid
     */
    private List<BellNote> loadSong(String filename){
        // If the filename is null, return null and tell user that a file name must be given
        if (filename == null) {
            System.out.println("Please give valid file input, file name must be given");
            return new LinkedList<>();
        }
        // Decide if the input data is for a local file or using a global path
        if (!filename.contains("/")) {
            System.out.println("File assumed to be in resources folder");
            filename = "src/" + filename;
        } else // If there is a / in the string, then the file we assume its global path
            System.out.println("File assumed to be file path");

        File gameFile = new File(filename);
        List<String> errors = new LinkedList<>(); // List that errors are added to
        List<BellNote> notes = new LinkedList<>();
        Map<String, String> noteLength = new HashMap<>();
        noteLength.put("8", "EIGTH");
        noteLength.put("4", "QUARTER");
        noteLength.put("2", "HALF");
        noteLength.put("1", "WHOLE");
        try (Scanner fileReader = new Scanner(gameFile)){
            int line = 1;
            while (fileReader.hasNextLine()) {
                String[] data = fileReader.nextLine().split(" ");
                Note note = null;
                NoteLength length = null;
                try{
                    note = Note.valueOf(data[0].toUpperCase());
                }catch(IllegalArgumentException e){
                    errors.add("Your stated file has an invalid character of '" + data[0] + "' at line " + line);
                }
                if(data.length < 2){
                    errors.add("Note length not found on line " + line);
                }else {
                    String noteL = noteLength.get(data[1]);
                    if(noteL == null)
                        noteL = "0";
                    try {
                        length = NoteLength.valueOf(noteL);
                    } catch (IllegalArgumentException e) {
                        errors.add("your stated file has an invalid character of '" + data[1] + "' at line " + line + " column 2");
                    }
                }
                notes.add(new BellNote(note, length));
                line++;
            }
            if(line == 1){
                errors.add("Zero Notes found in file. Empty file.");
            }

        }catch(FileNotFoundException e){
            System.out.println("Your file was not found, the path we used to find your file was: " + filename);
            return new LinkedList<>();
        }
        if(!errors.isEmpty()){
            for(String error:errors)
                System.err.println(error);
            System.out.println("--------Supported Values for Notes and note length--------");
            System.out.println("Note Values: " + Arrays.toString(Note.values()));
            System.out.println("Note Lengths: " + noteLength.keySet());
            return new LinkedList<>();
        }
        return notes;
    }

    public static NoteLength valueOfLabel(int label) {
        for (NoteLength e : NoteLength.values()) {
            if (e.timeMs() == label) {
                return e;
            }
        }
        return null;
    }

    void playSong(List<BellNote> song) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            for (BellNote bn : song) {
                playNote(line, bn);
            }
            line.drain();
        }
    }
    private void createChoristers(){

    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}

class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }
}

