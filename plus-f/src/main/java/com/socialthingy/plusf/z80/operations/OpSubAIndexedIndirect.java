package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpSubAIndexedIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;
    private final String toString;

    public OpSubAIndexedIndirect(final Processor processor, final Clock clock, final Memory memory, final Register indexRegister, final boolean useCarryFlag) {
        super(processor, clock, useCarryFlag);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);

        if (useCarryFlag) {
            this.toString = "sbc a, (" + indexRegister.name() + " + n)";
        } else {
            this.toString = "sub a, (" + indexRegister.name() + " + n)";
        }
    }

    @Override
    public void execute() {
        accumulator.set(sub(memory.get(indexRegister.withOffset(processor.fetchNextByte())), true));
        clock.tick(5);
    }

    @Override
    public String toString() {
        return toString;
    }
}
