package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.RepeatingList;
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

    public boolean shouldStopTape() {
        return pauseLength.isZero();
    }

    @Override
    public Iterator<Boolean> bits(final SignalState signalState) {
        return getBits(signalState).iterator();
    }

    @Override
    public String toString() {
        return String.format("%s pause", pauseLength.getSeconds());
    }

    public RepeatingList<Boolean> getBits(final SignalState signalState) {
        final RepeatingList<Boolean> bits = new RepeatingList<>();
        bits.add(false, (int) pauseLength.toMillis() * 3500);
        return bits;
    }
}
