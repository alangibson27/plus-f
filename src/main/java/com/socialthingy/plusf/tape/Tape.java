package com.socialthingy.plusf.tape;

import java.util.List;

public class Tape {
    private String version;
    private TapeBlock[] blocks;

    public Tape(final String version, final List<TapeBlock> blocks) throws TapeException {
        this.version = version;
        this.blocks = blocks.toArray(new TapeBlock[blocks.size()]);
    }

    public String getVersion() {
        return version;
    }

    public TapeBlock[] getBlocks() {
        return blocks;
    }
}
