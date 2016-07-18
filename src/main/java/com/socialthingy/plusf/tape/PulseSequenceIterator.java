package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;

import java.util.Iterator;

public class PulseSequenceIterator implements Iterator<Boolean> {
    private final SignalState signalState;
    private final Adjustment initialState;
    private int[] pulseLengths;
    private int pulseCount;
    private int pulseIdx;
    private int tstatesUntilChange;
    private boolean initialPulse = true;

    public PulseSequenceIterator(final Adjustment initialState, final SignalState signalState, final int[] pulseLengths) {
        this.signalState = signalState;
        this.initialState = initialState;
        reset(pulseLengths);
    }

    public void reset(final int[] pulseLengths) {
        this.pulseCount = pulseLengths.length;
        this.pulseLengths = pulseLengths;
        this.tstatesUntilChange = pulseLengths[0];
        this.initialPulse = true;
        this.pulseIdx = 0;
    }

    @Override
    public boolean hasNext() {
        return !(tstatesUntilChange == 0 && pulseIdx == pulseCount);
    }

    @Override
    public Boolean next() {
        if (initialPulse) {
            initialPulse = false;
            signalState.adjust(initialState);
        }

        final boolean state = signalState.get();
        tstatesUntilChange--;
        if (tstatesUntilChange == 0) {
            pulseIdx++;
            if (pulseIdx < pulseCount) {
                tstatesUntilChange = pulseLengths[pulseIdx];
            }
            signalState.flip();
        }

        return state;
    }
}
