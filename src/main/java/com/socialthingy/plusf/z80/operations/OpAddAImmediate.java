package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpAddAImmediate extends ArithmeticOperation {

    public OpAddAImmediate(final Processor processor, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
    }

    @Override
    public int execute() {
        super.add(processor.fetchNextPC());
        return 7;
    }

    @Override
    public String toString() {
        return "add a, n";
    }
}
