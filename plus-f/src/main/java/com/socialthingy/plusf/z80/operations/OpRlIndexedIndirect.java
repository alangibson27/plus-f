package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpRlIndexedIndirect extends RotateOperation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpRlIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int address = indexRegister.withOffset(processor.fetchRelative(-2));
        final int result = rlValue(memory.get(address));
        setSignZeroAndParity(result);
        memory.set( address, result);
        return 23;
    }
}
