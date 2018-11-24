package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpCall extends CallOperation {
    public OpCall(final Processor processor) {
        super(processor);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int target = processor.fetchNextWord();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 3);
        contentionModel.applyContention(initialPcValue + 2, 3);
        contentionModel.applyContention(initialPcValue + 2, 1);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp - 1, 3);
        contentionModel.applyContention(sp - 2, 3);
        call(target);
    }

    @Override
    public String toString() {
        return "call nn";
    }
}
