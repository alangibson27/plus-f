package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;

public class OpRlca extends RotateOperation {

    public OpRlca(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        accumulator.set(rlcValue(accumulator.get()));
        return 4;
    }

    @Override
    public String toString() {
        return "rlca";
    }
}
