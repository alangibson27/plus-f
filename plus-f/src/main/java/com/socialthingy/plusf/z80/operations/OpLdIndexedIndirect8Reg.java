package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIndexedIndirect8Reg implements Operation {

    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;
    private final Register source;

    public OpLdIndexedIndirect8Reg(final Processor processor, final Memory memory, final Register indexRegister, final Register source) {
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.source = source;
    }

    @Override
    public int execute() {
        memory.set( indexRegister.withOffset(processor.fetchNextByte()), source.get());
        return 19;
    }

    @Override
    public String toString() {
        return String.format("ld (%s + n), %s", indexRegister.name(), source.name());
    }
}
