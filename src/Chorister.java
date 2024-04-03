package org.player;

import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

public class Chorister implements Runnable{

    private Note note;
    private int length;
    // public so the conductor can get the note to play
    public Baton baton;
    private SourceDataLine line;
    private final AtomicBoolean playing;
    Chorister(Note note, Baton baton, SourceDataLine line, AtomicBoolean playing){
        this.note = note;
        this.baton = baton;
        this.line = line;
        this.playing = playing;
    }
    /**
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
        while(playing.get()){
            // This means that the note was finished playing, so the thread can wait for another note
            baton.finished();
            // if playing is still true then play note
            if(playing.get()) {
                line.write(note.sample(), 0, length);
                line.write(Note.REST.sample(), 0, 50);
            }
        }
    }
    // Setting length of note before note plays
    public void setTime(int length){
        this.length = length;
    }
}
