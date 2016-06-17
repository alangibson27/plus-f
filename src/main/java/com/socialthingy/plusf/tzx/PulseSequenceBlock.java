package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class PulseSequenceBlock extends TzxBlock {

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

    private PulseSequenceBlock(final int pulseCount, final int[] pulseLengths) {
        this.pulseCount = pulseCount;
        this.pulseLengths = pulseLengths;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        boolean state = initialState;
        for (int i = 0; i < pulseCount; i++) {
            tape.add(new Bit(state, "pilot"), pulseLengths[i]);
            state = !state;
        }

        return state;
    }

    @Override
    public String toString() {
        return String.format("Pulse block - %d pulses", pulseCount);
    }
}