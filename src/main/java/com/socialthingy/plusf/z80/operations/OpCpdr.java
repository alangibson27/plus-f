package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpCpdr extends BlockOperation {
    public OpCpdr(final Processor processor, final int[] memory) {
        super(processor, memory, -1);
    }

    @Override
    public int execute() {
        if (blockCompare() == 0) {
            return 16;
        } else {
            return adjustPC();
        }
    }
}
