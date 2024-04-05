package org.player;

import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Aidan Scott
 * The Chorister is able to set a note, then able to play that note when told
 * By using Baton, it can make the conductor 'Tone' wait for itself to finish playing its note
 * Length must be set before baton notifies the Chorister to play.
 * Each Chorister has a separate baton that tells it to play, one baton for each chorister
 */
public class Chorister implements Runnable {

    private final Note note;
    private final SourceDataLine line;
    private final AtomicBoolean playing;
    public Baton baton;
    public Thread thread = null; // internally stored thread that runs this class. Makes .join easier
    private int length; // the length that is set before every note call

    /**
     * Chorister plays notes when told. Note length can be set as well as its thread
     *
     * @param note    The note that it will play
     * @param baton   public so the conductor can get the note to play
     * @param line    fundamental to playing each note, used in the previous single threaded TOne player
     * @param playing used to tell the thread to turn off. shared in memory between all threads
     */
    Chorister(Note note, Baton baton, SourceDataLine line, AtomicBoolean playing) {
        this.note = note;
        this.baton = baton;
        this.line = line;
        this.playing = playing;
    }

    /**
     * Run loops as long as playing is true. Each loop it waits for the baton to notify it to continue.
     * When it is released, it can play its note at the stated time.
     * When terminated the thread, it is safe to "flush" the button so this is released. Playing should be set to false
     * before the baton is flushed.
     * todo create protections so users are forced to call functions in the right order
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (playing.get()) {
            // This means that the note was finished playing, so the thread can wait for another note
            baton.finished();
            // if playing is still true then play note
            if (playing.get()) {
                // play the note. (this is magic known only by arcane wizards.)
                line.write(note.sample(), 0, length);
                line.write(Note.REST.sample(), 0, Note.SAMPLE_RATE * 50 / 1000);
            }
        }
    }

    // Setting length of note usually before note plays
    public void setTime(int length) {
        this.length = length;
    }

    // Setting thread as this Chorister's thread to allow for easy access of the .join function
    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
