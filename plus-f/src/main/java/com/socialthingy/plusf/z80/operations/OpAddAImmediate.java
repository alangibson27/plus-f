package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpAddAImmediate extends ArithmeticOperation {

    public OpAddAImmediate(final Processor processor, final Clock clock, final boolean useCarryFlag) {
        super(processor, clock, useCarryFlag);
    }

    @Override
    public void execute() {
        super.add(processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return useCarryFlag ? "adc a, n" : "add a, n";
    }
}
