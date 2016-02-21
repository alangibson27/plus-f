package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpBitIndexedIndirect extends BitOperation {

    private final int[] memory;
    private final IndexRegister indexRegister;
    private final Processor processor;

    public OpBitIndexedIndirect(final Processor processor, final int[] memory, final IndexRegister indexRegister, final int bitPosition) {
        super(processor, bitPosition);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int offset = processor.fetchRelative(-2);
        checkBit(memory[indexRegister.withOffset(offset)]);
        return 20;
    }
}
