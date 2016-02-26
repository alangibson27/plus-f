package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpCpi extends BlockOperation {
    public OpCpi(final Processor processor, final int[] memory) {
        super(processor, memory, 1);
    }

    @Override
    public int execute() {
        blockCompare();
        return 16;
    }
}
