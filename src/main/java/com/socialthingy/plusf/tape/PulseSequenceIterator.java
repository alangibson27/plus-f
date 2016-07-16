package com.socialthingy.plusf.tape;

import java.util.Iterator;

public class PulseSequenceIterator implements Iterator<TapeBlock.Bit> {
    private final SignalState signalState;
    private final int[] pulseLengths;
    private final int pulseCount;
    private int pulseIdx = 0;
    private int tstatesUntilChange;

    public PulseSequenceIterator(final SignalState signalState, final int[] pulseLengths) {
        this.signalState = signalState;
        this.pulseCount = pulseLengths.length;
        this.pulseLengths = pulseLengths;
        this.tstatesUntilChange = pulseLengths[0];
    }

    @Override
    public boolean hasNext() {
        return (tstatesUntilChange > 0) || (pulseIdx < pulseCount - 1);
    }

    @Override
    public TapeBlock.Bit next() {
        if (tstatesUntilChange == 0) {
            pulseIdx++;
            tstatesUntilChange = pulseLengths[pulseIdx];
            signalState.flip();
        }
        tstatesUntilChange--;
        return new TapeBlock.Bit(signalState.get(), "pulse");
    }
}
