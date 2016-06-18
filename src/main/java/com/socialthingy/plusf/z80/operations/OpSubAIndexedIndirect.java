package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSubAIndexedIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpSubAIndexedIndirect(final Processor processor, final int[] memory, final Register indexRegister, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        accumulator.set(sub(memory[indexRegister.withOffset(processor.fetchNextByte())]));
        return 19;
    }

    @Override
    public String toString() {
        return "sub (" + indexRegister.name() + ")";
    }
}
