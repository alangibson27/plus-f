package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpCpImmediate extends ArithmeticOperation {
    public OpCpImmediate(final Processor processor, final Clock clock) {
        super(processor, clock, false);
    }

    @Override
    public void execute() {
        sub(processor.fetchNextByte(), false);
        clock.tick(3);
    }

    @Override
    public String toString() {
        return "cp n";
    }
}
