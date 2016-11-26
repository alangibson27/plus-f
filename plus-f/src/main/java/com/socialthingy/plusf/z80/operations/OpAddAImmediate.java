package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpAddAImmediate extends ArithmeticOperation {

    public OpAddAImmediate(final Processor processor, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
    }

    @Override
    public int execute() {
        super.add(processor.fetchNextByte());
        return 7;
    }

    @Override
    public String toString() {
        return useCarryFlag ? "adc a, n" : "add a, n";
    }
}
