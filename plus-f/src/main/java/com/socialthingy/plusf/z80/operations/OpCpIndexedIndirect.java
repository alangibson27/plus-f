package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpCpIndexedIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpCpIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final Register indexRegister) {
        super(processor, clock, false);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public void execute() {
        sub(memory.get(indexRegister.withOffset(processor.fetchNextByte())), false);
        clock.tick(5);
    }

    @Override
    public String toString() {
        return "cp (" + indexRegister.name() + " + n)";
    }
}
