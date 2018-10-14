package com.socialthingy.plusf.spectrum;

public class Clock {
    private int ticks;

    public void reset() {
        this.ticks = 0;
    }

    public void tick(final int ticks) {
        this.ticks += ticks;
    }

    public int getTicks() {
        return this.ticks;
    }
}
