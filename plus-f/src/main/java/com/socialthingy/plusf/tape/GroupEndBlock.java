package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.InputStream;

public class GroupEndBlock extends TapeBlock {

    public static Try<GroupEndBlock> read(final InputStream tzxFile) {
        return Try.success(new GroupEndBlock());
    }

    @Override
    public String toString() {
        return "Group end";
    }
}
