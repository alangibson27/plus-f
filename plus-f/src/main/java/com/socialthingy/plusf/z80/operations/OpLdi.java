package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpLdi extends BlockOperation {
    public OpLdi(final Processor processor, final int[] memory) {
        super(processor, memory, 1);
    }

    @Override
    public int execute() {
        blockTransfer();
        return 16;
    }

    @Override
    public String toString() {
        return "ldi";
    }
}
