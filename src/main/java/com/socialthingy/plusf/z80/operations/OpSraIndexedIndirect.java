package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpSraIndexedIndirect extends SraOperation {
    private final IndexRegister indexRegister;
    private final int[] memory;
    private final Processor processor;

    public OpSraIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        memory[address] = shift(memory[address]);
        return 23;
    }
}
