package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;

public class OpRlca extends RotateOperation {

    public OpRlca(final Processor processor) {
        super(processor);
    }

    @Override
    public int execute() {
        accumulator.set(rlcValue(accumulator.get()));
        return 4;
    }
}
