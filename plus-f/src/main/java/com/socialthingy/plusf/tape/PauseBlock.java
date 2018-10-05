package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

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
    private final int pauseBits;

    public PauseBlock(final Duration pauseLength) {
        this.pauseLength = pauseLength;
        this.pauseBits = (int) pauseLength.toMillis() * 3500;
    }

    public boolean shouldStopTape() {
        return pauseLength.isZero();
    }

    @Override
    public BlockSignal getBlockSignal(SignalState signalState) {
        return new PauseBlockSignal();
    }

    @Override
    public String toString() {
        return String.format("%s pause", pauseLength.getSeconds());
    }

    @Override
    public boolean isDataBlock() {
        return true;
    }

    private class PauseBlockSignal implements BlockSignal {
        private int idx;

        @Override
        public int skip(int count) {
            if (count < pauseBits - idx) {
                idx += count;
                return count;
            } else {
                final int skipped = pauseBits - idx;
                idx = pauseBits;
                return skipped;
            }
        }

        @Override
        public boolean hasNext() {
            return idx < pauseBits;
        }

        @Override
        public Boolean next() {
            idx++;
            return false;
        }
    }
}
