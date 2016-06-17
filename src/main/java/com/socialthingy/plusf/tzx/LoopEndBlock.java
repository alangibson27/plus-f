package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class LoopEndBlock extends TzxBlock {

    public static Try<LoopEndBlock> read(final InputStream tzxFile) {
        return Try.success(new LoopEndBlock());
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return String.format("Loop end");
    }
}
