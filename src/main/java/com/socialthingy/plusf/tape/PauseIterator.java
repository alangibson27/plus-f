package com.socialthingy.plusf.tape;

import java.time.Duration;
import java.util.Iterator;

public class PauseIterator implements Iterator<Boolean> {
    private final SignalState signalState;
    private long tstatesUntilEnd;

    public PauseIterator(final SignalState signalState, final Duration pauseLength) {
        this.signalState = signalState;
        this.tstatesUntilEnd = pauseLength.toMillis() * 3500;
    }

    @Override
    public boolean hasNext() {
        return tstatesUntilEnd > 0;
    }

    @Override
    public Boolean next() {
        signalState.set(false);
        tstatesUntilEnd--;
        return false;
    }
}
