package com.socialthingy.plusf.tape;

import java.time.Duration;
import java.util.Iterator;

public class PauseIterator implements Iterator<TapeBlock.Bit> {
    public static final TapeBlock.Bit PAUSE_BIT = new TapeBlock.Bit(false, "pause");
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
    public TapeBlock.Bit next() {
        signalState.set(false);
        tstatesUntilEnd--;
        return PAUSE_BIT;
    }
}
