package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpDecIndexedIndirect extends DecOperation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpDecIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int address = indexRegister.withOffset(processor.fetchNextPC());
        final int result = decrement(memory[address]);
        Memory.set(memory, address, result);
        return 23;
    }

    @Override
    public String toString() {
        return "dec (" + indexRegister.name() + ")";
    }
}
