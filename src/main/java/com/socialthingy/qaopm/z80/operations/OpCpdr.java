package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpCpdr extends BlockOperation {
    public OpCpdr(final Processor processor, final int[] memory) {
        super(processor, memory, -1);
    }

    @Override
    public int execute() {
        blockCompare();
        return adjustPC();
    }
}
