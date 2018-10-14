package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddAIndexedIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;
    private final String toString;

    public OpAddAIndexedIndirect(final Processor processor, final Memory memory, final Register indexRegister, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);

        if (useCarryFlag) {
            this.toString = "adc a, (" + indexRegister.name() + " + n)";
        } else {
            this.toString = "add a, (" + indexRegister.name() + " + n)";
        }
    }

    @Override
    public int execute() {
        add(memory.get(indexRegister.withOffset(processor.fetchNextByte())));
        return 19;
    }

    @Override
    public String toString() {
        return toString;
    }
}
