package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpCpImmediate extends ArithmeticOperation {
    public OpCpImmediate(final Processor processor) {
        super(processor, false);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        sub(processor.fetchNextByte(), false);
    }

    @Override
    public String toString() {
        return "cp n";
    }
}
