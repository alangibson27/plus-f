package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;

import java.util.Iterator;
import java.util.Optional;

public class PulseSequenceIterator implements Iterator<TapeBlock.Bit> {
    private final SignalState signalState;
    private final int[] pulseLengths;
    private final int pulseCount;
    private int pulseIdx = 0;
    private int tstatesUntilChange;
    private boolean initialPulse = true;
    private final Adjustment initialState;

    public PulseSequenceIterator(final Adjustment initialState, final SignalState signalState, final int[] pulseLengths) {
        this.signalState = signalState;
        this.pulseCount = pulseLengths.length;
        this.pulseLengths = pulseLengths;
        this.tstatesUntilChange = pulseLengths[0];
        this.initialState = initialState;
    }

    @Override
    public boolean hasNext() {
        return !(tstatesUntilChange == 0 && pulseIdx == pulseCount);
    }

    @Override
    public TapeBlock.Bit next() {
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

        return new TapeBlock.Bit(state, "pulse");
    }
}
