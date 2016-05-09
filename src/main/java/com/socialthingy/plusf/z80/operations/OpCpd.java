package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpCpd extends BlockOperation {
    public OpCpd(final Processor processor, final int[] memory) {
        super(processor, memory, -1);
    }

    @Override
    public int execute() {
        blockCompare();
        return 16;
    }
}
