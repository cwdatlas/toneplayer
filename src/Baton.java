package org.player;

public class Baton {
    private boolean notePlaying = false;

    // ONLY the conductor (Tone) should call this method
    public synchronized void play(){
        // Notifies the waiting chorister to play the note
        notePlaying = true;
        this.notify();
        while(notePlaying){
            try{
                wait();
            } catch (InterruptedException e) {
                System.out.println("Play wait was interrupted");
            }
        }
    }
    // ONLY Chorister should call this method
    public synchronized void finished(){
        // Notifies the waiting Baton to play the note
        notePlaying = false;
        this.notify();
        while(!notePlaying){
            try{
                wait();
            } catch (InterruptedException e) {
                System.out.println("Finished wait was interrupted");
            }
        }
    }
    // ONLY used when stopping all choristers and exiting program and playing = false
    public synchronized void flush(){
        notePlaying = true;
        this.notify();
    }
}
