package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLd8RegIndexedIndirect implements Operation {

    private final Processor processor;
    private final Memory memory;
    private final Register dest;
    private final IndexRegister indexRegister;

    public OpLd8RegIndexedIndirect(final Processor processor, final Memory memory, final Register dest, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        dest.set(memory.get(indexRegister.withOffset(processor.fetchNextByte())));
        return 19;
    }

    @Override
    public String toString() {
        return String.format("ld %s, (%s + n)", dest.name(), indexRegister.name());
    }
}
