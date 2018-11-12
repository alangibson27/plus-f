package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;

public class OpRlca extends RotateOperation {

    public OpRlca(final Processor processor, final Clock clock) {
        super(processor, clock);
    }

    @Override
    public void execute() {
        accumulator.set(rlcValue(accumulator.get()));
    }

    @Override
    public String toString() {
        return "rlca";
    }
}
