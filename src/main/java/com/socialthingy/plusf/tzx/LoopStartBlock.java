package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class LoopStartBlock extends TzxBlock {

    private final int iterations;

    public static Try<LoopStartBlock> read(final InputStream tzxFile) {
        try {
            final int iterations = nextWord(tzxFile);
            return Try.success(new LoopStartBlock(iterations));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public LoopStartBlock(final int iterations) {
        this.iterations = iterations;
    }

    public int getIterations() {
        return iterations;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return String.format("Loop start: %d iterations", iterations);
    }
}
