package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpSetIndexedIndirect extends BitModificationOperation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpSetIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister, final int bitPosition) {
        super(bitPosition);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int offset = processor.fetchRelative(-2);
        final int address = indexRegister.withOffset(offset);
        memory[address] = set(memory[address]);
        return 23;
    }
}
