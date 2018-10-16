package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpCpi extends BlockOperation {
    public OpCpi(final Processor processor, final Memory memory) {
        super(processor, memory, 1);
    }

    @Override
    public int execute() {
        blockCompare();
        return 16;
    }

    @Override
    public String toString() {
        return "cpi";
    }
}
