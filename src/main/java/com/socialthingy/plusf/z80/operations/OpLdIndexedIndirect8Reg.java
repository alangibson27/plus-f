package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdIndexedIndirect8Reg implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IndexRegister indexRegister;
    private final Register source;

    public OpLdIndexedIndirect8Reg(final Processor processor, final int[] memory, final Register indexRegister, final Register source) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.source = source;
    }

    @Override
    public int execute() {
        memory[indexRegister.withOffset(processor.fetchNextPC())] = source.get();
        return 19;
    }
}
