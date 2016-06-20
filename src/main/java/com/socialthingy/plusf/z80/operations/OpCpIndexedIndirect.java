package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCpIndexedIndirect extends ArithmeticOperation {

    private final int[] memory;
    private final IndexRegister indexRegister;

    public OpCpIndexedIndirect(final Processor processor, final int[] memory, final Register indexRegister) {
        super(processor, false);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        sub(memory[indexRegister.withOffset(processor.fetchNextByte())]);
        return 19;
    }

    @Override
    public String toString() {
        return "cp (" + indexRegister.name() + ")";
    }
}
