package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdIndexedIndirect8Reg extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final IndexRegister indexRegister;
    private final Register source;

    public OpLdIndexedIndirect8Reg(final Processor processor, final Clock clock, final Memory memory, final Register indexRegister, final Register source) {
        super(clock);
        this.processor = processor;
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.source = source;
    }

    @Override
    public void execute() {
        memory.set( indexRegister.withOffset(processor.fetchNextByte()), source.get());
        clock.tick(11);
    }

    @Override
    public String toString() {
        return String.format("ld (%s + n), %s", indexRegister.name(), source.name());
    }
}
