package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;

public class GroupEndBlock extends TzxBlock {

    public static Try<GroupEndBlock> read(final InputStream tzxFile) {
        return Try.success(new GroupEndBlock());
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // NOP
        return initialState;
    }

    @Override
    public String toString() {
        return "Group end";
    }
}
