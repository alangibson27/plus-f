package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpSubAImmediate extends ArithmeticOperation {

    public OpSubAImmediate(final Processor processor, final Clock clock, final boolean useCarryFlag) {
        super(processor, clock, useCarryFlag);
    }

    @Override
    public void execute() {
        accumulator.set(sub(processor.fetchNextByte(), true));
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
        clock.tick(3);
    }

    @Override
    public String toString() {
        return useCarryFlag ? "sbc a, n" : "sub n";
    }
}
