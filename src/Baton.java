package org.player;

/**
 * @author Aidan Scott
 * Baton is the 'flip flop mutex' (I know it's not exactly a mutex, but I like the name)
 * used to make the chorister wait when other notes are being played
 * and the conductor 'tone' wait when the note is being played
 */
public class Baton {
    // Used to tell which side is waiting.
    private boolean notePlaying = false;

    /**
     * ONLY the conductor (Tone) should call the play method
     * This will notify the chorister and make the conductor 'tone' wait for the chorister to finish playing its note
     */
    public synchronized void play() {
        // Notifies the waiting chorister to play the note
        notePlaying = true; // set that the note is playing
        this.notify(); // notify the chorister
        while (notePlaying) { // make sure if the thread is interrupted, the conductor wont stop waiting
            try {
                wait();
            } catch (InterruptedException e) {
                // Simply notify that the thread was interrupted.
                System.out.println("Play wait was interrupted");
            }
        }
    }

    /**
     * ONLY Chorister should call the finished method
     * This will notify the conductor and make the chorister wait for the conductor to say to play another note
     */
    public synchronized void finished() {
        // Notifies the waiting Baton to play the note
        notePlaying = false; // sets notePlaying to false, its finished playing the note, so it shouldn't be true!
        this.notify(); // tell the conductor to stop waiting (check previous function for more details, it's the same.
        while (!notePlaying) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Finished wait was interrupted");
            }
        }
    }

    /**
     * ONLY used when stopping the chorister and exiting program and playing = false
     * Flush is used to stop the chorister waiting, but doesn't make the conductor 'tone' wait
     */
    public synchronized void flush() {
        notePlaying = true;
        this.notify();
    }
}
