package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCpIndexedIndirect extends ArithmeticOperation {

    private final Memory memory;
    private final IndexRegister indexRegister;

    public OpCpIndexedIndirect(final Processor processor, final Memory memory, final Register indexRegister) {
        super(processor, false);
        this.memory = memory;
        this.indexRegister = IndexRegister.class.cast(indexRegister);
    }

    @Override
    public int execute() {
        sub(memory.get(indexRegister.withOffset(processor.fetchNextByte())), false);
        return 19;
    }

    @Override
    public String toString() {
        return "cp (" + indexRegister.name() + " + n)";
    }
}
