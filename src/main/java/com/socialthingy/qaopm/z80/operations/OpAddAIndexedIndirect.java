package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpAddAIndexedIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpAddAIndexedIndirect(final Processor processor, final int[] memory, final Register indexRegister, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        add(memory[indexRegister.withOffset(processor.fetchNextPC())]);
        return 19;
    }
}
