package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.InputStream;

public class LoopEndBlock extends TapeBlock {

    public static Try<LoopEndBlock> read(final InputStream tzxFile) {
        return Try.success(new LoopEndBlock());
    }

    @Override
    public String toString() {
        return String.format("Loop end");
    }
}
