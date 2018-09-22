package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;

class PulseSequenceSignal implements BlockSignal {
    private final SignalState state;
    private int[] pulseLengths;
    private int pulseCount;
    private int pulseIdx;
    private int bitIdx;

    public PulseSequenceSignal(
        final SignalState signalState,
        final Adjustment initialSignalAdjustment,
        final int[] pulseLengths
    ) {
        this.state = signalState;
        this.pulseLengths = pulseLengths;
        this.pulseCount = pulseLengths.length;
        signalState.adjust(initialSignalAdjustment);
    }

    public void setPulseLengths(final int[] pulseLengths) {
        this.pulseLengths = pulseLengths;
        this.pulseCount = pulseLengths.length;
        this.pulseIdx = 0;
        this.bitIdx = 0;
    }

    @Override
    public int skip(int count) {
        int skipped = 0;
        while (count > 0 && pulseIdx < pulseCount) {
            int bitsLeftInPulse = pulseLengths[pulseIdx] - bitIdx;
            if (count < bitsLeftInPulse) {
                bitIdx += count;
                skipped += count;
                count = 0;
            } else {
                state.flip();
                skipped += bitsLeftInPulse;
                count -= bitsLeftInPulse;
                pulseIdx++;
                bitIdx = 0;
            }
        }
        return skipped;
    }

    @Override
    public boolean hasNext() {
        return (pulseIdx < pulseCount - 1) ||
                (pulseIdx == pulseCount - 1 && bitIdx < pulseLengths[pulseCount - 1]);
    }

    @Override
    public Boolean next() {
        final Boolean next = state.get();
        bitIdx++;
        if (bitIdx >= pulseLengths[pulseIdx]) {
            state.flip();
            pulseIdx++;
            bitIdx = 0;
        }
        return next;
    }
}
