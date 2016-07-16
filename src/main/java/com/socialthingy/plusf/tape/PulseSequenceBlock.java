package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.RepeatingList;
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

            return Try.success(new PulseSequenceBlock(pulseCount, pulseLengths));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private final int pulseCount;
    private final int[] pulseLengths;

    public PulseSequenceBlock(final int pulseCount, final int[] pulseLengths) {
        this.pulseCount = pulseCount;
        this.pulseLengths = pulseLengths;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        boolean state = false;
        for (int i = 0; i < pulseCount; i++) {
            tape.add(new Bit(state, "pilot"), pulseLengths[i]);
            state = !state;
        }

        return state;
    }

    @Override
    public Iterator<Bit> bits(final SignalState signalState) {
        signalState.set(false);
        return new PulseSequenceIterator(signalState, pulseLengths);
    }

    @Override
    public String toString() {
        final String pulses = Arrays.stream(pulseLengths).limit(5).mapToObj(String::valueOf).collect(Collectors.joining(","));
        return String.format("Pulse block - %d pulses [%s ...]", pulseCount, pulses);
    }
}
