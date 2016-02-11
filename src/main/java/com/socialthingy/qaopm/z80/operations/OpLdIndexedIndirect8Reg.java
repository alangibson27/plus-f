package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLdIndexedIndirect8Reg implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Register indexRegister;
    private final Register source;

    public OpLdIndexedIndirect8Reg(final Processor processor, final int[] memory, final Register indexRegister, final Register source) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
        this.source = source;
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextPC();
        memory[0xffff & (indexRegister.get() + offset)] = source.get();
        return 19;
    }
}
