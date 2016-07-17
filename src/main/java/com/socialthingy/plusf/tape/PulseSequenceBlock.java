package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class PulseSequenceBlock extends TapeBlock {

    public static Try<PulseSequenceBlock> read(final InputStream tzxFile) {
        try {
            final int pulseCount = nextByte(tzxFile);
            final int[] pulseLengths = new int[pulseCount];
            for (int i = 0; i < pulseCount; i++) {
                pulseLengths[i] = nextWord(tzxFile);
            }

            return Try.success(new PulseSequenceBlock(pulseLengths));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private final int pulseCount;
    private final int[] pulseLengths;
    private final Adjustment initialState;

    public PulseSequenceBlock(final Adjustment initialState, final int[] pulseLengths) {
        this.pulseCount = pulseLengths.length;
        this.pulseLengths = pulseLengths;
        this.initialState = initialState;
    }

    public PulseSequenceBlock(final int[] pulseLengths) {
        this(Adjustment.SET_LOW, pulseLengths);
    }

    @Override
    public Iterator<Bit> bits(final SignalState signalState) {
        return new PulseSequenceIterator(initialState, signalState, pulseLengths);
    }

    @Override
    public String toString() {
        final String pulses = Arrays.stream(pulseLengths).limit(5).mapToObj(String::valueOf).collect(Collectors.joining(","));
        return String.format("Pulse block - %d pulses [%s ...]", pulseCount, pulses);
    }
}
