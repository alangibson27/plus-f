package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpLdd extends BlockOperation {
    public OpLdd(final Processor processor, final int[] memory) {
        super(processor, memory, -1);
    }

    @Override
    public int execute() {
        blockTransfer();
        return 16;
    }
}
