package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpAndIndexedIndirect extends AndOperation {
    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpAndIndexedIndirect(final Processor processor, final Memory memory, final IndexRegister indexRegister) {
        super(processor);
        this.processor = processor;
        this.indexRegister = indexRegister;
        this.memory = memory;
    }

    @Override
    public int execute() {
        and(memory.get(indexRegister.withOffset(processor.fetchNextByte())));
        return 19;
    }

    @Override
    public String toString() {
        return "and (" + indexRegister.name() + " + n)";
    }
}
