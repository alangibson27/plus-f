package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpAndIndexedIndirect extends AndOperation {
    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpAndIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        and(memory[indexRegister.withOffset(processor.fetchNextPC())]);
        return 19;
    }

    @Override
    public String toString() {
        return "and (" + indexRegister.name() + ")";
    }
}
