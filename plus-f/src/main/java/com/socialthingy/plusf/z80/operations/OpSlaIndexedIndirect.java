package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpSlaIndexedIndirect extends SlaOperation {
    private final IndexRegister indexRegister;
    private final int[] memory;
    private final Processor processor;

    public OpSlaIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        Memory.set(memory, address, shift(memory[address]));
        return 23;
    }
}
