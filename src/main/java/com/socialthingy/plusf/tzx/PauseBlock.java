package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class PauseBlock extends TzxBlock {

    public static Try<PauseBlock> read(final InputStream tzxFile) {
        try {
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            return Try.success(new PauseBlock(pauseLength));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private final Duration pauseLength;

    private PauseBlock(final Duration pauseLength) {
        this.pauseLength = pauseLength;
    }

    public Duration getPauseLength() {
        return pauseLength;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        tape.add(new Bit(false, "pause"), 3500000 * (int) pauseLength.getSeconds());
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s pause", pauseLength.getSeconds());
    }
}
