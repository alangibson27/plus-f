package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.SignalState.Adjustment;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PulseSequenceBlock extends TapeBlock {

    public static Try<PulseSequenceBlock> read(final InputStream tzxFile) {
        try {
            final int pulseCount = nextByte(tzxFile);
            final int[] pulseLengths = new int[pulseCount];
            for (int i = 0; i < pulseCount; i++) {
                pulseLengths[i] = nextWord(tzxFile);
            }

            return Try.success(new PulseSequenceBlock(Adjustment.NO_CHANGE, pulseLengths));
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
    public BlockBits getBitList(SignalState signalState) {
        return new PulseSequenceBlockBits(signalState);
    }

    @Override
    public String toString() {
        final String pulses = Arrays.stream(pulseLengths).limit(5).mapToObj(String::valueOf).collect(Collectors.joining(","));
        return String.format("Pulse block - %d pulses [%s ...]", pulseCount, pulses);
    }

    private class PulseSequenceBlockBits implements BlockBits {
        private final SignalState state;
        private int pulseIdx;
        private int bitIdx;

        public PulseSequenceBlockBits(final SignalState signalState) {
            this.state = signalState;
            signalState.adjust(initialState);
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
}
