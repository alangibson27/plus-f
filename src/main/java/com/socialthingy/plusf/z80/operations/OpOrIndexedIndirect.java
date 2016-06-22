package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpOrIndexedIndirect extends OrOperation {
    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpOrIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        or(memory[indexRegister.withOffset(processor.fetchNextByte())]);
        return 19;
    }

    @Override
    public String toString() {
        return "or (" + indexRegister.name() + " + n)";
    }
}
