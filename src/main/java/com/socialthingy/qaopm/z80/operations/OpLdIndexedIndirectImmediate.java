package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLdIndexedIndirectImmediate implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Register indexRegister;

    public OpLdIndexedIndirectImmediate(final Processor processor, final int[] memory, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextPC();
        final int value = processor.fetchNextPC();
        memory[0xffff & (indexRegister.get() + offset)] = value;
        return 19;
    }
}
