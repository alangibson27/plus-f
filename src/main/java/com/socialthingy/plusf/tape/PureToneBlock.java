package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class PureToneBlock extends TapeBlock {

    public static Try<PureToneBlock> read(final InputStream tzxFile) {
        try {
            final int pulseLength = nextWord(tzxFile);
            final int toneLength = nextWord(tzxFile);

            return Try.success(new PureToneBlock(pulseLength, toneLength));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private final int pulseLength;
    private final int toneLength;

    public PureToneBlock(final int pulseLength, final int toneLength) {
        this.pulseLength = pulseLength;
        this.toneLength = toneLength;
    }

    @Override
    public Iterator<Boolean> bits(final SignalState signalState) {
        return new PureToneIterator(signalState);
    }

    @Override
    public String toString() {
        return String.format("Pure tone block: %d pulses of %d", toneLength, pulseLength);
    }

    private class PureToneIterator implements Iterator<Boolean> {
        private final SignalState signalState;
        private boolean initialPulse = true;
        private int pulsesLeft = toneLength;
        private int tstatesUntilChange = pulseLength;

        private PureToneIterator(final SignalState signalState) {
            this.signalState = signalState;
        }

        @Override
        public boolean hasNext() {
            return !(pulsesLeft == 0 && tstatesUntilChange == 0);
        }

        @Override
        public Boolean next() {
            if (initialPulse) {
                signalState.set(false);
                initialPulse = false;
            }

            final boolean state = signalState.get();
            tstatesUntilChange--;
            if (tstatesUntilChange == 0) {
                pulsesLeft--;
                if (pulsesLeft > 0) {
                    tstatesUntilChange = pulseLength;
                }
                signalState.flip();
            }

            return state;
        }
    }
}
