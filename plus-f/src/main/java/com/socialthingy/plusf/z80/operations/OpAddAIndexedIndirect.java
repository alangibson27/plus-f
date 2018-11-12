package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpAddAIndexedIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;
    private final String toString;

    public OpAddAIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final Register indexRegister, final boolean useCarryFlag) {
        super(processor, clock, useCarryFlag);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);

        if (useCarryFlag) {
            this.toString = "adc a, (" + indexRegister.name() + " + n)";
        } else {
            this.toString = "add a, (" + indexRegister.name() + " + n)";
        }
    }

    @Override
    public void execute() {
        final int addr = indexRegister.withOffset(processor.fetchNextByte());
        clock.tick(5);
        add(memory.get(addr));
    }

    @Override
    public String toString() {
        return toString;
    }
}
