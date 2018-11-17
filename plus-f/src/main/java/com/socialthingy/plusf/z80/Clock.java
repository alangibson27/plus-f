package com.socialthingy.plusf.z80;

public class Clock {
    private int ticks;

    public void reset() {
        this.ticks = 0;
    }

    public void tick(final int ticks) {
        if (ticks > 0) {
            this.ticks += ticks;
        }
    }

    public int getTicks() {
        return this.ticks;
    }
}
