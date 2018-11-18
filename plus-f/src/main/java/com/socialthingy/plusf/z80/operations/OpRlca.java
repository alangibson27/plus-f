package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpRlca extends RotateOperation {

    public OpRlca(final Processor processor) {
        super(processor);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        accumulator.set(rlcValue(accumulator.get()));
    }

    @Override
    public String toString() {
        return "rlca";
    }
}
