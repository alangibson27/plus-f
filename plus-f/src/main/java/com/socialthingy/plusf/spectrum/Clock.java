package com.socialthingy.plusf.spectrum;

public class Clock {
    private int ticks;
    private Runnable resetHandler;

    public void reset() {
        this.ticks = 0;
        if (resetHandler != null) {
            resetHandler.run();
        }
    }

    public void tick(final int ticks) {
        this.ticks += ticks;
    }

    public int getTicks() {
        return this.ticks;
    }

    public void setResetHandler(final Runnable resetHandler) {
        this.resetHandler = resetHandler;
    }
}
