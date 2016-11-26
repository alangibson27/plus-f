package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class LoopStartBlock extends TapeBlock {

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
    public String toString() {
        return String.format("Loop start: %d iterations", iterations);
    }
}
