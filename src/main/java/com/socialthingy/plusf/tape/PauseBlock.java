package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Iterator;

public class PauseBlock extends TapeBlock {

    public static Try<PauseBlock> read(final InputStream tzxFile) {
        try {
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            return Try.success(new PauseBlock(pauseLength));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private final Duration pauseLength;

    public PauseBlock(final Duration pauseLength) {
        this.pauseLength = pauseLength;
    }

    public Duration getPauseLength() {
        return pauseLength;
    }

    @Override
    public Iterator<Bit> bits(final SignalState signalState) {
        if (pauseLength.isZero()) {
            return new StopTapeIterator();
        } else {
            return new PauseIterator(signalState, pauseLength);
        }
    }

    @Override
    public String toString() {
        return String.format("%s pause", pauseLength.getSeconds());
    }

    private class StopTapeIterator implements Iterator<Bit> {
        private boolean read = false;

        @Override
        public boolean hasNext() {
            return !read;
        }

        @Override
        public Bit next() {
            return StopTapeBit.INSTANCE;
        }
    }
}
