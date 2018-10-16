package com.socialthingy.plusf.spectrum;

import java.util.ArrayList;
import java.util.List;

public class Clock {
    private int ticks;
    private List<Runnable> resetHandlers = new ArrayList<>();

    public void reset() {
        this.ticks = 0;
        resetHandlers.forEach(Runnable::run);
    }

    public void tick(final int ticks) {
        this.ticks += ticks;
    }

    public int getTicks() {
        return this.ticks;
    }

    public void addResetHandler(final Runnable resetHandler) {
        resetHandlers.add(resetHandler);
    }
}
