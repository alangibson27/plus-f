package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpAddAImmediate extends ArithmeticOperation {

    public OpAddAImmediate(final Processor processor, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        super.add(processor.fetchNextByte());
    }

    @Override
    public String toString() {
        return useCarryFlag ? "adc a, n" : "add a, n";
    }
}
