package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class JumpBlock extends TapeBlock {

    private final int block;

    public static Try<JumpBlock> read(final InputStream tzxFile) {
        try {
            final int blocks = nextWord(tzxFile);
            return Try.success(new JumpBlock(blocks));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public JumpBlock(final int block) {
        this.block = block;
    }

    public int getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return String.format("Jump block: %d", block);
    }
}
