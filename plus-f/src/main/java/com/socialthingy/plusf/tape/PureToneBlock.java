package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class PureToneBlock extends TapeBlock {

    public static Try<TapeBlock> read(final InputStream tzxFile) {
        try {
            final int pulseLength = nextWord(tzxFile);
            final int toneLength = nextWord(tzxFile);
            return Try.success(create(pulseLength, toneLength));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public static PulseSequenceBlock create(final int pulseLength, final int toneLength) {
        final int[] pulseLengths = new int[toneLength];
        for (int i = 0; i < toneLength; i++) {
            pulseLengths[i] = pulseLength;
        }
        return new PulseSequenceBlock(SignalState.Adjustment.SET_LOW, pulseLengths);
    }

    @Override
    public boolean isDataBlock() {
        return true;
    }
}
