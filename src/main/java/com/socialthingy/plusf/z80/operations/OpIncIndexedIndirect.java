package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpIncIndexedIndirect extends IncOperation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpIncIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int address = indexRegister.withOffset(processor.fetchNextPC());
        final int result = increment(memory[address]);
        memory[address] = result;
        return 23;
    }
}
